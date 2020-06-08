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

import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.criteria.AuditDisjunction;
import org.jboss.pnc.common.graph.GraphBuilder;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.graph.NameUniqueVertex;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.RunningBuildsCountRest;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.rest.restmodel.graph.GraphRest;
import org.jboss.pnc.rest.restmodel.response.Page;
import org.jboss.pnc.rest.trigger.BuildConfigurationSetTriggerResult;
import org.jboss.pnc.rest.utils.RestGraphBuilder;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.predicates.ProjectPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.GraphWithMetadata;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withArtifactDistributedInMilestone;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withAttribute;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigSetId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigSetRecordId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIdAndStatusExecuted;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIdRev;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withUserId;

@PermitAll
@Stateless
public class BuildRecordProvider extends AbstractProvider<BuildRecord, BuildRecordRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String QUERY_BY_USER = "user.id==%d";
    private static final String QUERY_BY_BUILD_CONFIGURATION_ID = "buildConfigurationId==%d";

    protected BuildRecordRepository repository;

    private BuildExecutor buildExecutor;
    private BuildCoordinator buildCoordinator;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    ProjectRepository projectRepository;
    BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    EntityManager entityManager;

    @Deprecated
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
            BuildRecordRepository repository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            ProjectRepository projectRepository,
            BuildConfigSetRecordRepository buildConfigSetRecordRepository,
            EntityManager entityManager) {
        super(buildRecordRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildCoordinator = buildCoordinator;
        this.buildExecutor = buildExecutor;
        this.repository = repository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.projectRepository = projectRepository;
        this.entityManager = entityManager;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
    }

    @RolesAllowed("system-user")
    public void update(Integer id, BuildRecordRest restEntity) throws RestValidationException {
        super.update(id, restEntity);
    }

    @RolesAllowed("system-user")
    public void delete(Integer id) throws RestValidationException {
        super.delete(id);
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
                        && bcId.equals(t.getBuildConfigurationAudited().getId()))
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

    public GraphRest<BuildRecordRest> getDependencyGraphRest(Integer buildId) {
        GraphWithMetadata<BuildRecordRest, Integer> buildRecordGraph = getDependencyGraph(buildId);

        Map<String, String> metadata = getGraphMetadata(buildRecordGraph.getMissingNodeIds());
        RestGraphBuilder restGraphBuilder = new RestGraphBuilder(metadata);
        return restGraphBuilder.from(buildRecordGraph.getGraph(), BuildRecordRest.class);
    }

    public RunningBuildsCountRest getRunningCount() {

        List<BuildTask> x = buildCoordinator.getSubmittedBuildTasks();

        int waitingForDependencies = 0;
        int running = 0;
        int enqueued = 0;

        for (BuildTask task: x) {
            switch(task.getStatus()) {
                case ENQUEUED:
                    enqueued++;
                    continue;
                case BUILDING:
                    running++;
                    continue;
                case WAITING_FOR_DEPENDENCIES:
                    waitingForDependencies++;
                    continue;
            }
        }

        return new RunningBuildsCountRest(running, enqueued, waitingForDependencies);

    }

    GraphWithMetadata<BuildRecordRest, Integer> getDependencyGraph(Integer buildId) {
        BuildTask buildTask = getSubmittedBuild(buildId);

        GraphWithMetadata<BuildRecordRest, Integer> buildRecordGraph;
        if (buildTask == null) {
            logger.debug("Looking for stored buildRecordId: {}.", buildId);
            BuildRecord buildRecord = repository.queryById(buildId);
            if (buildRecord == null) {
                logger.warn("Cannot find build {}", buildId);
                return null;
            } else {
                //current node is in the database
                logger.debug("Getting dependency graph for a stored build: {}.", buildId);
                GraphWithMetadata<BuildRecord, Integer> dependencyGraph = repository.getDependencyGraph(buildId);
                Graph<BuildRecordRest> buildRecordRestGraph = convertBuildRecordToRest(dependencyGraph.getGraph());

                List<Integer> missingNodeIds = dependencyGraph.getMissingNodeIds();
                logger.trace("Rest graph for buildRecordId {} {}; Graph edges {}. Missing nodes {}.",
                        buildId,
                        buildRecordRestGraph,
                        buildRecordRestGraph.getEdges(), missingNodeIds);

                //if there are missing nodes, they should be from the running builds
                if (missingNodeIds.isEmpty()) {
                    buildRecordGraph = new GraphWithMetadata<>(buildRecordRestGraph, new ArrayList<>());
                } else {
                    GraphBuilder graphBuilder = new GraphBuilder<BuildRecordRest>(
                            //get the current build from the graph as it is not in the running builds
                            id -> Optional.ofNullable(id == buildId ? buildRecordRestGraph.findVertexByName(id.toString()).getData() : getSpecificRunning(id)),
                            build -> Arrays.asList(build.getDependencyBuildRecordIds()),
                            build -> Arrays.asList(build.getDependentBuildRecordIds())
                    );

                    graphBuilder.buildDependentGraph(buildRecordRestGraph, buildId);

                    Set<Integer> verticies = buildRecordRestGraph.getVerticies()
                            .stream()
                            .map(v -> v.getData().getId())
                            .collect(Collectors.toSet());
                    List<Integer> stillMissing = missingNodeIds.stream()
                            .filter(missing -> !verticies.contains(missing)).collect(Collectors.toList());

                    buildRecordGraph = new GraphWithMetadata<>(buildRecordRestGraph, stillMissing);
                }
            }
        } else {
            logger.debug("Getting dependency graph for running build: {}.", buildId);

            Graph<BuildTask> graph = getBuiltTaskDependencyGraph(buildId);

            Graph<BuildRecordRest> buildRecordRestGraph = convertBuildTaskToRecordRest(graph);
            buildRecordGraph = new GraphWithMetadata<>(buildRecordRestGraph, new ArrayList<>());
        }
        return buildRecordGraph;
    }

    private Graph<BuildTask> getBuiltTaskDependencyGraph(Integer buildId) {
        Graph<BuildTask> graph = new Graph<>();
        GraphBuilder graphBuilder = new GraphBuilder<BuildTask>(
                id -> Optional.ofNullable(getSubmittedBuild(id)),
                bt -> bt.getDependencies().stream().map(BuildTask::getId).collect(Collectors.toList()),
                bt -> bt.getDependants().stream().map(BuildTask::getId).collect(Collectors.toList())
        );

        Vertex<BuildTask> current = graphBuilder.buildDependencyGraph(graph, buildId);
        if (current != null) {
            BuildTask currentTask = current.getData();
            graphBuilder.buildDependentGraph(graph, currentTask.getId());
        }
        return graph;
    }

    private Graph<BuildRecordRest> convertBuildTaskToRecordRest(Graph<BuildTask> taskGraph) {
        Graph<BuildRecordRest> buildRecordGraph = new Graph<>();

        for (Vertex<BuildTask> buildTaskVertex : taskGraph.getVerticies()) {
            BuildRecordRest recordRest = createBuildRecordForTask(buildTaskVertex.getData());
            Vertex<BuildRecordRest> buildRecordVertex = new NameUniqueVertex<>(Integer.toString(recordRest.getId()), recordRest);
            buildRecordGraph.addVertex(buildRecordVertex);
        }
        //create edges
        for (Vertex<BuildTask> vertex: taskGraph.getVerticies()) {
            for (Object o : vertex.getOutgoingEdges()) {
                Edge<BuildTask> edge = (Edge<BuildTask>) o;
                buildRecordGraph.addEdge(
                        buildRecordGraph.findVertexByName(edge.getFrom().getName()),
                        buildRecordGraph.findVertexByName(edge.getTo().getName()),
                        edge.getCost());
            }
        }
        return buildRecordGraph;
    }

    private Graph<BuildRecordRest> convertBuildRecordToRest(Graph<BuildRecord> recordGraph) {
        Graph<BuildRecordRest> buildRecordGraph = new Graph<>();

        for (Vertex<BuildRecord> buildRecordVertex : recordGraph.getVerticies()) {
            BuildRecordRest recordRest = new BuildRecordRest(buildRecordVertex.getData());
            Vertex<BuildRecordRest> buildRecordRestVertex = new NameUniqueVertex<>(Integer.toString(recordRest.getId()), recordRest);
            buildRecordGraph.addVertex(buildRecordRestVertex);
        }
        //create edges
        for (Edge<BuildRecord> edge : recordGraph.getEdges()) {
            buildRecordGraph.addEdge(
                    buildRecordGraph.findVertexByName(edge.getFrom().getName()),
                    buildRecordGraph.findVertexByName(edge.getTo().getName()),
                    edge.getCost());
        }
        return buildRecordGraph;
    }

    BuildRecordRest createNewBuildRecordRest(BuildTask buildTask, Map<IdRev, BuildConfigurationAudited> buildConfigurationsAuditedMap) {
        // TODO do not mix executor and coordinator data in the same endpoint
        BuildExecutionSession runningExecution = buildExecutor.getRunningExecution(buildTask.getId());
        UserRest user = new UserRest(buildTask.getUser());
        // refresh entity
        IdRev idRev = buildTask.getBuildConfigurationAudited().getIdRev();
        logger.debug("Loading entity by idRev: {}.", idRev);
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationsAuditedMap.get(idRev);
        BuildConfigurationAuditedRest buildConfigAuditedRest = new BuildConfigurationAuditedRest(buildConfigurationAudited);

        Integer[] dependencyIds = buildTask.getDependencies().stream().map(BuildTask::getId).toArray(Integer[]::new);
        Integer[] dependentIds = buildTask.getDependants().stream().map(BuildTask::getId).toArray(Integer[]::new);

        BuildRecordRest buildRecRest;
        if (runningExecution != null) {
            buildRecRest = new BuildRecordRest(runningExecution, buildTask.getSubmitTime(), user, buildConfigAuditedRest,
                    dependencyIds, dependentIds);
        } else {
            buildRecRest = new BuildRecordRest(buildTask.getId(), buildTask.getStatus(), buildTask.getSubmitTime(),
                    buildTask.getStartTime(), buildTask.getEndTime(), user, buildConfigAuditedRest,
                    buildTask.getBuildOptions().isTemporaryBuild(), dependencyIds, dependentIds);
        }
        return buildRecRest;
    }

    BuildRecordRest createNewBuildRecordRest(BuildTask buildTask) {
        //TODO do not mix executor and coordinator data in the same endpoint
        BuildExecutionSession runningExecution = buildExecutor.getRunningExecution(buildTask.getId());
        UserRest user = new UserRest(buildTask.getUser());
        //refresh entity
        IdRev idRev = buildTask.getBuildConfigurationAudited().getIdRev();
        logger.debug("Loading entity by idRev: {}.", idRev);
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.queryById(idRev);
        BuildConfigurationAuditedRest buildConfigAuditedRest = new BuildConfigurationAuditedRest(buildConfigurationAudited);

        Integer[] dependencyIds = buildTask.getDependencies().stream().map(BuildTask::getId).toArray(Integer[]::new);
        Integer[] dependentIds = buildTask.getDependants().stream().map(BuildTask::getId).toArray(Integer[]::new);

        BuildRecordRest buildRecRest;
        if (runningExecution != null) {
            buildRecRest = new BuildRecordRest(
                    runningExecution,
                    buildTask.getSubmitTime(),
                    user,
                    buildConfigAuditedRest,
                    dependencyIds,
                    dependentIds);
        } else {
            buildRecRest = new BuildRecordRest(
                    buildTask.getId(),
                    buildTask.getStatus(),
                    buildTask.getSubmitTime(),
                    buildTask.getStartTime(),
                    buildTask.getEndTime(),
                    user,
                    buildConfigAuditedRest,
                    buildTask.getBuildOptions().isTemporaryBuild(),
                    dependencyIds,
                    dependentIds);
        }
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

    public GraphRest<BuildRecordRest> getBCSetRecordRestGraph(Integer bcSetRecordId) {
        Graph<BuildTask> runningBuildGraph = getRunningBCSetRecordGraph(bcSetRecordId);
        Graph<BuildRecordRest> runningBuildRecordGraph = convertBuildTaskToRecordRest(runningBuildGraph);

        GraphWithMetadata<BuildRecordRest, Integer> buildConfigSetRecordGraph = getBuildConfigSetRecordGraph(bcSetRecordId);
        Graph graph = buildConfigSetRecordGraph.getGraph();

        GraphUtils.merge(graph, runningBuildRecordGraph);

        Map<String, String> metadata = getGraphMetadata(buildConfigSetRecordGraph.getMissingNodeIds());
        RestGraphBuilder graphBuilder = new RestGraphBuilder(metadata);

        GraphRest<BuildRecordRest> graphRest = graphBuilder.from(graph, BuildRecordRest.class);
        return graphRest;
    }

    private Map<String, String> getGraphMetadata(List<Integer> missingBuildRecordIds) {
        Map<String, String> metadata = new HashMap<>();
        if (missingBuildRecordIds.size() > 0) {
            metadata.put("status", "INCOMPLETE");
            for (Integer buildRecordId : missingBuildRecordIds) {
                metadata.put("description", "Missing some Build Records: " + buildRecordId);
            }
        }
        return metadata;
    }

    Graph<BuildTask> getRunningBCSetRecordGraph(Integer bcSetRecordId) {
        //get all build tasks that are in the group
        List<BuildTask> buildTasks = nullableStreamOf(buildCoordinator.getSubmittedBuildTasks())
                .filter(t -> t != null)
                .filter(t -> t.getBuildSetTask() != null
                        && bcSetRecordId.equals(t.getBuildSetTask().getId()))
                .sorted((t1, t2) -> t1.getId() - t2.getId())
                .collect(Collectors.toList());

        Graph<BuildTask> buildGraph = new Graph<>();
        for (BuildTask buildTask : buildTasks) {
            //Adds buildTask and related tasks (dependencies and dependents) to the graph if they don't already exists
            Graph<BuildTask> dependencyGraph = getBuiltTaskDependencyGraph(buildTask.getId());
            GraphUtils.merge(buildGraph, dependencyGraph);
        }
        return buildGraph;
    }

    private GraphWithMetadata<BuildRecordRest, Integer> getBuildConfigSetRecordGraph(Integer bcSetRecordId) {
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.queryById(bcSetRecordId);
        Graph<BuildRecordRest> buildGraph = new Graph<>();
        List<Integer> missingBuildRecordId = new ArrayList<>();
        for (BuildRecord buildRecord : buildConfigSetRecord.getBuildRecords()) {
            GraphWithMetadata<BuildRecordRest, Integer> dependencyGraph = getDependencyGraph(buildRecord.getId());
            GraphUtils.merge(buildGraph, dependencyGraph.getGraph());
            logger.trace("Merged graph from buildRecordId {} to BuildConfigSetRecordGraph {}; Edges {},", buildRecord.getId(), buildGraph, buildGraph.getEdges());
            missingBuildRecordId.addAll(dependencyGraph.getMissingNodeIds());
        }
        return new GraphWithMetadata<>(buildGraph, missingBuildRecordId);
    }

    public CollectionInfo<BuildRecordRest> getAllForBuildConfiguration(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer configurationId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationId(configurationId));
    }

    public CollectionInfo<BuildRecordRest> getAllOfUser(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer userId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withUserId(userId));
    }

    public CollectionInfo<BuildRecordRest> getAllForProject(int pageIndex, int pageSize, String sortingRsql, String query, Integer projectId) {
        List<Object[]> buildConfigurationRevisions = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .add(AuditEntity.relatedId("project").eq(projectId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return queryForBuildRecords(pageIndex, pageSize, sortingRsql, query, buildConfigurationRevisions);
    }

    public CollectionInfo<BuildRecordRest> getAllForConfigurationOrProjectName(int pageIndex, int pageSize, String sortingRsql, String query, String name) {

        List<Project> projectsMatchingName = projectRepository.queryWithPredicates(ProjectPredicates.searchByProjectName(name));

        AuditDisjunction disjunction = AuditEntity.disjunction();
        projectsMatchingName.forEach(project -> {
                disjunction.add(AuditEntity.relatedId("project").eq(project.getId()));
        });
        disjunction.add(AuditEntity.property("name").like(name));

        List<Object[]> buildConfigurationRevisions = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .add(disjunction)
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return queryForBuildRecords(pageIndex, pageSize, sortingRsql, query, buildConfigurationRevisions);
    }

    private CollectionInfo<BuildRecordRest> queryForBuildRecords(int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            List<Object[]> buildConfigurationRevisions) {
        List<IdRev> buildConfigurationsWithProjectIdRevs = buildConfigurationRevisions.stream()
                .map(o -> toIdRev(o[0], o[1]))
                .collect(Collectors.toList());

        if (buildConfigurationsWithProjectIdRevs.isEmpty()) {
            return new CollectionInfo<>(0, 0, 0, Collections.EMPTY_SET);
        } else {
            return queryForCollection(
                    pageIndex,
                    pageSize,
                    sortingRsql,
                    query,
                    withBuildConfigurationIdRev(buildConfigurationsWithProjectIdRevs));
        }
    }

    private IdRev toIdRev(Object entity, Object revision) {
        BuildConfiguration buildConfiguration = (BuildConfiguration) entity;
        DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) revision;
        return new IdRev(buildConfiguration.getId(), revisionEntity.getId());
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
            String rsql, Integer buildConfigurationSetRecordId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, rsql, withBuildConfigSetRecordId(buildConfigurationSetRecordId));
    }

    public CollectionInfo<BuildRecordRest> getAllForBuildConfigSet(int pageIndex, int pageSize, String sortingRsql,
            String rsql, Integer buildConfigurationSetId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, rsql, withBuildConfigSetId(buildConfigurationSetId));
    }

    @Override
    protected Function<? super BuildRecord, ? extends BuildRecordRest> toRESTModel() {
        return (buildRecord) -> {
            Integer revision = buildRecord.getBuildConfigurationRev();

            BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                    .queryById(new IdRev(buildRecord.getBuildConfigurationId(), revision));

            buildRecord.setBuildConfigurationAudited(buildConfigurationAudited);
            return new BuildRecordRest(buildRecord);
        };
    }

    protected Function<? super BuildRecord, ? extends BuildRecordRest> toRESTModel(Map<IdRev, BuildConfigurationAudited> buildConfigurationsAuditedMap) {
        return (buildRecord) -> {
            BuildConfigurationAudited buildConfigurationAudited = buildConfigurationsAuditedMap
                    .get(new IdRev(buildRecord.getBuildConfigurationId(), buildRecord.getBuildConfigurationRev()));

            buildRecord.setBuildConfigurationAudited(buildConfigurationAudited);
            return new BuildRecordRest(buildRecord);
        };
    }

    private void preloadBuildConfigurationRelations(BuildConfiguration buildConfiguration) {
        buildConfiguration.getGenericParameters().forEach((k,v) -> k.equals(null));
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
        return createBuildRecordForTask(buildTask);
    }

    private BuildRecordRest createBuildRecordForTask(BuildTask task) {
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
        if (buildRecord.getBuildConfigurationAudited() != null) {
            return new BuildConfigurationAuditedRest(buildRecord.getBuildConfigurationAudited());
        } else {
            BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                    .queryById(new IdRev(buildRecord.getBuildConfigurationId(), buildRecord.getBuildConfigurationRev()));
            return new BuildConfigurationAuditedRest(buildConfigurationAudited);
        }
    }

    public BuildRecordRest getLatestBuildRecord(Integer configId, boolean executedOnly) {
        PageInfo pageInfo = this.pageInfoProducer.getPageInfo(0, 1);
        SortInfo sortInfo = this.sortInfoProducer.getSortInfo(SortInfo.SortingDirection.DESC, "endTime");
        List<BuildRecord> buildRecords;
        if (executedOnly) {
            buildRecords = repository.queryWithPredicates(pageInfo, sortInfo, withBuildConfigurationIdAndStatusExecuted(configId));
        } else {
            buildRecords = repository.queryWithPredicates(pageInfo, sortInfo, withBuildConfigurationId(configId));
        }

        if (buildRecords.isEmpty()) {
            return null;
        }
        return toRESTModel().apply(buildRecords.get(0));
    }

    public CollectionInfo<BuildRecordRest> getRunningAndCompletedBuildRecords(
            Integer pageIndex,
            Integer pageSize,
            String sort,
            String orFindByBuildConfigurationName,
            String andFindByBuildConfigurationName,
            String search) {

         return getBuilds(pageIndex, pageSize, sort, orFindByBuildConfigurationName, andFindByBuildConfigurationName, search);
    }

    public CollectionInfo<BuildRecordRest> getRunningAndCompletedBuildRecordsByUserId(Integer pageIndex, Integer pageSize, String sort, String search, Integer userId) {
        return getBuilds(pageIndex, pageSize, sort, null, null, search, String.format(QUERY_BY_USER, userId));
    }

    public CollectionInfo<BuildRecordRest> getRunningAndCompletedBuildRecordsByBuildConfigurationId(Integer pageIndex, Integer pageSize, String sort, String search, Integer buildConfigurationId) {
        return getBuilds(pageIndex, pageSize, sort, null, null, search, String.format(QUERY_BY_BUILD_CONFIGURATION_ID, buildConfigurationId));
    }

    /*
     * Abstracts away the implementation detail that BuildRecords are not persisted to the database until the build is
     * complete. This abstraction allows clients to query for a list of all builds whether running or completed.
     */
    private CollectionInfo<BuildRecordRest> getBuilds(Integer pageIndex, Integer pageSize, String sort, String orFindByBuildConfigurationName, String andFindByBuildConfigurationName, String... rsqlQueries) {

        logger.debug(
                "Get running and completed build records by pageIndex: {}, pageSize: {}, sort: {}, orFindByBuildConfigurationName: {}, andFindByBuildConfigurationName: {}, rsqlQueries: {}",
                pageIndex, pageSize, sort, orFindByBuildConfigurationName, andFindByBuildConfigurationName, rsqlQueries);

        // Combine predicates of both search query and the additional filters (e.g.userId, buildConfigurationId)
        List<Predicate<BuildRecord>> dbAndPredicatesList = Arrays.stream(rsqlQueries)
                .filter(rsqlString -> rsqlString != null && !rsqlString.isEmpty())
                .map(p -> rsqlPredicateProducer.getPredicate(BuildRecord.class, p))
                .collect(Collectors.toList());
        List<Predicate<BuildRecord>> dbOrPredicateList = new ArrayList<>();

        // Combine rsqlQueries AND-ing all the single queries
        String combinedQueries = combineRsqlQueriesMatchAll(rsqlQueries);

        // An AND condition (andFindByBuildConfigurationName) gets higher precedence over an OR condition
        // (orFindByBuildConfigurationName). If specified both, only AND is applied.

        if (!StringUtils.isEmpty(andFindByBuildConfigurationName)) {
            // add steam condition
            if (StringUtils.isEmpty(combinedQueries)) {
                combinedQueries = "(buildConfigurationName==" + andFindByBuildConfigurationName + ")";
            } else {
                combinedQueries = "(" + combinedQueries + ");(buildConfigurationName==" + andFindByBuildConfigurationName + ")";
            }

            // add DB predicate
            List<IdRev> buildConfigurationAuditedIdRevs = buildConfigurationAuditedRepository
                    .searchIdRevForBuildConfigurationName(andFindByBuildConfigurationName);
            if (!buildConfigurationAuditedIdRevs.isEmpty()) {
                dbAndPredicatesList.add(BuildRecordPredicates.withBuildConfigurationIdRev(buildConfigurationAuditedIdRevs));
            } else {
                dbAndPredicatesList.add(Predicate.nonMatching());
            }

        } else if (!StringUtils.isEmpty(orFindByBuildConfigurationName)) {
            // add steam condition
            if (StringUtils.isEmpty(combinedQueries)) {
                combinedQueries = "(buildConfigurationName=like=" + orFindByBuildConfigurationName + ")";
            } else {
                combinedQueries = "(" + combinedQueries + "),(buildConfigurationName=like=" + orFindByBuildConfigurationName + ")";
            }

            // add DB predicate
            List<IdRev> buildConfigurationAuditedIdRevs = buildConfigurationAuditedRepository
                    .searchIdRevForBuildConfigurationName(orFindByBuildConfigurationName);
            if (!buildConfigurationAuditedIdRevs.isEmpty()) {
                dbOrPredicateList.add(BuildRecordPredicates.withBuildConfigurationIdRev(buildConfigurationAuditedIdRevs));
            }
        }

        List<BuildTask> buildTasks = buildCoordinator.getSubmittedBuildTasks();
        Set<IdRev> buildConfigAuditedIdRevsOfBuildTasks = nullableStreamOf(buildTasks).map(buildTask -> buildTask.getBuildConfigurationAudited().getIdRev()).collect(Collectors.toSet());

        Set<BuildRecordRest> running = new HashSet<BuildRecordRest>();
        if (!buildConfigAuditedIdRevsOfBuildTasks.isEmpty()) {

            Map<IdRev, BuildConfigurationAudited> buildConfigurationsAuditedMap = buildConfigurationAuditedRepository.queryById(buildConfigAuditedIdRevsOfBuildTasks);
            running = nullableStreamOf(buildTasks)
                    .map(buildTask -> createNewBuildRecordRest(buildTask, buildConfigurationsAuditedMap))
                    .filter(rsqlPredicateProducer.getStreamPredicate(BuildRecordRest.class, combinedQueries))
                    .sorted(sortInfoProducer.getSortInfo(sort).getComparator())
                    .collect(Collectors.toSet());
        }

        final int totalRunning = running.size();

        CollectionInfo<BuildRecordRest> page = null;
        for (int i = 0; i <= pageIndex; i++) {
            final int offset = totalRunning - running.size();

            if (offset == totalRunning) {
                page = createInterleavedPage(pageIndex, pageSize, offset, totalRunning, sort, running, dbAndPredicatesList, dbOrPredicateList);
                break;
            }

            page = createInterleavedPage(i, pageSize, offset, totalRunning, sort, running, dbAndPredicatesList, dbOrPredicateList);
            running.removeAll(page.getContent());
        }
        return page;
    }

    private final CollectionInfo<BuildRecordRest> createInterleavedPage(int pageIndex, int pageSize, int offset, int totalRunning,
            String sort, Set<BuildRecordRest> running, List<Predicate<BuildRecord>> dbAndPredicates,
            List<Predicate<BuildRecord>> dbOrPredicates) {

        PageInfo pageInfo = new DefaultPageInfo(pageIndex * pageSize - offset, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sort);

        List<BuildRecord> buildRecords = nullableStreamOf(((BuildRecordRepository) repository)
                .queryWithPredicatesUsingCursor(pageInfo, sortInfo, dbAndPredicates, dbOrPredicates))
                .collect(Collectors.toList());
        Set<IdRev> buildRecordsIdRevs = nullableStreamOf(buildRecords).map(br -> new IdRev(br.getBuildConfigurationId(), br.getBuildConfigurationRev())).collect(Collectors.toSet());

        List<BuildRecordRest> content = new ArrayList<>();
        if (!buildRecordsIdRevs.isEmpty()) {
            Map<IdRev, BuildConfigurationAudited> buildConfigurationsAuditedMap = buildConfigurationAuditedRepository.queryById(buildRecordsIdRevs);
            content = nullableStreamOf(buildRecords).map(toRESTModel(buildConfigurationsAuditedMap))
                    .collect(Collectors.toList());
        }

        content.addAll(running);

        content = content.stream()
                .sorted(sortInfoProducer.getSortInfo(sort).getComparator())
                .limit(pageSize)
                .collect(Collectors.toList());
        int totalPages = calculateInterleavedPageCount(totalRunning, repository.count(dbAndPredicates, dbOrPredicates), pageSize);

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

    public CollectionInfo<BuildRecordRest> getByAttribute(int pageIndex, int pageSize, String sortingRsql,
            String rsql, String key, String value) {
        return queryForCollection(pageIndex,pageSize,sortingRsql,rsql,withAttribute(key, value));
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
                        .map(this::createBuildRecordForTask)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())));

        return Response.ok(uri).header("location", uri).entity(resultsToBeReturned).build();
    }

    @RolesAllowed("system-user")
    public void setBuiltArtifacts(Integer buildRecordId, Collection<Integer> artifactIds) {
        BuildRecord buildRecord = repository.queryById(buildRecordId);
        Set<Artifact> artifacts = artifactIds.stream()
                .map(id -> Artifact.Builder.newBuilder().id(id).build())
                .collect(Collectors.toSet());
        buildRecord.setBuiltArtifacts(artifacts);
        repository.save(buildRecord);
    }

    @RolesAllowed("system-user")
    public void setDependentArtifacts(Integer buildRecordId, Collection<Integer> artifactIds) {
        BuildRecord buildRecord = repository.queryById(buildRecordId);
        Set<Artifact> artifacts = artifactIds.stream()
                .map(id -> Artifact.Builder.newBuilder().id(id).build())
                .collect(Collectors.toSet());
        buildRecord.setDependencies(artifacts);
        repository.save(buildRecord);
    }

    public CollectionInfo<BuildRecordRest> getAllByStatusAndLogContaining(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            BuildStatus status,
            String search) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, BuildRecordPredicates.withStatus(status),
                BuildRecordPredicates.withBuildLogContains(search));
    }

    public CollectionInfo<BuildRecordRest> getAllTemporaryOlderThanTimestamp(int pageIndex,
                                                  int pageSize,
                                                  String sort,
                                                  String q,
                                                  long timestamp) {
        return queryForCollection(pageIndex, pageSize, sort, q, BuildRecordPredicates.temporaryBuild(),
                BuildRecordPredicates.buildFinishedBefore(new Date(timestamp)),
                BuildRecordPredicates.withoutImplicitDepencenciesOlderThanTimestamp(new Date(timestamp)));
    }
}
