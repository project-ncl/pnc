/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.core.builder.coordinator.BuildCoordinator;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigSetId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withProjectId;

@Stateless
public class BuildRecordProvider extends AbstractProvider<BuildRecord, BuildRecordRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private BuildCoordinator buildCoordinator;
    private BuildExecutor buildExecutor;

    public BuildRecordProvider() {
    }

    @Inject
    public BuildRecordProvider(BuildRecordRepository buildRecordRepository, BuildCoordinator buildCoordinator,
            PageInfoProducer pageInfoProducer, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer,
            BuildExecutor buildExecutor) {
        super(buildRecordRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildCoordinator = buildCoordinator;
        this.buildExecutor = buildExecutor;
    }

    public CollectionInfo<BuildRecordRest> getAllRunning(Integer pageIndex, Integer pageSize, String search, String sort) {
        List<BuildTask> x = buildCoordinator.getActiveBuildTasks();
        return nullableStreamOf(x)
                .filter(rsqlPredicateProducer.getStreamPredicate(BuildTask.class, search))
                .sorted(sortInfoProducer.getSortInfo(sort).getComparator())
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .map(submittedBuild -> createNewBuildRecordRest(submittedBuild))
                .collect(new CollectionInfoCollector<>(pageIndex, pageSize,
                        (int) Math.ceil((double) buildCoordinator.getActiveBuildTasks().size() / pageSize)));
    }


    public CollectionInfo<BuildRecordRest> getAllRunningForBC(int pageIndex, int pageSize, String search, Integer bcId) {
        List<BuildTask> x = buildCoordinator.getActiveBuildTasks();
        return nullableStreamOf(x)
                .filter(t -> t != null)
                .filter(t -> t.getBuildConfigurationAudited() != null
                        && bcId.equals(t.getBuildConfigurationAudited().getId().getId()))
                .filter(task -> search == null
                        || "".equals(search)
                        || String.valueOf(task.getId()).contains(search)
                        || (task.getBuildConfigurationAudited() != null
                        && task.getBuildConfigurationAudited().getName() != null
                        && task.getBuildConfigurationAudited().getName().contains(search)))
                .sorted((t1, t2) -> t1.getId() - t2.getId())
                .map(submittedBuild -> createNewBuildRecordRest(submittedBuild))
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .collect(new CollectionInfoCollector<>(pageIndex, pageSize,
                        (int) Math.ceil((double) buildCoordinator.getActiveBuildTasks().size() / pageSize)));
    }

    private BuildRecordRest createNewBuildRecordRest(BuildTask submittedBuild) {
        BuildExecutionSession runningExecution = buildExecutor.getRunningExecution(submittedBuild.getId());
        UserRest user = new UserRest(submittedBuild.getUser());
        BuildRecordRest buildRecRest = null;
        if (runningExecution != null) {
            buildRecRest = new BuildRecordRest(runningExecution, submittedBuild.getSubmitTime(), user);
        } else {
            buildRecRest = new BuildRecordRest(
                    submittedBuild.getId(),
                    submittedBuild.getStatus(),
                    submittedBuild.getSubmitTime(),
                    submittedBuild.getStartTime(),
                    submittedBuild.getEndTime(),
                    new UserRest(submittedBuild.getUser()));
        }

        buildRecRest.setBuildConfigurationId(submittedBuild.getBuildConfiguration().getId());
        return buildRecRest;
    }

    public CollectionInfo<Object> getAllRunningForBCSetRecord(int pageIndex, int pageSize, String search, Integer bcSetRecordId) {
        return nullableStreamOf(buildCoordinator.getActiveBuildTasks())
                .filter(t -> t != null)
                .filter(t -> t.getBuildSetTask() != null
                        && bcSetRecordId.equals(t.getBuildSetTask().getId()))
                .filter(task -> search == null
                        || "".equals(search)
                        || String.valueOf(task.getId()).contains(search)
                        || (task.getBuildConfigurationAudited() != null
                        && task.getBuildConfigurationAudited().getName() != null
                        && task.getBuildConfigurationAudited().getName().contains(search)))
                .sorted((t1, t2) -> t1.getId() - t2.getId())
                .map(submittedBuild -> createNewBuildRecordRest(submittedBuild))
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .collect(new CollectionInfoCollector<>(pageIndex, pageSize,
                        (int) Math.ceil((double) buildCoordinator.getActiveBuildTasks().size() / pageSize)));
    }


    public CollectionInfo<BuildRecordRest> getAllForBuildConfiguration(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer configurationId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationId(configurationId));
    }

    public CollectionInfo<BuildRecordRest> getAllForProject(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer projectId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProjectId(projectId));
    }

    public CollectionInfo<BuildRecordRest> getAllForBuildConfigSetRecord(int pageIndex, int pageSize, String sortingRsql,
            String rsql, Integer buildConfigurationSetId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, rsql, withBuildConfigSetId(buildConfigurationSetId));
    }

    @Override
    protected Function<? super BuildRecord, ? extends BuildRecordRest> toRESTModel() {
        return buildRecord -> new BuildRecordRest(buildRecord);
    }

    @Override
    protected Function<? super BuildRecordRest, ? extends BuildRecord> toDBModel() {
        throw new UnsupportedOperationException("Not supported by BuildRecordProvider");
    }

    public String getBuildRecordLog(Integer id) {
        BuildRecord buildRecord = ((BuildRecordRepository)repository).findByIdFetchAllProperties(id);
        if(buildRecord != null)
            return buildRecord.getBuildLog();
        else
            return null;
    }

    public StreamingOutput getLogsForBuild(String buildRecordLog) {
        if(buildRecordLog == null)
            return null;

        return outputStream -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(buildRecordLog);
            writer.flush();
        };
    }

    public BuildRecordRest getSpecificRunning(Integer id) {
        if (id == null) {
            return null;
        }
        BuildTask buildTask = getSubmittedBuild(id);
        if (buildTask != null) {
            return createNewBuildRecordRest(buildTask);
        }
        return null;
    }

    private BuildTask getSubmittedBuild(Integer id) {
        return buildCoordinator.getActiveBuildTasks().stream()
                .filter(submittedBuild -> id.equals(submittedBuild.getId()))
                .findFirst().orElse(null);
    }

    public BuildConfigurationAuditedRest getBuildConfigurationAudited(Integer id) {
        BuildRecord buildRecord = repository.queryById(id);
        if (buildRecord == null) {
            return null;
        }
        if (buildRecord.getBuildConfigurationAudited() == null) {
            return null;
        }
        return new BuildConfigurationAuditedRest(buildRecord.getBuildConfigurationAudited());
    }

    public BuildRecordRest getLatestBuildRecord(Integer configId) {
        PageInfo pageInfo = this.pageInfoProducer.getPageInfo(0, 1);
        SortInfo sortInfo = this.sortInfoProducer.getSortInfo(SortInfo.SortingDirection.DESC, "endTime");
        List<BuildRecord> buildRecords = repository.queryWithPredicates(pageInfo, sortInfo, withBuildConfigurationId(configId));
        if (buildRecords.isEmpty()) {
            return null;
        }
        return toRESTModel().apply(buildRecords.get(0));
    }

    public CollectionInfo<BuildRecordRest> getRunningAndArchivedBuildRecords(Integer pageIndex, Integer pageSize, String search, String sort) {
        CollectionInfo<BuildRecordRest> buildRecords = getAll(pageIndex, pageSize, sort, search);
        CollectionInfo<BuildRecordRest> allRunning = getAllRunning(pageIndex, pageSize, search, sort);

        List<BuildRecordRest> allBuildRecords = new ArrayList<>();
        allBuildRecords.addAll(buildRecords.getContent());
        allBuildRecords.addAll(allRunning.getContent());

        allBuildRecords = allBuildRecords.stream()
                .filter(rsqlPredicateProducer.getStreamPredicate(BuildRecordRest.class, search))
                .sorted(sortInfoProducer.getSortInfo(sort).getComparator())
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        int totalPages = buildRecords.getTotalPages() + allRunning.getTotalPages();
        CollectionInfo<BuildRecordRest> allBuildRecordsWithMetadata = new CollectionInfo<>(pageIndex, pageSize, totalPages, allBuildRecords);

        return allBuildRecordsWithMetadata;
    }
}
