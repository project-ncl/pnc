/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jboss.pnc.common.gerrit.Gerrit;
import org.jboss.pnc.common.gerrit.GerritException;
import org.jboss.pnc.common.graph.GraphBuilder;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.graph.NameUniqueVertex;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RunningBuildCount;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.util.GraphDtoBuilder;
import org.jboss.pnc.facade.util.MergeIterator;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.ResultMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.RunningBuildsCountRest;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.GraphWithMetadata;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.exception.ValidationException;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.Math.min;
import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withIds;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProjectId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.buildFinishedBefore;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.temporaryBuild;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withArtifactDependency;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withArtifactProduced;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withAttribute;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigSetId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigSetRecordId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIds;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withPerformedInMilestone;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withUserId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withoutAttribute;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withoutImplicitDependants;

@PermitAll
@Stateless
public class BuildProviderImpl extends AbstractProvider<Integer, BuildRecord, Build, BuildRef>
        implements BuildProvider {

    private static final Logger logger = LoggerFactory.getLogger(BuildProviderImpl.class);

    private ArtifactRepository artifactRepository;
    private BuildRecordRepository buildRecordRepository;
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    private Gerrit gerrit;
    private BuildConfigurationRevisionMapper buildConfigurationRevisionMapper;
    private BuildMapper buildMapper;

    private BuildCoordinator buildCoordinator;
    private SortInfoProducer sortInfoProducer;
    private UserService userService;

    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;
    private ResultMapper resultMapper;

    @Inject
    public BuildProviderImpl(
            ArtifactRepository artifactRepository,
            BuildRecordRepository repository,
            BuildMapper mapper,
            BuildConfigurationRepository buildConfigurationRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            BuildConfigSetRecordRepository buildConfigSetRecordRepository,
            Gerrit gerrit,
            BuildConfigurationRevisionMapper buildConfigurationRevisionMapper,
            BuildCoordinator buildCoordinator,
            SortInfoProducer sortInfoProducer,
            UserService userService,
            TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker,
            ResultMapper resultMapper) {
        super(repository, mapper, BuildRecord.class);

        this.artifactRepository = artifactRepository;
        this.buildRecordRepository = repository;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.gerrit = gerrit;
        this.buildConfigurationRevisionMapper = buildConfigurationRevisionMapper;
        this.buildMapper = mapper;
        this.buildCoordinator = buildCoordinator;
        this.sortInfoProducer = sortInfoProducer;
        this.userService = userService;
        this.temporaryBuildsCleanerAsyncInvoker = temporaryBuildsCleanerAsyncInvoker;
        this.resultMapper = resultMapper;
    }

    @Override
    public Build store(Build restEntity) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct build creation is not available.");
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting persistent builds is never allowed");
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public Build update(String id, Build restEntity) {
        return super.update(id, restEntity);
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public boolean delete(String buildId, String callback) {
        User user = userService.currentUser();

        if (user == null) {
            throw new RuntimeException("Failed to load user metadata.");
        }

        try {
            return temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuild(
                    BuildMapper.idMapper.toEntity(buildId),
                    user.getLoginToken(),
                    notifyOnBuildDeletionCompletion(callback));
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }
    }

    private Consumer<Result> notifyOnBuildDeletionCompletion(String callback) {
        return (result) -> {
            if (callback != null && !callback.isEmpty()) {
                try {
                    HttpUtils.performHttpPostRequest(callback, resultMapper.toDTO(result));
                } catch (JsonProcessingException e) {
                    logger.error("Failed to perform a callback of delete operation.", e);
                }
            }
        };
    }

    @Override
    public Page<Build> getAllIndependentTemporaryOlderThanTimestamp(
            int pageIndex,
            int pageSize,
            String sort,
            String q,
            long timestamp) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sort,
                q,
                temporaryBuild(),
                buildFinishedBefore(new Date(timestamp)),
                withoutImplicitDependants());
    }

    @Override
    public void addAttribute(String buildId, String key, String value) {
        BuildRecord buildRecord = getBuildRecord(buildId);
        if (null == key) {
            throw new IllegalArgumentException("Attribute key must not be null");
        }
        if (!key.matches("[a-zA-Z_0-9]+")) {
            throw new IllegalArgumentException("Attribute key must match [a-zA-Z_0-9]+");
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
    public void removeAttribute(String buildId, String key) {
        BuildRecord buildRecord = getBuildRecord(buildId);
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
    public BuildConfigurationRevision getBuildConfigurationRevision(String buildId) {

        BuildRecord buildRecord = getBuildRecord(buildId);

        if (buildRecord.getBuildConfigurationAudited() != null) {
            return buildConfigurationRevisionMapper.toDTO(buildRecord.getBuildConfigurationAudited());
        } else {
            BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.queryById(
                    new IdRev(buildRecord.getBuildConfigurationId(), buildRecord.getBuildConfigurationRev()));

            return buildConfigurationRevisionMapper.toDTO(buildConfigurationAudited);
        }
    }

    @Override
    public String getRepourLog(String buildId) {
        return getBuildRecord(buildId).getRepourLog();
    }

    @Override
    public String getBuildLog(String buildId) {
        return getBuildRecord(buildId).getBuildLog();
    }

    @Override
    public SSHCredentials getSshCredentials(String buildId) {
        BuildRecord buildRecord = getBuildRecord(buildId);
        User user = null;
        try {
            user = userService.currentUser();
        } catch (IllegalStateException e) {
            // leaves user null
        }
        if (!buildRecord.getUser().equals(user)) {
            throw new EJBAccessException("Only user who executed the build is allowed to get the SSH credentials");
        }

        return SSHCredentials.builder()
                .command(buildRecord.getSshCommand())
                .password(buildRecord.getSshPassword())
                .build();
    }

    @Override
    public Page<Build> getAll(int pageIndex, int pageSize, String sort, String query) {
        BuildPageInfo pageInfo = new BuildPageInfo(pageIndex, pageSize, sort, query, false, false, "");
        return getBuilds(pageInfo);
    }

    @Override
    public Page<Build> getBuilds(BuildPageInfo pageInfo) {
        return getBuildList(pageInfo, _t -> true, (_r, _q, cb) -> cb.conjunction());
    }

    @Override
    public Page<Build> getBuildsForMilestone(BuildPageInfo pageInfo, String milestoneId) {
        java.util.function.Predicate<BuildTask> predicate = t -> t.getProductMilestone() != null
                && Integer.valueOf(milestoneId).equals(t.getProductMilestone().getId());
        return getBuildList(pageInfo, predicate, withPerformedInMilestone(Integer.valueOf(milestoneId)));
    }

    @Override
    public Page<Build> getBuildsForProject(BuildPageInfo pageInfo, String projectId) {
        @SuppressWarnings("unchecked")
        Set<Integer> buildConfigIds = buildConfigurationRepository
                .queryWithPredicates(withProjectId(Integer.valueOf(projectId)))
                .stream()
                .map(BuildConfiguration::getId)
                .collect(Collectors.toSet());

        java.util.function.Predicate<BuildTask> predicate = t -> Integer.valueOf(projectId)
                .equals(t.getBuildConfigurationAudited().getProject().getId());
        return getBuildList(pageInfo, predicate, withBuildConfigurationIds(buildConfigIds));
    }

    @Override
    public Page<Build> getBuildsForArtifact(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String artifactId) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withArtifactProduced(Integer.valueOf(artifactId)));
    }

    @Override
    public Page<Build> getDependantBuildsForArtifact(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String artifactId) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withArtifactDependency(Integer.valueOf(artifactId)));
    }

    @Override
    public Page<Build> getBuildsForBuildConfiguration(BuildPageInfo pageInfo, String buildConfigurationId) {
        java.util.function.Predicate<BuildTask> predicate = t -> Integer.valueOf(buildConfigurationId)
                .equals(t.getBuildConfigurationAudited().getId());
        return getBuildList(pageInfo, predicate, withBuildConfigurationId(Integer.valueOf(buildConfigurationId)));
    }

    @Override
    public Page<Build> getBuildsForUser(BuildPageInfo pageInfo, String userId) {
        java.util.function.Predicate<BuildTask> predicate = t -> Integer.valueOf(userId).equals(t.getUser().getId());
        return getBuildList(pageInfo, predicate, withUserId(Integer.valueOf(userId)));
    }

    @Override
    public Page<Build> getBuildsForGroupConfiguration(BuildPageInfo pageInfo, String groupConfigurationId) {
        java.util.function.Predicate<BuildTask> predicate = t -> t.getBuildSetTask() != null && t.getBuildSetTask()
                .getBuildConfigSetRecord()
                .map(gc -> Integer.valueOf(groupConfigurationId).equals(gc.getBuildConfigurationSet().getId()))
                .orElse(false);
        return getBuildList(pageInfo, predicate, withBuildConfigSetId(Integer.valueOf(groupConfigurationId)));
    }

    @Override
    public Page<Build> getBuildsForGroupBuild(BuildPageInfo pageInfo, String groupBuildId) {
        java.util.function.Predicate<BuildTask> predicate = t -> t.getBuildSetTask() != null && t.getBuildSetTask()
                .getBuildConfigSetRecord()
                .map(gc -> Integer.valueOf(groupBuildId).equals(gc.getId()))
                .orElse(false);
        return getBuildList(pageInfo, predicate, withBuildConfigSetRecordId(Integer.valueOf(groupBuildId)));
    }

    @Override
    public Graph<Build> getBuildGraphForGroupBuild(String groupBuildId) {
        org.jboss.util.graph.Graph<BuildTask> runningBuildTaskGraph = getRunningBuildGraphForGroupBuild(
                Integer.valueOf(groupBuildId));
        org.jboss.util.graph.Graph<Build> runningBuildGraph = convertBuildTaskToBuildDto(runningBuildTaskGraph);

        GraphWithMetadata<Build, Integer> groupBuildGraph = getBuildConfigSetRecordGraph(Integer.valueOf(groupBuildId));
        org.jboss.util.graph.Graph<Build> graph = groupBuildGraph.getGraph();

        GraphUtils.merge(graph, runningBuildGraph);

        Map<String, String> metadata = getGraphMetadata(groupBuildGraph.getMissingNodeIds());
        GraphDtoBuilder graphBuilder = new GraphDtoBuilder(metadata);

        Graph<Build> graphDto = graphBuilder.from(graph, Build.class);
        return graphDto;
    }

    @Override
    public Graph<Build> getDependencyGraph(String buildId) {
        GraphWithMetadata<Build, Integer> buildGraph = getDependencyGraph(BuildMapper.idMapper.toEntity(buildId));

        Map<String, String> metadata = getGraphMetadata(buildGraph.getMissingNodeIds());
        GraphDtoBuilder graphBuilder = new GraphDtoBuilder(metadata);
        return graphBuilder.from(buildGraph.getGraph(), Build.class);
    }

    org.jboss.util.graph.Graph<BuildTask> getRunningBuildGraphForGroupBuild(Integer groupBuildId) {
        List<BuildTask> buildTasks = nullableStreamOf(buildCoordinator.getSubmittedBuildTasks())
                .filter(Objects::nonNull)
                .filter(t -> t.getBuildSetTask() != null && groupBuildId.equals(t.getBuildSetTask().getId()))
                .sorted(Comparator.comparingInt(BuildTask::getId))
                .collect(Collectors.toList());

        org.jboss.util.graph.Graph<BuildTask> buildGraph = new org.jboss.util.graph.Graph<>();
        for (BuildTask buildTask : buildTasks) {
            // Adds buildTask and related tasks (dependencies and dependents) to the graph if they don't already exists
            org.jboss.util.graph.Graph<BuildTask> dependencyGraph = getBuiltTaskDependencyGraph(buildTask.getId());
            GraphUtils.merge(buildGraph, dependencyGraph);
        }
        return buildGraph;
    }

    private org.jboss.util.graph.Graph<BuildTask> getBuiltTaskDependencyGraph(Integer buildId) {
        org.jboss.util.graph.Graph<BuildTask> graph = new org.jboss.util.graph.Graph<>();
        GraphBuilder<BuildTask> graphBuilder = new GraphBuilder<BuildTask>(
                id -> Optional.ofNullable(getSubmittedBuild(id)),
                bt -> bt.getDependencies().stream().map(BuildTask::getId).collect(Collectors.toList()),
                bt -> bt.getDependants().stream().map(BuildTask::getId).collect(Collectors.toList()));

        Vertex<BuildTask> current = graphBuilder.buildDependencyGraph(graph, buildId);
        if (current != null) {
            BuildTask currentTask = current.getData();
            graphBuilder.buildDependentGraph(graph, currentTask.getId());
        }
        return graph;
    }

    private org.jboss.util.graph.Graph<Build> convertBuildTaskToBuildDto(
            org.jboss.util.graph.Graph<BuildTask> taskGraph) {
        org.jboss.util.graph.Graph<Build> buildGraph = new org.jboss.util.graph.Graph<>();

        for (Vertex<BuildTask> buildTaskVertex : taskGraph.getVerticies()) {
            Build recordRest = buildMapper.fromBuildTask(buildTaskVertex.getData());
            Vertex<Build> buildRecordVertex = new NameUniqueVertex<>(recordRest.getId(), recordRest);
            buildGraph.addVertex(buildRecordVertex);
        }

        // create edges
        for (Vertex<BuildTask> vertex : taskGraph.getVerticies()) {
            for (Object o : vertex.getOutgoingEdges()) {
                Edge<BuildTask> edge = (Edge<BuildTask>) o;
                buildGraph.addEdge(
                        buildGraph.findVertexByName(edge.getFrom().getName()),
                        buildGraph.findVertexByName(edge.getTo().getName()),
                        edge.getCost());
            }
        }
        return buildGraph;
    }

    private GraphWithMetadata<Build, Integer> getBuildConfigSetRecordGraph(Integer groupBuildId) {
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.queryById(groupBuildId);
        org.jboss.util.graph.Graph<Build> buildGraph = new org.jboss.util.graph.Graph<>();
        List<Integer> missingBuildRecordId = new ArrayList<>();
        for (BuildRecord buildRecord : buildConfigSetRecord.getBuildRecords()) {
            GraphWithMetadata<Build, Integer> dependencyGraph = getDependencyGraph(buildRecord.getId());
            GraphUtils.merge(buildGraph, dependencyGraph.getGraph());
            logger.trace(
                    "Merged graph from buildRecordId {} to BuildConfigSetRecordGraph {}; Edges {},",
                    buildRecord.getId(),
                    buildGraph,
                    buildGraph.getEdges());
            missingBuildRecordId.addAll(dependencyGraph.getMissingNodeIds());
        }
        return new GraphWithMetadata<>(buildGraph, missingBuildRecordId);
    }

    private GraphWithMetadata<Build, Integer> getDependencyGraph(int buildId) {
        BuildTask buildTask = getSubmittedBuild(buildId);

        GraphWithMetadata<Build, Integer> buildRecordGraph;
        if (buildTask == null) {
            logger.debug("Looking for stored buildRecordId: {}.", buildId);
            BuildRecord buildRecord = repository.queryById(buildId);
            if (buildRecord == null) {
                logger.warn("Cannot find build {}", buildId);
                return null;
            } else {
                GraphWithMetadata<BuildRecord, Integer> dependencyGraph = buildRecordRepository
                        .getDependencyGraph(buildId);
                org.jboss.util.graph.Graph<Build> buildGraph = convertBuildRecordToRest(dependencyGraph.getGraph());
                logger.trace(
                        "Rest graph for buildRecordId {} {}; Graph edges {}.",
                        buildId,
                        buildGraph,
                        buildGraph.getEdges());
                buildRecordGraph = new GraphWithMetadata<>(buildGraph, dependencyGraph.getMissingNodeIds());
            }
        } else {
            logger.debug("Getting dependency graph for running build: {}.", buildId);

            org.jboss.util.graph.Graph<BuildTask> graph = getBuiltTaskDependencyGraph(buildId);

            org.jboss.util.graph.Graph<Build> buildGraph = convertBuildTaskToBuildDto(graph);
            buildRecordGraph = new GraphWithMetadata<>(buildGraph, new ArrayList<>());
        }
        return buildRecordGraph;
    }

    private org.jboss.util.graph.Graph<Build> convertBuildRecordToRest(
            org.jboss.util.graph.Graph<BuildRecord> recordGraph) {
        org.jboss.util.graph.Graph<Build> buildGraph = new org.jboss.util.graph.Graph<>();

        for (Vertex<BuildRecord> buildRecordVertex : recordGraph.getVerticies()) {
            Build recordRest = mapper.toDTO(buildRecordVertex.getData());
            Vertex<Build> buildRecordRestVertex = new NameUniqueVertex<>(recordRest.getId(), recordRest);
            buildGraph.addVertex(buildRecordRestVertex);
        }
        // create edges
        for (Edge<BuildRecord> edge : recordGraph.getEdges()) {
            buildGraph.addEdge(
                    buildGraph.findVertexByName(edge.getFrom().getName()),
                    buildGraph.findVertexByName(edge.getTo().getName()),
                    edge.getCost());
        }
        return buildGraph;
    }

    private Map<String, String> getGraphMetadata(List<Integer> missingBuildIds) {
        Map<String, String> metadata = new HashMap<>();
        if (missingBuildIds.size() > 0) {
            metadata.put("status", "INCOMPLETE");
            for (Integer buildId : missingBuildIds) {
                metadata.put("description", "Missing some Build Records: " + buildId);
            }
        }
        return metadata;
    }

    @Override
    public URI getInternalScmArchiveLink(String buildId) {

        BuildRecord buildRecord = repository.queryById(BuildMapper.idMapper.toEntity(buildId));

        if (buildRecord.getScmRevision() == null) {
            return null;
        } else {

            try {
                return new URI(
                        gerrit.generateDownloadUrlWithGerritGitweb(
                                buildRecord.getScmRepoURL(),
                                buildRecord.getScmRevision()));
            } catch (GerritException | URISyntaxException e) {
                throw new RepositoryViolationException(e);
            }
        }
    }

    /**
     * If a build record with the id is not found, EmptyEntityException is thrown
     * 
     * @param buildId
     * @return BuildRecord
     * @throws EmptyEntityException if build record with associated id does not exist
     */
    private BuildRecord getBuildRecord(String buildId) {
        BuildRecord buildRecord = repository.queryById(BuildMapper.idMapper.toEntity(buildId));

        if (buildRecord == null) {
            throw new EmptyEntityException("Build with id: " + buildId + " does not exist!");
        } else {
            return buildRecord;
        }
    }

    private BuildTask getSubmittedBuild(Integer id) {
        return buildCoordinator.getSubmittedBuildTasks()
                .stream()
                .filter(submittedBuild -> id.equals(submittedBuild.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Build getSpecific(String buildId) {

        List<BuildTask> runningBuilds = buildCoordinator.getSubmittedBuildTasks();

        Build build = runningBuilds.stream()
                .filter(buildTask -> buildId.equals(BuildMapper.idMapper.toDto(buildTask.getId())))
                .findAny()
                .map(buildMapper::fromBuildTask)
                .orElse(null);

        // if build not in runningBuilds, check the database
        if (build == null) {
            // use findByIdFetchProperties instead of super.getSpecific to get 'BuildConfigurationAudited' object
            build = mapper.toDTO(buildRecordRepository.findByIdFetchProperties(BuildMapper.idMapper.toEntity(buildId)));
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
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                BuildRecordPredicates.withStatus(status),
                BuildRecordPredicates.withBuildLogContains(search));
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public void setBuiltArtifacts(String buildId, List<String> artifactIds) {
        Set<Integer> ids = artifactIds.stream().map(Integer::valueOf).collect(Collectors.toSet());
        List<Artifact> artifacts = artifactRepository.queryWithPredicates(withIds(ids));

        if (ids.size() != artifacts.size()) {
            artifacts.stream().map(Artifact::getId).forEach(ids::remove);
            throw new InvalidEntityException("Artifacts not found, missing ids: " + ids);
        }

        final Integer id = BuildMapper.idMapper.toEntity(buildId);
        BuildRecord buildRecord = repository.queryById(id);
        for (Artifact artifact : artifacts) {
            if (artifact.getBuildRecord() != null && !id.equals(artifact.getBuildRecord().getId())) {
                throw new ConflictedEntryException(
                        "Artifact " + artifact.getId() + " is already marked as built by different build.",
                        BuildRecord.class,
                        artifact.getBuildRecord().getId().toString());
            }
            artifact.setBuildRecord(buildRecord);
        }
        HashSet<Artifact> oldBuiltArtifacts = new HashSet<>(buildRecord.getBuiltArtifacts());
        oldBuiltArtifacts.stream().filter(a -> !ids.contains(a.getId())).forEach(a -> a.setBuildRecord(null));
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public void setDependentArtifacts(String buildId, List<String> artifactIds) {
        BuildRecord buildRecord = repository.queryById(BuildMapper.idMapper.toEntity(buildId));
        Set<Artifact> artifacts = artifactIds.stream()
                .map(aId -> Artifact.Builder.newBuilder().id(Integer.valueOf(aId)).build())
                .collect(Collectors.toSet());
        buildRecord.setDependencies(artifacts);
        repository.save(buildRecord);
    }

    @Override
    public RunningBuildCount getRunningCount() {

        List<BuildTask> x = buildCoordinator.getSubmittedBuildTasks();

        int waitingForDependencies = 0;
        int running = 0;
        int enqueued = 0;

        for (BuildTask task : x) {
            switch (task.getStatus()) {
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

        return new RunningBuildCount(running, enqueued, waitingForDependencies);
    }

    public Page<Build> getByAttribute(BuildPageInfo buildPageInfo, Map<String, String> attributeConstraints) {
        Set<Predicate<BuildRecord>> predicates = new HashSet<>();
        for (Map.Entry<String, String> entry : attributeConstraints.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith("!")) {
                predicates.add(withoutAttribute(key.substring(1)));
            } else {
                predicates.add(withAttribute(key, value));
            }
        }

        Predicate<BuildRecord> queryPredicate = rsqlPredicateProducer
                .getCriteriaPredicate(BuildRecord.class, buildPageInfo.getQ());
        predicates.add(queryPredicate);

        Predicate<BuildRecord>[] predicatesArray = predicates.toArray(new Predicate[predicates.size()]);

        PageInfo pageInfo = toPageInfo(buildPageInfo);
        SortInfo sortInfo = rsqlPredicateProducer.getSortInfo(type, buildPageInfo.getSort());
        List<BuildRecord> resultList = ((BuildRecordRepository) BuildProviderImpl.this.repository)
                .queryWithPredicatesUsingCursor(pageInfo, sortInfo, predicatesArray);

        int hits = repository.count(predicatesArray);

        return new Page<>(
                buildPageInfo.getPageIndex(),
                buildPageInfo.getPageSize(),
                hits,
                resultList.stream().map(b -> mapper.toDTO(b)).collect(Collectors.toList()));
    }

    private DefaultPageInfo toPageInfo(BuildPageInfo buildPageInfo) {
        return new DefaultPageInfo(
                buildPageInfo.getPageIndex() * buildPageInfo.getPageSize(),
                buildPageInfo.getPageSize());
    }

    /**
     * Returns the page of builds filtered by given BuildPageInfo parameters and predicate.
     */
    private Page<Build> getBuildList(
            BuildPageInfo pageInfo,
            java.util.function.Predicate<BuildTask> predicate,
            Predicate<BuildRecord> dbPredicate) {

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
        List<Build> build = readLatestRunningBuild(predicate).map(Collections::singletonList)
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
    private Page<Build> getLatestBuild(
            java.util.function.Predicate<BuildTask> predicate,
            Predicate<BuildRecord> dbPredicate) {
        TreeSet<Build> sorted = new TreeSet<>(Comparator.comparing(Build::getSubmitTime).reversed());
        readLatestRunningBuild(predicate).ifPresent(sorted::add);
        readLatestFinishedBuild(dbPredicate).ifPresent(sorted::add);
        if (sorted.size() > 1) {
            sorted.pollLast();
        }

        return new Page<>(0, 1, sorted.size(), sorted.size(), sorted);
    }

    /**
     * Returns the page of builds (running or finished) filtered by given BuildPageInfo and predicate.
     */
    private Page<Build> getBuilds(
            BuildPageInfo pageInfo,
            java.util.function.Predicate<BuildTask> predicate,
            Predicate<BuildRecord> dbPredicate) {
        List<Build> runningBuilds = readRunningBuilds(pageInfo, predicate);

        int firstPossibleDBIndex = pageInfo.getPageIndex() * pageInfo.getPageSize() - runningBuilds.size();
        int lastPossibleDBIndex = (pageInfo.getPageIndex() + 1) * pageInfo.getPageSize() - 1;
        int toSkip = min(runningBuilds.size(), pageInfo.getPageIndex() * pageInfo.getPageSize());

        Predicate<BuildRecord>[] predicates = preparePredicates(
                dbPredicate,
                pageInfo.getQ(),
                pageInfo.getBuildConfigName());
        Comparator<Build> comparing = Comparator.comparing(Build::getSubmitTime).reversed();
        if (!StringUtils.isEmpty(pageInfo.getSort())) {
            comparing = rsqlPredicateProducer.getComparator(pageInfo.getSort());
        }

        SortInfo sortInfo = rsqlPredicateProducer.getSortInfo(type, pageInfo.getSort());
        MergeIterator<Build> builds = new MergeIterator(
                runningBuilds.iterator(),
                new BuildIterator(
                        firstPossibleDBIndex,
                        lastPossibleDBIndex,
                        pageInfo.getPageSize(),
                        sortInfo,
                        predicates),
                comparing);
        List<Build> resultList = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(builds, Spliterator.ORDERED | Spliterator.SORTED), false)
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

    private Predicate<BuildRecord>[] preparePredicates(
            Predicate<BuildRecord> dbPredicate,
            String query,
            String buildConfigName) {
        List<Predicate<BuildRecord>> predicates = new ArrayList<>(3);

        predicates.add(dbPredicate);

        if (!StringUtils.isEmpty(query)) {
            predicates.add(rsqlPredicateProducer.getCriteriaPredicate(BuildRecord.class, query));
        }

        if (!StringUtils.isEmpty(buildConfigName)) {
            predicates.add(getPredicateWithBuildConfigName(buildConfigName));
        }

        return predicates.toArray(new Predicate[0]);
    }

    private Predicate<BuildRecord> getPredicateWithBuildConfigName(String buildConfigName) {
        List<IdRev> buildConfigurationAuditedIdRevs = buildConfigurationAuditedRepository
                .searchIdRevForBuildConfigurationName(buildConfigName);

        if (!buildConfigurationAuditedIdRevs.isEmpty()) {
            return BuildRecordPredicates.withBuildConfigurationIdRev(buildConfigurationAuditedIdRevs);
        } else {
            return Predicate.nonMatching();
        }
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

        if (!StringUtils.isEmpty(pageInfo.getBuildConfigName())) {
            predicate = predicate
                    .and(t -> pageInfo.getBuildConfigName().equals(t.getBuildConfigurationAudited().getName()));
        }

        return nullableStreamOf(buildCoordinator.getSubmittedBuildTasks()).filter(t -> t != null)
                .filter(predicate)
                .map(buildMapper::fromBuildTask)
                .filter(streamPredicate)
                .sorted(comparing)
                .collect(Collectors.toList());
    }

    private Optional<Build> readLatestRunningBuild(java.util.function.Predicate<BuildTask> predicate) {
        return nullableStreamOf(buildCoordinator.getSubmittedBuildTasks()).filter(t -> t != null)
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

        public BuildIterator(
                int firstIndex,
                int lastIndex,
                int pageSize,
                SortInfo sortInfo,
                Predicate<BuildRecord>... predicate) {
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
            builds = ((BuildRecordRepository) BuildProviderImpl.this.repository)
                    .queryWithPredicatesUsingCursor(pageInfo, sortInfo, predicates);
            it = builds.iterator();
            if (builds.size() < size) {
                firstIndex = lastIndex + 1;
            } else {
                firstIndex += size;
            }
        }
    }
}
