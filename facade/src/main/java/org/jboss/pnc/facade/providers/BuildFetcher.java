/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.providers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.facade.util.MergeIterator;
import org.jboss.pnc.facade.validation.CorruptedDataException;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.CursorPageInfo;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Math.min;

/**
 * This class is used for fetching builds in an DB effective way. This means reading data in bulk and precisely
 * computing what that bulk should contain and utilizes data preloading and caching.
 */
@RequestScoped
@Slf4j
public class BuildFetcher {

    @Inject
    private BuildMapper buildMapper;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildCoordinator buildCoordinator;

    @Inject
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private final Map<String, BuildTask> buildTaskCache = new HashMap<>();
    private final Map<Base32LongID, BuildRecord> buildRecordCache = new HashMap<>();
    private final Set<Base32LongID> depsToFetch = new HashSet<>();

    private final Set<Base32LongID> depsProcessed = new HashSet<>();

    public BuildWithDeps getBuildWithDeps(String id) {
        BuildTask buildTask = buildTaskCache.get(id);
        if (buildTask != null) {
            return new BuildWithDeps(buildMapper.fromBuildTask(buildTask), buildTask);
        }
        Base32LongID dbid = buildMapper.getIdMapper().toEntity(id);
        BuildRecord buildRecord = buildRecordCache.get(dbid);
        if (buildRecord == null) {
            buildRecord = buildRecordRepository.queryById(dbid);
            if (buildRecord == null) {
                throw new CorruptedDataException("Missing build with id:" + id);
            }
            log.warn("Prefetch missed build {}", id);
            buildRecordCache.put(buildRecord.getId(), buildRecord);
        }
        return new BuildWithDeps(buildMapper.toDTO(buildRecord), buildRecord);
    }

    public static Comparator<BuildWrapper> wrapperComparator(Comparator<Build> comparator) {
        return (o1, o2) -> (comparator.compare(o1.mappedBuild, o2.mappedBuild));
    }

    public List<Build> getBuildPage(
            int pageIndex,
            int pageSize,
            List<Build> runningBuilds,
            Predicate<BuildRecord>[] predicates,
            SortInfo<BuildRecord> sortInfo,
            Comparator<Build> comparing) {

        int firstPossibleDBIndex = pageIndex * pageSize - runningBuilds.size();
        int lastPossibleDBIndex = (pageIndex + 1) * pageSize - 1;
        int toSkip = min(runningBuilds.size(), pageIndex * pageSize);

        Iterator<BuildWrapper> wrappedRunningBuilds = runningBuilds.stream()
                .sorted(comparing)
                .map(BuildWrapper::new)
                .iterator();
        BuildIterator buildIterator = new BuildIterator(
                firstPossibleDBIndex,
                lastPossibleDBIndex,
                pageSize,
                sortInfo,
                predicates);
        MergeIterator<BuildWrapper> buildsIT = new MergeIterator<>(
                wrappedRunningBuilds,
                buildIterator,
                wrapperComparator(comparing));
        List<BuildWrapper> builds = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(buildsIT, Spliterator.ORDERED | Spliterator.SORTED), false)
                .skip(toSkip)
                .limit(pageSize)
                .collect(Collectors.toList());

        fetchBuildConfigAudited(builds.stream().flatMap(BuildWrapper::buildRecordStream).collect(Collectors.toSet()));
        return builds.stream().map(BuildWrapper::getBuild).collect(Collectors.toList());
    }

    /**
     * Fetch all running builds from build coordinator and cache them.
     */
    public void fetchRunningBuilds() {
        try {
            for (BuildTask buildTask : buildCoordinator.getSubmittedBuildTasks()) {
                buildTaskCache.put(buildTask.getId(), buildTask);
            }
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean buildExists(Base32LongID buildId) {
        try {
            Optional<BuildTask> submittedBuildTask = buildCoordinator.getSubmittedBuildTask(buildId.getId());
            if (submittedBuildTask.isPresent()) {
                fetchRunningBuilds();
                return true;
            }
            BuildRecord buildRecord = buildRecordRepository.queryById(buildId);
            if (buildRecord == null) {
                return false;
            }
            buildRecordCache.put(buildRecord.getId(), buildRecord);
            return true;
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the set of build IDs that are in a Group Build. Empty set means the Group Build does not exist.
     */
    public Set<Base32LongID> getGroupBuildContent(long buildConfigSetRecordId) {
        try {
            Set<Base32LongID> buildIds = new HashSet<>();

            List<BuildTask> buildTasks = buildCoordinator.getSubmittedBuildTasksBySetId(buildConfigSetRecordId);
            if (!buildTasks.isEmpty()) {
                fetchRunningBuilds();
                buildTasks.stream()
                        .map(BuildTask::getId)
                        .map(buildMapper.getIdMapper()::toEntity)
                        .forEach(buildIds::add);
            }

            // we need to check DB after build coordinator because of possible race condition
            BuildConfigSetRecord buildGroup = buildConfigSetRecordRepository.queryById(buildConfigSetRecordId);
            if (buildGroup == null) {
                return Set.of();
            }
            buildGroup.getBuildRecords().stream().map(BuildRecord::getId).forEach(buildIds::add);
            return buildIds;
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fetches the build and all its transitive dependencies and dependants to cache.
     */
    public void precacheAllBuildsDeps(Base32LongID buildId) {
        checkCacheForMissingDeps(buildId);
        fetchRemainingDeps();
        fetchBuildConfigAudited(buildRecordCache.values());
    }

    /**
     * Fetches the builds and all their transitive dependencies and dependants to cache.
     */
    public void precacheAllBuildsDeps(Collection<Base32LongID> buildIds) {
        for (Base32LongID buildId : buildIds) {
            checkCacheForMissingDeps(buildId);
        }
        fetchRemainingDeps();
        fetchBuildConfigAudited(buildRecordCache.values());
    }

    /**
     * Recursively checks cached build tasks if all their dependencies and dependants are cached, and add to set of ids
     * to fetch if any is missing. This can happen when a dependency already finished, but parent is still running.
     */
    private void checkBuildTasksForMissingDeps(String id) {
        Base32LongID dbId = buildMapper.getIdMapper().toEntity(id);
        if (depsProcessed.contains(dbId)) {
            return;
        }
        BuildTask buildTask = buildTaskCache.get(id);
        if (buildTask == null) {
            // No build task for given ID, will need to fetch this one from DB
            depsToFetch.add(dbId);
        } else {
            depsProcessed.add(dbId);
            for (BuildTask dependency : buildTask.getDependencies()) {
                checkBuildTasksForMissingDeps(dependency.getId());
            }
            for (BuildTask dependant : buildTask.getDependants()) {
                checkBuildTasksForMissingDeps(dependant.getId());
            }
        }
    }

    /**
     * Check if the build with given id is already cached or not; if not, adds it to the set of ids to fetch, if yes, it
     * recursively checks its dependencies and dependants in the same way.
     */
    private void checkCacheForMissingDeps(Base32LongID id) {
        if (depsProcessed.contains(id)) {
            return;
        }
        if (buildTaskCache.containsKey(id.getId())) {
            checkBuildTasksForMissingDeps(id.getId());
        } else if (buildRecordCache.containsKey(id)) {
            BuildRecord buildRecord = buildRecordCache.get(id);
            depsProcessed.add(id);
            for (Base32LongID dependency : buildRecord.getDependencyBuildRecordIds()) {
                checkCacheForMissingDeps(dependency);
            }
            for (Base32LongID dependant : buildRecord.getDependentBuildRecordIds()) {
                checkCacheForMissingDeps(dependant);
            }
        } else {
            depsToFetch.add(id);
        }
    }

    /**
     * Goes through the set of builds missing in cache, fetches them all and then fetches all their dependencies and
     * dependants layer by layer.
     */
    private void fetchRemainingDeps() {
        depsToFetch.removeAll(buildRecordCache.keySet());
        while (!depsToFetch.isEmpty()) {
            List<BuildRecord> buildRecords = buildRecordRepository
                    .queryWithPredicates(BuildRecordPredicates.withIds(depsToFetch));
            depsToFetch.clear();
            for (BuildRecord buildRecord : buildRecords) {
                buildRecordCache.put(buildRecord.getId(), buildRecord);
                if (depsProcessed.contains(buildRecord.getId())) {
                    continue;
                }
                depsProcessed.add(buildRecord.getId());
                depsToFetch.addAll(buildRecord.getDependencyBuildRecordIds());
                depsToFetch.addAll(buildRecord.getDependentBuildRecordIds());
            }
            depsToFetch.removeAll(buildRecordCache.keySet());
        }
    }

    private void fetchBuildConfigAudited(Collection<BuildRecord> buildRecords) {
        if (buildRecords.isEmpty()) {
            return;
        }
        Set<IdRev> idRevs = buildRecords.stream()
                .filter(buildRecord -> buildRecord.getBuildConfigurationAudited() == null)
                .map(BuildRecord::getBuildConfigurationAuditedIdRev)
                .collect(Collectors.toSet());
        prefetchFieldsOfBuildConfigs(idRevs);
        Map<IdRev, BuildConfigurationAudited> buildConfigRevisions = buildConfigurationAuditedRepository
                .queryById(idRevs);
        for (BuildRecord buildRecord : buildRecords) {
            if (buildRecord.getBuildConfigurationAudited() != null) {
                continue;
            }
            IdRev idRev = buildRecord.getBuildConfigurationAuditedIdRev();
            BuildConfigurationAudited buildConfigurationAudited = buildConfigRevisions.get(idRev);
            buildRecord.setBuildConfigurationAudited(buildConfigurationAudited);
        }
    }

    private void prefetchFieldsOfBuildConfigs(Set<IdRev> idRevs) {
        Set<Integer> buildConfigIDs = idRevs.stream().map(IdRev::getId).collect(Collectors.toSet());
        buildConfigurationRepository.queryWithPredicates(BuildConfigurationPredicates.withIds(buildConfigIDs));
    }

    /**
     * Wrapper around {@link Build} that has also sets of ids of its deps (dependencies and dependants).
     */
    @Getter
    public static class BuildWithDeps {
        private final Build build;
        private final Collection<String> dependencies;
        private final Collection<String> dependants;

        public BuildWithDeps(Build build, BuildTask buildTask) {
            this.build = build;
            dependencies = buildTask.getDependencies().stream().map(BuildTask::getId).collect(Collectors.toSet());
            dependants = buildTask.getDependants().stream().map(BuildTask::getId).collect(Collectors.toSet());
        }

        public BuildWithDeps(Build build, BuildRecord buildRecord) {
            this.build = build;
            dependencies = buildRecord.getDependencyBuildRecordIds()
                    .stream()
                    .map(BuildMapper.idMapper::toDto)
                    .collect(Collectors.toSet());
            dependants = buildRecord.getDependentBuildRecordIds()
                    .stream()
                    .map(BuildMapper.idMapper::toDto)
                    .collect(Collectors.toSet());
        }
    }

    public class BuildIterator implements Iterator<BuildWrapper> {

        private final int maxPageSize;
        private final int lastIndex;
        private final SortInfo<BuildRecord> sortInfo;
        private final Predicate<BuildRecord>[] predicates;
        private List<BuildRecord> builds;
        private Iterator<BuildRecord> it;
        private int firstIndex;

        public BuildIterator(
                int firstIndex,
                int lastIndex,
                int pageSize,
                SortInfo<BuildRecord> sortInfo,
                Predicate<BuildRecord>... predicate) {
            this.maxPageSize = Math.max(pageSize, 10);
            this.firstIndex = Math.max(firstIndex, 0);
            this.lastIndex = lastIndex;
            this.predicates = predicate;
            this.sortInfo = sortInfo;
            nextPage();
        }

        @Override
        public boolean hasNext() {
            if (it.hasNext()) {
                return true;
            }
            if (firstIndex > lastIndex) {
                return false;
            }
            nextPage();
            return it.hasNext();
        }

        @Override
        public BuildWrapper next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return new BuildWrapper(it.next());
        }

        private void nextPage() {
            int size = lastIndex - firstIndex + 1;
            if (size > maxPageSize) {
                size = maxPageSize;
            }
            PageInfo pageInfo = new CursorPageInfo(firstIndex, size);
            builds = buildRecordRepository.queryWithPredicates(pageInfo, sortInfo, predicates);
            it = builds.iterator();
            if (builds.size() < size) {
                firstIndex = lastIndex + 1;
            } else {
                firstIndex += size;
            }
        }
    }

    public class BuildWrapper {
        private final BuildRecord buildRecord;

        private final Build mappedBuild;

        public BuildWrapper(BuildRecord buildRecord) {
            this.buildRecord = buildRecord;
            this.mappedBuild = buildMapper.toDTOWithoutBCR(buildRecord);
        }

        public BuildWrapper(Build build) {
            this.buildRecord = null;
            this.mappedBuild = build;
        }

        public Build getBuild() {
            if (buildRecord == null) {
                return mappedBuild;
            } else {
                return buildMapper.toDTO(buildRecord);
            }
        }

        public Stream<BuildRecord> buildRecordStream() {
            return Stream.ofNullable(buildRecord);
        }
    }
}
