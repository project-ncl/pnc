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

import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.rest.restmodel.response.Page;
import org.jboss.pnc.rest.trigger.BuildConfigurationSetTriggerResult;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withArtifactDistributedInMilestone;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withAttribute;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigSetId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withProjectId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withUserId;

@Stateless
public class BuildRecordProvider extends AbstractProvider<BuildRecord, BuildRecordRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String QUERY_BY_USER = "user.id==%d";
    private static final String QUERY_BY_BUILD_CONFIGURATION_ID = "buildConfigurationAudited.idRev.id==%d";

    private BuildExecutor buildExecutor;
    private BuildCoordinator buildCoordinator;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    public BuildRecordProvider() {
    }

    @Inject
    public BuildRecordProvider(
            BuildRecordRepository buildRecordRepository,
            BuildCoordinator buildCoordinator,
            PageInfoProducer pageInfoProducer,
            RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer,
            BuildExecutor buildExecutor,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository) {
        super(buildRecordRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildCoordinator = buildCoordinator;
        this.buildExecutor = buildExecutor;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
    }

    public CollectionInfo<BuildRecordRest> getAllRunning(Integer pageIndex, Integer pageSize, String search, String sort) {
        List<BuildTask> x = buildCoordinator.getSubmittedBuildTasks();
        return nullableStreamOf(x)
                .filter(rsqlPredicateProducer.getStreamPredicate(BuildTask.class, search))
                .sorted(sortInfoProducer.getSortInfo(sort).getComparator())
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .map(submittedBuild -> createNewBuildRecordRest(submittedBuild))
                .collect(new CollectionInfoCollector<>(pageIndex, pageSize,
                        (int) Math.ceil((double) buildCoordinator.getSubmittedBuildTasks().size() / pageSize)));
    }

    public CollectionInfo<BuildRecordRest> getAllRunningForBuildConfiguration(int pageIndex, int pageSize, String search, String sort, Integer bcId) {
        List<BuildTask> x = buildCoordinator.getSubmittedBuildTasks();
        return nullableStreamOf(x)
                .filter(t -> t != null)
                .filter(t -> t.getBuildConfigurationAudited() != null
                        && bcId.equals(t.getBuildConfigurationAudited().getId().getId()))
                .filter(rsqlPredicateProducer.getStreamPredicate(BuildTask.class, search))
                .sorted(sortInfoProducer.getSortInfo(sort).getComparator())
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .map(submittedBuild -> createNewBuildRecordRest(submittedBuild))
                .collect(new CollectionInfoCollector<>(pageIndex, pageSize,
                        (int) Math.ceil((double) buildCoordinator.getSubmittedBuildTasks().size() / pageSize)));
    }

    public CollectionInfo<BuildRecordRest> getAllRunningOfUser(int pageIndex, int pageSize, String search, String sort, Integer userId) {
        List<BuildTask> x = buildCoordinator.getSubmittedBuildTasks();
        return nullableStreamOf(x)
                .filter(t -> t != null)
                .filter(t -> t.getUser() != null
                        && userId.equals(t.getUser().getId()))
                .filter(rsqlPredicateProducer.getStreamPredicate(BuildTask.class, search))
                .sorted(sortInfoProducer.getSortInfo(sort).getComparator())
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .map(submittedBuild -> createNewBuildRecordRest(submittedBuild))
                .collect(new CollectionInfoCollector<>(pageIndex, pageSize,
                        (int) Math.ceil((double) buildCoordinator.getSubmittedBuildTasks().size() / pageSize)));
    }

    private BuildRecordRest createNewBuildRecordRest(BuildTask buildTask) {
        //TODO do not mix executor and coordinator data in the same endpoint
        BuildExecutionSession runningExecution = buildExecutor.getRunningExecution(buildTask.getId());
        UserRest user = new UserRest(buildTask.getUser());
        //refresh entity
        IdRev idRev = buildTask.getBuildConfigurationAudited().getIdRev();
        logger.debug("Loading entity by idRev: {}.", idRev);
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.queryById(idRev);
        BuildConfigurationAuditedRest buildConfigAuditedRest = new BuildConfigurationAuditedRest(buildConfigurationAudited);

        BuildRecordRest buildRecRest;
        if (runningExecution != null) {
            buildRecRest = new BuildRecordRest(runningExecution, buildTask.getSubmitTime(), user, buildConfigAuditedRest);
        } else {
            buildRecRest = new BuildRecordRest(
                    buildTask.getId(),
                    buildTask.getStatus(),
                    buildTask.getSubmitTime(),
                    buildTask.getStartTime(),
                    buildTask.getEndTime(),
                    user, buildConfigAuditedRest);
        }

        buildRecRest.setBuildConfigurationId(buildTask.getBuildConfiguration().getId());
        return buildRecRest;
    }

    public CollectionInfo<Object> getAllRunningForBCSetRecord(int pageIndex, int pageSize, String search, Integer bcSetRecordId) {
        return nullableStreamOf(buildCoordinator.getSubmittedBuildTasks())
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
                        (int) Math.ceil((double) buildCoordinator.getSubmittedBuildTasks().size() / pageSize)));
    }


    public CollectionInfo<BuildRecordRest> getAllForBuildConfiguration(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer configurationId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationId(configurationId));
    }

    public CollectionInfo<BuildRecordRest> getAllOfUser(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer userId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withUserId(userId));
    }

    public CollectionInfo<BuildRecordRest> getAllForProject(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer projectId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProjectId(projectId));
    }

    public CollectionInfo<BuildRecordRest> getAllBuildRecordsWithArtifactsDistributedInProductMilestone(int pageIndex, int pageSize, String sortingRsql, String query, Integer milestoneId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withArtifactDistributedInMilestone(milestoneId));
    }

    /**
     * @deprecated Use getAllBuildRecordsWithArtifactsDistributedInProductMilestone
     */
    @Deprecated
    public Collection<Integer> getAllBuildsInDistributedRecordsetOfProductMilestone(Integer milestoneId) {
        return getAllBuildRecordsWithArtifactsDistributedInProductMilestone(0, 50, null, null, milestoneId).getContent()
                .stream().map(BuildRecordRest::getId).collect(Collectors.toList());
    }

    public CollectionInfo<BuildRecordRest> getAllForBuildConfigSetRecord(int pageIndex, int pageSize, String sortingRsql,
            String rsql, Integer buildConfigurationSetId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, rsql, withBuildConfigSetId(buildConfigurationSetId));
    }

    @Override
    protected Function<? super BuildRecord, ? extends BuildRecordRest> toRESTModel() {
        return BuildRecordRest::new;
    }

    @Override
    protected Function<? super BuildRecordRest, ? extends BuildRecord> toDBModel() {
        throw new UnsupportedOperationException("Not supported by BuildRecordProvider");
    }

    public String getBuildRecordLog(Integer id) {
        BuildRecord buildRecord = ((BuildRecordRepository) repository).findByIdFetchAllProperties(id);
        if (buildRecord != null)
            return buildRecord.getBuildLog();
        else
            return null;
    }

    public String getBuildRecordRepourLog(Integer id) {
        BuildRecord buildRecord = ((BuildRecordRepository) repository).findByIdFetchAllProperties(id);
        if (buildRecord != null) {
            return buildRecord.getRepourLog();
        } else {
            return null;
        }
    }

    public StreamingOutput getLogsForBuild(String buildRecordLog) {
        if (buildRecordLog == null)
            return null;

        return outputStream -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(buildRecordLog);
            writer.flush();
        };
    }

    public StreamingOutput getRepourLogsForBuild(String repourLog) {
        if (repourLog == null)
            return null;

        return outputStream -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(repourLog);
            writer.flush();
        };
    }

    public BuildRecordRest getSpecificRunning(Integer id) {
        if (id == null) {
            return null;
        }
        BuildTask buildTask = getSubmittedBuild(id);
        return getBuildRecordForTask(buildTask);
    }

    public BuildRecordRest getBuildRecordForTask(BuildTask task) {
        return task == null ? null : createNewBuildRecordRest(task);
    }

    private BuildTask getSubmittedBuild(Integer id) {
        return buildCoordinator.getSubmittedBuildTasks().stream()
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


    public CollectionInfo<BuildRecordRest> getRunningAndCompletedBuildRecords(Integer pageIndex, Integer pageSize, String sort, String search) {
        return getBuilds(pageIndex, pageSize, sort, search);
    }

    public CollectionInfo<BuildRecordRest> getRunningAndCompletedBuildRecordsByUserId(Integer pageIndex, Integer pageSize, String sort, String search, Integer userId) {
        return getBuilds(pageIndex, pageSize, sort, search, String.format(QUERY_BY_USER, userId));
    }

    public CollectionInfo<BuildRecordRest> getRunningAndCompletedBuildRecordsByBuildConfigurationId(Integer pageIndex, Integer pageSize, String sort, String search, Integer buildConfigurationId) {
        return getBuilds(pageIndex, pageSize, sort, search, String.format(QUERY_BY_BUILD_CONFIGURATION_ID, buildConfigurationId));
    }

    /*
     * Abstracts away the implementation detail that BuildRecords are not persisted to the database until the build is
     * complete. This abstraction allows clients to query for a list of all builds whether running or completed.
     */
    private CollectionInfo<BuildRecordRest> getBuilds(Integer pageIndex, Integer pageSize, String sort, String... rsqlQueries) {

        Predicate[] dbPredicates = Arrays.stream(rsqlQueries)
                .map(p -> rsqlPredicateProducer.getPredicate(BuildRecord.class, p))
                .toArray(Predicate[]::new);

        Set<BuildRecordRest> running = nullableStreamOf(buildCoordinator.getSubmittedBuildTasks())
                .map(this::createNewBuildRecordRest)
                .filter(rsqlPredicateProducer.getStreamPredicate(BuildRecordRest.class, combineRsqlQueriesMatchAll(rsqlQueries)))
                .sorted(sortInfoProducer.getSortInfo(sort).getComparator())
                .collect(Collectors.toSet());

        final int totalRunning = running.size();

        CollectionInfo<BuildRecordRest> page = null;

        for (int i = 0; i <= pageIndex; i++) {
            final int offset = totalRunning - running.size();

            if (offset == totalRunning) {
                page = createInterleavedPage(pageIndex, pageSize, offset, totalRunning, sort, running, dbPredicates);
                break;
            }

            page = createInterleavedPage(i, pageSize, offset, totalRunning, sort, running, dbPredicates);
            running.removeAll(page.getContent());
        }
        return page;
    }

    @SafeVarargs
    private final CollectionInfo<BuildRecordRest> createInterleavedPage(int pageIndex, int pageSize, int offset, int totalRunning,
            String sort, Set<BuildRecordRest> running, Predicate<BuildRecord>... dbPredicates) {

        PageInfo pageInfo = new DefaultPageInfo(pageIndex * pageSize - offset, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sort);

        List<BuildRecordRest> content = nullableStreamOf(((BuildRecordRepository) repository).queryWithPredicatesUsingCursor(pageInfo, sortInfo, dbPredicates))
                .map(toRESTModel())
                .collect(Collectors.toList());

        content.addAll(running);

        content = content.stream()
                .sorted(sortInfoProducer.getSortInfo(sort).getComparator())
                .limit(pageSize)
                .collect(Collectors.toList());

        int totalPages = calculateInterleavedPageCount(totalRunning, repository.count(dbPredicates), pageSize);

        return new CollectionInfo<>(pageIndex, pageSize, totalPages, content);
    }

    private String combineRsqlQueriesMatchAll(String... rsqlQueries) {
        return nullableStreamOf(Arrays.asList(rsqlQueries)).filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(";"));
    }

    private int calculateInterleavedPageCount(int totalRunningBuilds, int totalDbBuilds, int pageSize) {
        return (int) Math.ceil( (totalRunningBuilds + totalDbBuilds) / (double) pageSize );
    }


    public Map<String, String> putAttribute(Integer id, String name, String value) {
        BuildRecord buildRecord = repository.queryById(id);
        buildRecord.putAttribute(name, value);
        return buildRecord.getAttributes();
    }

    public void removeAttribute(Integer id, String name) {
        BuildRecord buildRecord = repository.queryById(id);
        buildRecord.removeAttribute(name);

    }

    public Map<String, String> getAttributes(Integer id) {
        BuildRecord buildRecord = repository.queryById(id);
        return buildRecord.getAttributes();
    }

    public Collection<BuildRecordRest> getByAttribute(String key, String value) {
        List<BuildRecord> buildRecords = repository.queryWithPredicates(withAttribute(key, value));
        return buildRecords.stream().map(BuildRecordRest::new).collect(Collectors.toList());
    }

    public SshCredentials getSshCredentialsForUser(Integer id, User currentUser) {
        BuildRecord buildRecord = repository.queryById(id);
        if (buildRecord != null && currentUser != null) {
            User buildRequester = buildRecord.getUser();
            if (buildRequester != null
                    && currentUser.getId().equals(buildRequester.getId())
                    && buildRecord.getSshCommand() != null) {
                return new SshCredentials(buildRecord.getSshCommand(), buildRecord.getSshPassword());
            }
        }
        return null;
    }

    public Response createResultSet(BuildConfigurationSetTriggerResult result, UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-config-set-records/{id}");
        URI uri = uriBuilder.build(result.getBuildRecordSetId());

        Page<BuildRecordRest> resultsToBeReturned = new Page<>(new CollectionInfo<>(0,
                result.getBuildTasks().size(),
                1,
                result.getBuildTasks().stream()
                        .map(this::getBuildRecordForTask)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())));

        return Response.ok(uri).header("location", uri).entity(resultsToBeReturned).build();
    }
}
