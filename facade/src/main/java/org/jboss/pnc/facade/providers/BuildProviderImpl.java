/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.gerrit.Gerrit;
import org.jboss.pnc.common.gerrit.GerritException;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.util.MergeIterator;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.Math.min;
import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProjectId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigSetId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigSetRecordId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIds;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withPerformedInMilestone;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withUserId;

@PermitAll
@Stateless
public class BuildProviderImpl extends AbstractIntIdProvider<BuildRecord, Build, BuildRef> implements BuildProvider {

    private BuildRecordRepository buildRecordRepository;
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private Gerrit gerrit;
    private BuildConfigurationRevisionMapper buildConfigurationRevisionMapper;
    private BuildMapper buildMapper;

    private BuildCoordinator buildCoordinator;
    private SortInfoProducer sortInfoProducer;

    @Inject
    public BuildProviderImpl(BuildRecordRepository repository,
                             BuildMapper mapper,
                             BuildConfigurationRepository buildConfigurationRepository,
                             BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
                             Gerrit gerrit,
                             BuildConfigurationRevisionMapper buildConfigurationRevisionMapper,
                             BuildCoordinator buildCoordinator,
                             SortInfoProducer sortInfoProducer) {
        super(repository, mapper, BuildRecord.class);

        this.buildRecordRepository = repository;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.gerrit = gerrit;
        this.buildConfigurationRevisionMapper = buildConfigurationRevisionMapper;
        this.buildMapper = mapper;
        this.buildCoordinator = buildCoordinator;
        this.sortInfoProducer = sortInfoProducer;
    }

    @Override
    public Build store(Build restEntity) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct build creation is not available.");
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public void delete(String id) {
        super.delete(id);
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public Build update(String id, Build restEntity) {
        return super.update(id, restEntity);
    }

    @Override
    public void addAttribute(String id, String key, String value) {
        BuildRecord buildRecord = getBuildRecord(id);
        if(null == key){
            throw new IllegalArgumentException("Attribute key must not be null");
        }
        switch (key) {
            case Attributes.BUILD_BREW_NAME: // workaround for NCL-4889
                buildRecord.setExecutionRootName(value);
                break;
            case Attributes.BUILD_BREW_VERSION: // workaround for NCL-4889
                buildRecord.setExecutionRootVersion(value);
                break;
            default:
                buildRecord.putAttribute(key, value);
                break;
        }
        repository.save(buildRecord);
    }

    @Override
    public void removeAttribute(String id, String key) {
        BuildRecord buildRecord = getBuildRecord(id);
        switch (key) {
            case Attributes.BUILD_BREW_NAME: // workaround for NCL-4889
                buildRecord.setExecutionRootName(null);
                break;
            case Attributes.BUILD_BREW_VERSION: // workaround for NCL-4889
                buildRecord.setExecutionRootVersion(null);
                break;
            default:
                buildRecord.removeAttribute(key);
                break;
        }
        repository.save(buildRecord);
    }

    @Override
    public BuildConfigurationRevision getBuildConfigurationRevision(String id) {

        BuildRecord buildRecord = getBuildRecord(id);

        if (buildRecord.getBuildConfigurationAudited() != null) {
            return buildConfigurationRevisionMapper.toDTO(buildRecord.getBuildConfigurationAudited());
        } else {
            BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                    .queryById(new IdRev(buildRecord.getBuildConfigurationId(), buildRecord.getBuildConfigurationRev()));

            return buildConfigurationRevisionMapper.toDTO(buildConfigurationAudited);
        }
    }

    @Override
    public String getRepourLog(String id) {
        return getBuildRecord(id).getRepourLog();
    }

    @Override
    public String getBuildLog(String id) {
        return getBuildRecord(id).getBuildLog();
    }

    @Override
    public SSHCredentials getSshCredentials(String id) {
        BuildRecord buildRecord = getBuildRecord(id);

        return SSHCredentials
                .builder()
                .command(buildRecord.getSshCommand())
                .password(buildRecord.getSshPassword())
                .build();
    }

    @Override
    public Page<Build> getAll(int pageIndex, int pageSize, String sort, String query) {
        BuildPageInfo pageInfo = new BuildPageInfo(pageIndex, pageSize, sort, query, false, false);
        return getBuilds(pageInfo);
    }

    @Override
    public Page<Build> getBuilds(BuildPageInfo pageInfo) {
        return getBuildList(pageInfo,  _t -> true, (_r, _q, cb) -> cb.conjunction());
    }

    @Override
    public Page<Build> getBuildsForMilestone(BuildPageInfo pageInfo, String milestoneId) {
        java.util.function.Predicate<BuildTask> predicate = t -> Integer.valueOf(milestoneId).equals(t.getProductMilestone().getId());
        return getBuildList(pageInfo, predicate, withPerformedInMilestone(Integer.valueOf(milestoneId)));
    }

    @Override
    public Page<Build> getBuildsForProject(BuildPageInfo pageInfo,
                                           String projectId) {
        @SuppressWarnings("unchecked")
        Set<Integer> buildConfigIds = buildConfigurationRepository
                .queryWithPredicates(withProjectId(Integer.valueOf(projectId)))
                .stream()
                .map(BuildConfiguration::getId)
                .collect(Collectors.toSet());

        java.util.function.Predicate<BuildTask> predicate = t -> Integer.valueOf(projectId).equals(t.getBuildConfigurationAudited().getProject().getId());
        return getBuildList(pageInfo, predicate, withBuildConfigurationIds(buildConfigIds));
    }

    @Override
    public Page<Build> getBuildsForBuildConfiguration(BuildPageInfo pageInfo, String buildConfigurationId) {
        java.util.function.Predicate<BuildTask> predicate = t -> Integer.valueOf(buildConfigurationId).equals(t.getBuildConfigurationAudited().getId());
        return getBuildList(pageInfo, predicate, withBuildConfigurationId(Integer.valueOf(buildConfigurationId)));
    }

    @Override
    public Page<Build> getBuildsForUser(BuildPageInfo pageInfo, String userId) {
        java.util.function.Predicate<BuildTask> predicate = t -> Integer.valueOf(userId).equals(t.getUser().getId());
        return getBuildList(pageInfo, predicate, withUserId(Integer.valueOf(userId)));
    }

    @Override
    public Page<Build> getBuildsForGroupConfiguration(BuildPageInfo pageInfo, String groupConfigurationId) {
        java.util.function.Predicate<BuildTask> predicate = t -> t.getBuildSetTask() != null && t.getBuildSetTask().getBuildConfigSetRecord().map(gc -> Integer.valueOf(groupConfigurationId).equals(gc.getBuildConfigurationSet().getId())).orElse(false);
        return getBuildList(pageInfo, predicate, withBuildConfigSetId(Integer.valueOf(groupConfigurationId)));
    }

    @Override
    public Page<Build> getBuildsForGroupBuild(BuildPageInfo pageInfo, String groupBuildId) {
        java.util.function.Predicate<BuildTask> predicate = t -> t.getBuildSetTask() != null && t.getBuildSetTask().getBuildConfigSetRecord().map(gc -> Integer.valueOf(groupBuildId).equals(gc.getId())).orElse(false);
        return getBuildList(pageInfo, predicate, withBuildConfigSetRecordId(Integer.valueOf(groupBuildId)));
    }

    @Override
    public Graph<Build> getGroupBuildGraph(String id) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public URI getInternalScmArchiveLink(String id) {

        BuildRecord buildRecord = repository.queryById(Integer.valueOf(id));

        if (buildRecord.getScmRevision() == null) {
            return null;
        } else {

            try {
                return new URI(gerrit.generateDownloadUrlWithGerritGitweb(buildRecord.getScmRepoURL(), buildRecord.getScmRevision()));
            } catch (GerritException | URISyntaxException e) {
                throw new RepositoryViolationException(e);
            }
        }
    }

    /**
     * If a build record with the id is not found, EmptyEntityException is thrown
     * @param id
     * @return BuildRecord
     * @throws EmptyEntityException if build record with associated id does not exist
     */
    private BuildRecord getBuildRecord(String id) {
        BuildRecord buildRecord = repository.queryById(Integer.valueOf(id));

        if (buildRecord == null) {
            throw new EmptyEntityException("Build with id: " + id + " does not exist!");
        } else {
            return buildRecord;
        }
    }

    @Override
    public Build getSpecific(String id) {

        List<BuildTask> runningBuilds = buildCoordinator.getSubmittedBuildTasks();

        Build build = runningBuilds.stream()
                .filter(buildTask -> id.equals(Integer.toString(buildTask.getId())))
                .findAny()
                .map(buildMapper::fromBuildTask)
                .orElse(null);

        // if build not in runningBuilds, check the database
        if (build == null) {
            // use findByIdFetchProperties instead of super.getSpecific to get 'BuildConfigurationAudited' object
            build = mapper.toDTO(buildRecordRepository.findByIdFetchProperties(Integer.valueOf(id)));
        }

        return build;
    }

    @Override
    public Page<Build> getAllByStatusAndLogContaining(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            BuildStatus status,
            String search) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query,
                BuildRecordPredicates.withStatus(status),
                BuildRecordPredicates.withBuildLogContains(search)
        );
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public void setBuiltArtifacts(String id, List<String> artifactIds) {
        BuildRecord buildRecord = repository.queryById(Integer.valueOf(id));
        Set<Artifact> artifacts = artifactIds.stream()
                .map(aId -> Artifact.Builder.newBuilder().id(Integer.valueOf(aId)).build())
                .collect(Collectors.toSet());
        buildRecord.setBuiltArtifacts(artifacts);
        repository.save(buildRecord);
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public void setDependentArtifacts(String id, List<String> artifactIds) {
        BuildRecord buildRecord = repository.queryById(Integer.valueOf(id));
        Set<Artifact> artifacts = artifactIds.stream()
                .map(aId -> Artifact.Builder.newBuilder().id(Integer.valueOf(aId)).build())
                .collect(Collectors.toSet());
        buildRecord.setDependencies(artifacts);
        repository.save(buildRecord);
    }

    /**
     * Returns the page of builds filtered by given BuildPageInfo parameters and predicate.
     */
    private Page<Build> getBuildList(BuildPageInfo pageInfo, java.util.function.Predicate<BuildTask> predicate, Predicate<BuildRecord> dbPredicate) {
        if (pageInfo.isRunning()) {
            if (pageInfo.isLatest()) {
                return getLatestRunningBuild(predicate);
            } else {
                return getRunningBuilds(pageInfo, predicate);
            }
        } else {
            if (pageInfo.isLatest()) {
                return getLatestBuild(predicate, dbPredicate);
            } else {
                return getBuilds(pageInfo, predicate, dbPredicate);
            }
        }
    }

    /**
     * Returns the page of Latest Running build filtered by given predicate.
     */
    private Page<Build> getLatestRunningBuild(java.util.function.Predicate<BuildTask> predicate) {
        List<Build> build = readLatestRunningBuild(predicate)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
        return new Page<>(0, 1, build.size(), build.size(), build);
    }

    /**
     * Returns the page of Running builds filtered by given BuildPageInfo and predicate.
     */
    private Page<Build> getRunningBuilds(BuildPageInfo pageInfo, java.util.function.Predicate<BuildTask> predicate) {
        List<Build> runningBuilds = readRunningBuilds(pageInfo, predicate);

        List<Build> builds = runningBuilds.stream()
                .skip(pageInfo.getPageIndex() * pageInfo.getPageSize())
                .limit(pageInfo.getPageSize())
                .collect(Collectors.toList());
        return new Page<>(
                pageInfo.getPageIndex(),
                pageInfo.getPageSize(),
                (int) Math.ceil((double) runningBuilds.size() / pageInfo.getPageSize()),
                runningBuilds.size(),
                builds);
    }

    /**
     * Returns the page of Latest build (running or finished) filtered by given predicate.
     */
    private Page<Build> getLatestBuild(java.util.function.Predicate<BuildTask> predicate, Predicate<BuildRecord> dbPredicate) {
        TreeSet<Build> sorted = new TreeSet<>(Comparator.comparing(Build::getSubmitTime).reversed());
        readLatestRunningBuild(predicate).ifPresent(sorted::add);
        readLatestFinishedBuild(dbPredicate).ifPresent(sorted::add);
        if(sorted.size() > 1){
            sorted.pollLast();
        }

        return new Page<>(0, 1, sorted.size(), sorted.size(), sorted);
    }

    /**
     * Returns the page of builds (running or finished) filtered by given BuildPageInfo and
     * predicate.
     */
    private Page<Build> getBuilds(BuildPageInfo pageInfo, java.util.function.Predicate<BuildTask> predicate, Predicate<BuildRecord> dbPredicate) {
        List<Build> runningBuilds = readRunningBuilds(pageInfo, predicate);

        int firstPossibleDBIndex = pageInfo.getPageIndex() * pageInfo.getPageSize() - runningBuilds.size();
        int lastPossibleDBIndex = (pageInfo.getPageIndex() + 1) * pageInfo.getPageSize() - 1;
        int toSkip = min(runningBuilds.size(), pageInfo.getPageIndex() * pageInfo.getPageSize());

        Predicate<BuildRecord>[] predicates = preparePredicates(dbPredicate, pageInfo.getQ());
        Comparator<Build> comparing = Comparator.comparing(Build::getSubmitTime).reversed();
        if (!StringUtils.isEmpty(pageInfo.getSort())) {
            comparing = rsqlPredicateProducer.getComparator(pageInfo.getSort());
        }

        SortInfo sortInfo = rsqlPredicateProducer.getSortInfo(type, pageInfo.getSort());
        MergeIterator<Build> builds = new MergeIterator(
                runningBuilds.iterator(),
                new BuildIterator(firstPossibleDBIndex, lastPossibleDBIndex, pageInfo.getPageSize(), sortInfo, predicates),
                comparing
        );
        List<Build> resultList = StreamSupport.stream(Spliterators.spliteratorUnknownSize(builds, Spliterator.ORDERED | Spliterator.SORTED), false)
                .skip(toSkip)
                .limit(pageInfo.getPageSize())
                .collect(Collectors.toList());

        int hits = repository.count(predicates) + runningBuilds.size();

        return new Page<>(
                pageInfo.getPageIndex(),
                pageInfo.getPageSize(),
                (int) Math.ceil((double) hits / pageInfo.getPageSize()),
                hits,
                resultList);
    }

    private Predicate<BuildRecord>[] preparePredicates(Predicate<BuildRecord> dbPredicate, String query) {
        Predicate<BuildRecord>[] predicates;
        if (StringUtils.isEmpty(query)){
            predicates = new Predicate[1];
        }else{
            predicates = new Predicate[2];
            predicates[1] = rsqlPredicateProducer.getCriteriaPredicate(BuildRecord.class, query);
        }
        predicates[0] = dbPredicate;
        return predicates;
    }

    private List<Build> readRunningBuilds(BuildPageInfo pageInfo, java.util.function.Predicate<BuildTask> predicate) {
        java.util.function.Predicate<Build> streamPredicate = (f) -> true;
        if (!StringUtils.isEmpty(pageInfo.getQ())) {
            streamPredicate = rsqlPredicateProducer.getStreamPredicate(pageInfo.getQ());
        }
        Comparator<Build> comparing = Comparator.comparing(Build::getSubmitTime).reversed();
        if (!StringUtils.isEmpty(pageInfo.getSort())) {
            comparing = rsqlPredicateProducer.getComparator(pageInfo.getSort());
        }
        return nullableStreamOf(buildCoordinator.getSubmittedBuildTasks())
                .filter(t -> t != null)
                .filter(predicate)
                .map(buildMapper::fromBuildTask)
                .filter(streamPredicate)
                .sorted(comparing)
                .collect(Collectors.toList());
    }

    private Optional<Build> readLatestRunningBuild(java.util.function.Predicate<BuildTask> predicate) {
        return nullableStreamOf(buildCoordinator.getSubmittedBuildTasks())
                .filter(t -> t != null)
                .filter(predicate)
                .sorted(Comparator.comparing(BuildTask::getSubmitTime).reversed())
                .findFirst()
                .map(buildMapper::fromBuildTask);
    }

    private Optional<Build> readLatestFinishedBuild(Predicate<BuildRecord> predicate) {
        PageInfo pageInfo = this.pageInfoProducer.getPageInfo(0, 1);
        SortInfo sortInfo = this.sortInfoProducer.getSortInfo(SortInfo.SortingDirection.DESC, "submitTime");
        List<BuildRecord> buildRecords = repository.queryWithPredicates(pageInfo, sortInfo, predicate);

        return buildRecords.stream().map(mapper::toDTO).findFirst();
    }

    class BuildIterator implements Iterator<Build> {

        private List<BuildRecord> builds;
        private Iterator<BuildRecord> it;
        private final int maxPageSize;
        private int firstIndex;
        private final int lastIndex;
        private final SortInfo sortInfo;
        private final Predicate<BuildRecord>[] predicates;

        public BuildIterator(int firstIndex, int lastIndex, int pageSize, SortInfo sortInfo, Predicate<BuildRecord>... predicate) {
            this.maxPageSize = pageSize > 10 ? pageSize : 10;
            this.firstIndex = firstIndex > 0 ? firstIndex : 0;
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
        public Build next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return mapper.toDTO(it.next());
        }

        private void nextPage() {
            int size = lastIndex - firstIndex + 1;
            if (size > maxPageSize) {
                size = maxPageSize;
            }
            PageInfo pageInfo = new DefaultPageInfo(firstIndex, size);
            builds = ((BuildRecordRepository) BuildProviderImpl.this.repository).queryWithPredicatesUsingCursor(pageInfo, sortInfo, predicates);
            it = builds.iterator();
            if (builds.size() < size) {
                firstIndex = lastIndex + 1;
            } else {
                firstIndex += size;
            }
        }
    }
}
