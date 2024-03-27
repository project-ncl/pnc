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

import static java.lang.Math.min;
import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_BUILD_ADMIN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_BUILD_DELETE;
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
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withoutLinkedNRRRecordOlderThanTimestamp;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.gerrit.ScmUrlGeneratorProvider;
import org.jboss.pnc.common.gerrit.ScmException;
import org.jboss.pnc.common.graph.GraphBuilder;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.pnc.LongBase32IdConverter;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.insights.BuildRecordInsights;
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
import org.jboss.pnc.facade.validation.CorruptedDataException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.ResultMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.CursorPageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;
import org.jboss.pnc.spi.exception.ValidationException;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PermitAll
@Stateless
public class BuildProviderImpl extends AbstractUpdatableProvider<Base32LongID, BuildRecord, Build, BuildRef>
        implements BuildProvider {

    private static final Logger logger = LoggerFactory.getLogger(BuildProviderImpl.class);

    private ArtifactRepository artifactRepository;
    private BuildRecordRepository buildRecordRepository;
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    private ScmUrlGeneratorProvider scmUrlGenerator;
    private BuildConfigurationRevisionMapper buildConfigurationRevisionMapper;
    private BuildMapper buildMapper;

    private BuildCoordinator buildCoordinator;
    private UserService userService;

    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;
    private ResultMapper resultMapper;

    private KeycloakServiceClient keycloakServiceClient;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    public BuildProviderImpl(
            ArtifactRepository artifactRepository,
            BuildRecordRepository repository,
            BuildMapper mapper,
            BuildConfigurationRepository buildConfigurationRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            BuildConfigSetRecordRepository buildConfigSetRecordRepository,
            ScmUrlGeneratorProvider scmUrlGenerator,
            BuildConfigurationRevisionMapper buildConfigurationRevisionMapper,
            BuildCoordinator buildCoordinator,
            UserService userService,
            TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker,
            ResultMapper resultMapper,
            KeycloakServiceClient keycloakServiceClient) {
        super(repository, mapper, BuildRecord.class);

        this.artifactRepository = artifactRepository;
        this.buildRecordRepository = repository;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.scmUrlGenerator = scmUrlGenerator;
        this.buildConfigurationRevisionMapper = buildConfigurationRevisionMapper;
        this.buildMapper = mapper;
        this.buildCoordinator = buildCoordinator;
        this.userService = userService;
        this.temporaryBuildsCleanerAsyncInvoker = temporaryBuildsCleanerAsyncInvoker;
        this.resultMapper = resultMapper;
        this.keycloakServiceClient = keycloakServiceClient;
    }

    @Override
    public Build store(Build restEntity) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct build creation is not available.");
    }

    @RolesAllowed({ USERS_BUILD_ADMIN, USERS_ADMIN })
    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting persistent builds is never allowed");
    }

    @RolesAllowed({ USERS_BUILD_ADMIN, USERS_ADMIN })
    @Override
    public Build update(String buildId, Build restEntity) {
        Base32LongID id = parseId(buildId);
        validateBeforeUpdating(id, restEntity);
        logger.debug("Updating build: " + restEntity.toString());
        BuildRecord entityInDB = repository.queryById(id);
        entityInDB.setStatus(restEntity.getStatus());
        return mapper.toDTO(entityInDB);
    }

    @RolesAllowed({ USERS_BUILD_DELETE, USERS_BUILD_ADMIN, USERS_ADMIN })
    @Override
    public boolean delete(String buildId, String callback) {

        try {
            String accessToken = keycloakServiceClient.getAuthToken();
            return temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuild(
                    parseId(buildId),
                    accessToken,
                    notifyOnBuildDeletionCompletion(callback, accessToken));
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }
    }

    private Consumer<Result> notifyOnBuildDeletionCompletion(String callback, String authToken) {
        return (result) -> {
            if (callback != null && !callback.isEmpty()) {
                try {
                    HttpUtils.performHttpPostRequest(
                            callback,
                            OBJECT_MAPPER.writeValueAsString(resultMapper.toDTO(result)),
                            authToken);
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
                withoutImplicitDependants(),
                withoutLinkedNRRRecordOlderThanTimestamp(new Date(timestamp)));
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
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository
                .queryById(Integer.valueOf(groupBuildId));
        if (buildConfigSetRecord == null) {
            throw new EmptyEntityException("Build group " + groupBuildId + " does not exists.");
        }
        List<String> runningAndStoredIds = getBuildIdsInTheGroup(buildConfigSetRecord);
        org.jboss.util.graph.Graph<BuildWithDependencies> buildGraph = new org.jboss.util.graph.Graph<>();
        for (String buildId : runningAndStoredIds) {
            org.jboss.util.graph.Graph<BuildWithDependencies> dependencyGraph = createBuildDependencyGraph(buildId);
            GraphUtils.merge(buildGraph, dependencyGraph);
            logger.trace(
                    "Merged graph from buildRecordId {} to BuildConfigSetRecordGraph {}; Edges {},",
                    buildId,
                    buildGraph,
                    buildGraph.getEdges());
        }

        GraphDtoBuilder<BuildWithDependencies, Build> graphBuilder = new GraphDtoBuilder();
        Graph<Build> graphDto = graphBuilder.from(buildGraph, Build.class, vertex -> vertex.getData().getBuild());
        return graphDto;
    }

    /**
     * @return Running and completed build ids from the Build Group.
     */
    private List<String> getBuildIdsInTheGroup(BuildConfigSetRecord buildConfigSetRecord) {
        List<String> runningTaskIds = nullableStreamOf(buildCoordinator.getSubmittedBuildTasks())
                .filter(Objects::nonNull)
                .filter(
                        t -> t.getBuildSetTask() != null
                                && buildConfigSetRecord.getId().equals(t.getBuildSetTask().getId()))
                .sorted(Comparator.comparing(bt -> bt.getBuildConfigurationAudited().getName()))
                .map(t -> t.getId())
                .collect(Collectors.toList());

        List<String> runningAndStoredIds = new ArrayList<>(runningTaskIds);

        Set<String> storedBuildIds = buildConfigSetRecord.getBuildRecords()
                .stream()
                .map(br -> BuildMapper.idMapper.toDto(br.getId()))
                .collect(Collectors.toSet());
        runningAndStoredIds.addAll(storedBuildIds);
        return runningAndStoredIds;
    }

    @Override
    public Graph<Build> getDependencyGraph(String buildId) {
        Build specific = getSpecific(buildId);
        if (specific == null) {
            throw new EmptyEntityException("there is no record for given buildId.");
        }
        org.jboss.util.graph.Graph<BuildWithDependencies> buildGraph = createBuildDependencyGraph(buildId);
        GraphDtoBuilder<BuildWithDependencies, Build> graphBuilder = new GraphDtoBuilder();
        return graphBuilder.from(buildGraph, Build.class, vertex -> vertex.getData().getBuild());
    }

    private org.jboss.util.graph.Graph<BuildWithDependencies> createBuildDependencyGraph(String buildId) {
        org.jboss.util.graph.Graph<BuildWithDependencies> graph = new org.jboss.util.graph.Graph<>();
        GraphBuilder<BuildWithDependencies, String> graphBuilder = new GraphBuilder<>(
                this::getRunningOrCompletedBuild,
                BuildWithDependencies::getDependencies,
                BuildWithDependencies::getDependants);

        Vertex<BuildWithDependencies> current = graphBuilder.buildDependencyGraph(graph, buildId);
        if (current != null) {
            BuildWithDependencies currentTask = current.getData();
            graphBuilder.buildDependentGraph(graph, currentTask.getBuild().getId());
        }
        return graph;
    }

    @Override
    public URI getInternalScmArchiveLink(String buildId) {

        BuildRecord buildRecord = buildRecordRepository.findByIdFetchProperties(parseId(buildId));

        if (buildRecord.getScmRevision() == null) {
            return null;
        } else {
            try {
                var provider = scmUrlGenerator.determineScmProvider(
                        buildRecord.getScmRepoURL(),
                        buildRecord.getBuildConfigurationAudited().getRepositoryConfiguration().getInternalUrl());

                return new URI(
                        scmUrlGenerator.getScmUrlGenerator(provider)
                                .generateDownloadUrlWithGitweb(
                                        buildRecord.getScmRepoURL(),
                                        buildRecord.getScmRevision()));
            } catch (ScmException | URISyntaxException e) {
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
        BuildRecord buildRecord = repository.queryById(parseId(buildId));

        if (buildRecord == null) {
            throw new EmptyEntityException("Build with id: " + buildId + " does not exist!");
        } else {
            return buildRecord;
        }
    }

    /**
     *
     * @param id
     * @return BuildWithDependencies
     * @throws CorruptedDataException when there is no running nor completed build for a given id
     */
    private BuildWithDependencies getRunningOrCompletedBuild(String id) {
        Optional<BuildTask> buildTask = buildCoordinator.getSubmittedBuildTasks()
                .stream()
                .filter(submittedBuild -> submittedBuild.getId().equals(id))
                .findFirst();
        if (buildTask.isPresent()) {
            return new BuildWithDependencies(buildTask.get());
        } else {
            BuildRecord buildRecord = buildRecordRepository.findByIdFetchProperties(parseId(id));
            if (buildRecord == null) {
                throw new CorruptedDataException("Missing build with id:" + id);
            }
            return new BuildWithDependencies(buildRecord);
        }
    }

    @Override
    public Build getSpecific(String buildId) {
        List<BuildTask> runningBuilds = buildCoordinator.getSubmittedBuildTasks();

        Build build = runningBuilds.stream()
                .filter(buildTask -> buildId.equals(buildTask.getId()))
                .findAny()
                .map(buildMapper::fromBuildTask)
                .orElse(null);

        // if build not in runningBuilds, check the database
        if (build == null) {
            // use findByIdFetchProperties instead of super.getSpecific to get 'BuildConfigurationAudited' object
            build = mapper.toDTO(buildRecordRepository.findByIdFetchProperties(parseId(buildId)));
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

    @RolesAllowed({ USERS_BUILD_ADMIN, USERS_ADMIN })
    @Override
    public void setBuiltArtifacts(String buildId, List<String> artifactIds) {
        Set<Integer> ids = artifactIds.stream().map(Integer::valueOf).collect(Collectors.toSet());
        List<Artifact> artifacts = artifactRepository.queryWithPredicates(withIds(ids));

        if (ids.size() != artifacts.size()) {
            artifacts.stream().map(Artifact::getId).forEach(ids::remove);
            throw new InvalidEntityException("Artifacts not found, missing ids: " + ids);
        }

        final Base32LongID id = parseId(buildId);
        BuildRecord buildRecord = repository.queryById(id);
        for (Artifact artifact : artifacts) {
            if (artifact.getBuildRecord() != null && !id.equals(artifact.getBuildRecord().getId())) {
                throw new ConflictedEntryException(
                        "Artifact " + artifact.getId() + " is already marked as built by different build.",
                        BuildRecord.class,
                        BuildMapper.idMapper.toDto(artifact.getBuildRecord().getId()));
            }
            artifact.setBuildRecord(buildRecord);
        }
        HashSet<Artifact> oldBuiltArtifacts = new HashSet<>(buildRecord.getBuiltArtifacts());
        oldBuiltArtifacts.stream().filter(a -> !ids.contains(a.getId())).forEach(a -> a.setBuildRecord(null));
    }

    @Override
    public Set<String> getBuiltArtifactIds(String buildId) {
        final Base32LongID id = parseId(buildId);
        BuildRecord buildRecord = repository.queryById(id);
        return nullableStreamOf(buildRecord.getBuiltArtifacts()).map(builtArtifact -> builtArtifact.getId().toString())
                .collect(Collectors.toSet());
    }

    @RolesAllowed({ USERS_BUILD_ADMIN, USERS_ADMIN })
    @Override
    public void setDependentArtifacts(String buildId, List<String> artifactIds) {
        BuildRecord buildRecord = repository.queryById(parseId(buildId));
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
        SortInfo<BuildRecord> sortInfo = rsqlPredicateProducer.getSortInfo(type, buildPageInfo.getSort());
        List<BuildRecord> resultList = ((BuildRecordRepository) BuildProviderImpl.this.repository)
                .queryWithPredicates(pageInfo, sortInfo, predicatesArray);

        int hits = repository.count(predicatesArray);

        return new Page<>(
                buildPageInfo.getPageIndex(),
                buildPageInfo.getPageSize(),
                hits,
                resultList.stream().map(b -> mapper.toDTO(b)).collect(Collectors.toList()));
    }

    @Override
    public Page<BuildRecordInsights> getAllBuildRecordInsightsNewerThanTimestamp(
            int pageIndex,
            int pageSize,
            Date lastupdatetime) {

        logger.debug(
                "Executing getAllBuildRecordInsightsNewerThanTimestamp with parameters pageIndex: {}, pageSize: {}, lastupdatetime: {}",
                pageIndex,
                pageSize,
                lastupdatetime);

        int count = buildRecordRepository.countAllBuildRecordInsightsNewerThanTimestamp(lastupdatetime);
        logger.debug("BuildRecordInsightsCount: {}", count);

        int totalPages = (int) Math.ceil((count) / (double) pageSize);
        logger.debug("TotalPages of BuildRecordInsightsCount: {}", totalPages);

        List<BuildRecordInsights> content = new ArrayList<>();

        if (count > 0) {
            int offset = pageIndex * pageSize;

            logger.debug("offset: {}", offset);

            List<Object[]> rawBuildInsights = buildRecordRepository
                    .getAllBuildRecordInsightsNewerThanTimestamp(lastupdatetime, pageSize, offset);
            for (Object[] rawBuildInsight : rawBuildInsights) {

                Long buildRecordId = ((BigInteger) rawBuildInsight[0]).longValue();
                String buildContentId = (String) rawBuildInsight[1];
                buildContentId = StringUtils.isEmpty(buildContentId) ? LongBase32IdConverter.toString(buildRecordId)
                        : buildContentId;
                Date submitTime = (Date) rawBuildInsight[2];
                Date startTime = (Date) rawBuildInsight[3];
                Date endTime = (Date) rawBuildInsight[4];
                Date lastTime = (Date) rawBuildInsight[5];
                Integer submitYear = (Integer) rawBuildInsight[6];
                Integer submitMonth = (Integer) rawBuildInsight[7];
                Integer submitQuarter = (Integer) rawBuildInsight[8];
                String status = (String) rawBuildInsight[9];
                Boolean temporaryBuild = (Boolean) rawBuildInsight[10];
                Boolean autoAlign = (Boolean) rawBuildInsight[11];
                Boolean brewPullActive = (Boolean) rawBuildInsight[12];
                String buildType = (String) rawBuildInsight[13];
                String executionRootName = (String) rawBuildInsight[14];
                String executionRootVersion = (String) rawBuildInsight[15];
                Integer userId = (Integer) rawBuildInsight[16];
                String username = (String) rawBuildInsight[17];
                Integer buildConfigurationId = (Integer) rawBuildInsight[18];
                Integer buildConfigurationRev = (Integer) rawBuildInsight[19];
                String buildConfigurationName = (String) rawBuildInsight[20];
                Integer buildConfigSetRecordId = (Integer) rawBuildInsight[21];
                Integer productMilestoneId = (Integer) rawBuildInsight[22];
                String productMilestoneVersion = (String) rawBuildInsight[23];
                Integer projectId = (Integer) rawBuildInsight[24];
                String projectName = (String) rawBuildInsight[25];
                Integer productVersionId = (Integer) rawBuildInsight[26];
                String productVersion = (String) rawBuildInsight[27];
                Integer productId = (Integer) rawBuildInsight[28];
                String productName = (String) rawBuildInsight[29];

                BuildRecordInsights buildRecordInsights = BuildRecordInsights.builder()
                        .buildId(buildRecordId)
                        .buildContentId(buildContentId)
                        .submitTime(TimeUtils.toInstant(submitTime))
                        .startTime(TimeUtils.toInstant(startTime))
                        .endTime(TimeUtils.toInstant(endTime))
                        .lastUpdateTime(TimeUtils.toInstant(lastTime))
                        .submitYear(submitYear)
                        .submitMonth(submitMonth)
                        .submitQuarter(submitQuarter)
                        .status(status)
                        .temporarybuild(temporaryBuild)
                        .autoalign(autoAlign)
                        .brewpullactive(brewPullActive)
                        .buildType(buildType)
                        .executionRootName(executionRootName)
                        .executionRootVersion(executionRootVersion)
                        .userId(userId)
                        .username(username)
                        .buildConfigurationId(buildConfigurationId)
                        .buildConfigurationRev(buildConfigurationRev)
                        .buildConfigurationName(buildConfigurationName)
                        .buildConfigSetRecordId(buildConfigSetRecordId)
                        .productMilestoneId(productMilestoneId)
                        .productMilestoneVersion(productMilestoneVersion)
                        .projectId(projectId)
                        .projectName(projectName)
                        .productVersionId(productVersionId)
                        .productVersionVersion(productVersion)
                        .productId(productId)
                        .productName(productName)
                        .build();

                content.add(buildRecordInsights);
            }
        }

        return new Page<>(pageIndex, pageSize, totalPages, count, content);
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
        Iterator<BuildWrapper> wrappedRunningBuilds = runningBuilds.stream().map(BuildWrapper::new).iterator();

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

        SortInfo<BuildRecord> sortInfo = rsqlPredicateProducer.getSortInfo(type, pageInfo.getSort());
        MergeIterator<BuildWrapper> buildsIT = new MergeIterator<>(
                wrappedRunningBuilds,
                new BuildIterator(
                        firstPossibleDBIndex,
                        lastPossibleDBIndex,
                        pageInfo.getPageSize(),
                        sortInfo,
                        predicates),
                wrapperComparator(comparing));
        List<BuildWrapper> builds = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(buildsIT, Spliterator.ORDERED | Spliterator.SORTED), false)
                .skip(toSkip)
                .limit(pageInfo.getPageSize())
                .collect(Collectors.toList());

        fetchBuildConfigAudited(builds.stream().flatMap(BuildWrapper::buildRecordStream).collect(Collectors.toSet()));
        List<Build> resultList = builds.stream().map(BuildWrapper::getBuild).collect(Collectors.toList());

        int hits = repository.count(predicates) + runningBuilds.size();

        return new Page<>(
                pageInfo.getPageIndex(),
                pageInfo.getPageSize(),
                (int) Math.ceil((double) hits / pageInfo.getPageSize()),
                hits,
                resultList);
    }

    private void fetchBuildConfigAudited(Set<BuildRecord> buildRecords) {
        if (buildRecords.isEmpty()) {
            return;
        }
        Set<IdRev> idRevs = buildRecords.stream()
                .filter(buildRecord -> buildRecord.getBuildConfigurationAudited() == null)
                .map(BuildRecord::getBuildConfigurationAuditedIdRev)
                .collect(Collectors.toSet());
        prefetchFiledsOfBuildConfigs(idRevs);
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

    private void prefetchFiledsOfBuildConfigs(Set<IdRev> idRevs) {
        Set<Integer> buildConfigIDs = idRevs.stream().map(IdRev::getId).collect(Collectors.toSet());
        buildConfigurationRepository.queryWithPredicates(BuildConfigurationPredicates.withIds(buildConfigIDs));
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

    private Predicate<BuildRecord> getPredicateWithBuildConfigName(final String buildConfigName) {
        Function<List<IdRev>, Predicate<BuildRecord>> predicateFunction;
        String justBuildConfigName = buildConfigName;
        if (justBuildConfigName.startsWith("!")) {
            justBuildConfigName = justBuildConfigName.substring(1);
            predicateFunction = BuildRecordPredicates::exceptBuildConfigurationIdRev;
        } else {
            predicateFunction = BuildRecordPredicates::withBuildConfigurationIdRev;
        }

        String name = justBuildConfigName.replaceAll("[*]", "%").replaceAll("[?]", "_");

        List<BuildConfigurationAudited> buildConfigurationsAudited = buildConfigurationAuditedRepository
                .searchForBuildConfigurationName(name);
        List<IdRev> buildConfigurationAuditedIdRevs = buildConfigurationsAudited.stream()
                .filter(getBCAPredicateWithBuildConfigName(justBuildConfigName)) // with stripped "!"
                .map(BuildConfigurationAudited::getIdRev)
                .collect(Collectors.toList());

        if (!buildConfigurationAuditedIdRevs.isEmpty()) {
            return predicateFunction.apply(buildConfigurationAuditedIdRevs);
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
            predicate = predicate.and(
                    t -> getBCAPredicateWithBuildConfigName(pageInfo.getBuildConfigName())
                            .test(t.getBuildConfigurationAudited()));
        }

        return nullableStreamOf(buildCoordinator.getSubmittedBuildTasks()).filter(Objects::nonNull)
                .filter(predicate)
                .map(buildMapper::fromBuildTask)
                .filter(streamPredicate)
                .sorted(comparing)
                .collect(Collectors.toList());
    }

    private java.util.function.Predicate<BuildConfigurationAudited> getBCAPredicateWithBuildConfigName(
            String buildConfigName) {
        boolean negate = false;
        if (buildConfigName.startsWith("!")) {
            buildConfigName = buildConfigName.substring(1);
            negate = true;
        }

        java.util.function.Predicate<BuildConfigurationAudited> predicate;
        if (buildConfigName.contains("*") || buildConfigName.contains("%") || buildConfigName.contains("?")) {
            String name = buildConfigName.replaceAll("[*%]", ".*").replaceAll("[?]", ".");
            predicate = bca -> bca.getName().matches(name);
        } else {
            String name = buildConfigName;
            predicate = bca -> name.equals(bca.getName());
        }
        if (negate) {
            predicate = predicate.negate();
        }
        return predicate;
    }

    private Optional<Build> readLatestRunningBuild(java.util.function.Predicate<BuildTask> predicate) {
        return nullableStreamOf(buildCoordinator.getSubmittedBuildTasks()).filter(Objects::nonNull)
                .filter(predicate)
                .sorted(Comparator.comparing(BuildTask::getSubmitTime).reversed())
                .findFirst()
                .map(buildMapper::fromBuildTask);
    }

    private Optional<Build> readLatestFinishedBuild(Predicate<BuildRecord> predicate) {
        PageInfo pageInfo = this.pageInfoProducer.getPageInfo(0, 1);
        SortInfo<BuildRecord> sortInfo = DefaultSortInfo.desc(BuildRecord_.submitTime);
        List<BuildRecord> buildRecords = repository.queryWithPredicates(pageInfo, sortInfo, predicate);

        return buildRecords.stream().map(mapper::toDTO).findFirst();
    }

    class BuildIterator implements Iterator<BuildWrapper> {

        private List<BuildRecord> builds;
        private Iterator<BuildRecord> it;
        private final int maxPageSize;
        private int firstIndex;
        private final int lastIndex;
        private final SortInfo<BuildRecord> sortInfo;
        private final Predicate<BuildRecord>[] predicates;

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
            builds = BuildProviderImpl.this.repository.queryWithPredicates(pageInfo, sortInfo, predicates);
            it = builds.iterator();
            if (builds.size() < size) {
                firstIndex = lastIndex + 1;
            } else {
                firstIndex += size;
            }
        }
    }

    @Getter
    private class BuildWithDependencies {
        private final Build build;
        private final Collection<String> dependencies;
        private final Collection<String> dependants;

        public BuildWithDependencies(BuildTask buildTask) {
            build = buildMapper.fromBuildTask(buildTask);
            dependencies = buildTask.getDependencies().stream().map(BuildTask::getId).collect(Collectors.toSet());
            dependants = buildTask.getDependants().stream().map(BuildTask::getId).collect(Collectors.toSet());
        }

        public BuildWithDependencies(BuildRecord buildRecord) {
            build = buildMapper.toDTO(buildRecord);
            dependencies = Arrays.stream(buildRecord.getDependencyBuildRecordIds())
                    .map(BuildMapper.idMapper::toDto)
                    .collect(Collectors.toSet());
            dependants = Arrays.stream(buildRecord.getDependentBuildRecordIds())
                    .map(BuildMapper.idMapper::toDto)
                    .collect(Collectors.toSet());
        }
    }

    class BuildWrapper {
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
            if (buildRecord == null) {
                return Stream.empty();
            } else {
                return Stream.of(buildRecord);
            }
        }
    }

    private static Comparator<BuildWrapper> wrapperComparator(Comparator<Build> comparator) {
        return (o1, o2) -> (comparator.compare(o1.mappedBuild, o2.mappedBuild));
    }
}
