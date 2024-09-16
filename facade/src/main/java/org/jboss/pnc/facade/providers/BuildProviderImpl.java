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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.scm.ScmException;
import org.jboss.pnc.common.graph.GraphBuilder;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.pnc.LongBase32IdConverter;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.remotecoordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.insights.BuildRecordInsights;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RunningBuildCount;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.facade.providers.BuildFetcher.BuildWithDeps;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.util.GraphDtoBuilder;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mapper.api.ResultMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultOrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.jboss.pnc.spi.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

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
import static org.jboss.pnc.common.scm.ScmUrlGeneratorProvider.determineScmProvider;
import static org.jboss.pnc.common.scm.ScmUrlGeneratorProvider.getScmUrlGenerator;

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

    private BuildConfigurationRevisionMapper buildConfigurationRevisionMapper;
    private BuildMapper buildMapper;

    private BuildCoordinator buildCoordinator;
    private UserService userService;

    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;
    private ResultMapper resultMapper;
    private GroupBuildMapper groupBuildMapper;
    private BuildFetcher buildFetcher;

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
            BuildConfigurationRevisionMapper buildConfigurationRevisionMapper,
            BuildCoordinator buildCoordinator,
            UserService userService,
            TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker,
            ResultMapper resultMapper,
            GroupBuildMapper groupBuildMapper,
            BuildFetcher buildFetcher,
            KeycloakServiceClient keycloakServiceClient) {
        super(repository, mapper, BuildRecord.class);

        this.artifactRepository = artifactRepository;
        this.buildRecordRepository = repository;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.buildConfigurationRevisionMapper = buildConfigurationRevisionMapper;
        this.buildMapper = mapper;
        this.buildCoordinator = buildCoordinator;
        this.userService = userService;
        this.temporaryBuildsCleanerAsyncInvoker = temporaryBuildsCleanerAsyncInvoker;
        this.resultMapper = resultMapper;
        this.groupBuildMapper = groupBuildMapper;
        this.buildFetcher = buildFetcher;
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
            return temporaryBuildsCleanerAsyncInvoker
                    .deleteTemporaryBuild(parseId(buildId), notifyOnBuildDeletionCompletion(callback));
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }
    }

    private Consumer<Result> notifyOnBuildDeletionCompletion(String callback) {
        return (result) -> {
            if (callback != null && !callback.isEmpty()) {
                try {
                    HttpUtils.performHttpPostRequest(
                            callback,
                            OBJECT_MAPPER.writeValueAsString(resultMapper.toDTO(result)),
                            keycloakServiceClient.getAuthToken());
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
    public SSHCredentials getSshCredentials(String buildId) {
        BuildRecord buildRecord = getBuildRecord(buildId);
        User user = null;
        try {
            user = userService.currentUser();
        } catch (IllegalStateException e) {
            // leaves user null
        }
        if (!buildRecord.getUser().equals(user) && !userService.hasLoggedInUserRole(USERS_ADMIN)) {
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
        try {
            return getBuildList(pageInfo, _t -> true, (_r, _q, cb) -> cb.conjunction());
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Build> getBuildsForMilestone(BuildPageInfo pageInfo, String milestoneId) {
        java.util.function.Predicate<BuildTask> predicate = t -> t.getProductMilestone() != null
                && Integer.valueOf(milestoneId).equals(t.getProductMilestone().getId());
        try {
            return getBuildList(pageInfo, predicate, withPerformedInMilestone(Integer.valueOf(milestoneId)));
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
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
        try {
            return getBuildList(pageInfo, predicate, withBuildConfigurationIds(buildConfigIds));
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
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
        try {
            return getBuildList(pageInfo, predicate, withBuildConfigurationId(Integer.valueOf(buildConfigurationId)));
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Build> getBuildsForUser(BuildPageInfo pageInfo, String userId) {
        java.util.function.Predicate<BuildTask> predicate = t -> Integer.valueOf(userId).equals(t.getUser().getId());
        try {
            return getBuildList(pageInfo, predicate, withUserId(Integer.valueOf(userId)));
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Build> getBuildsForGroupConfiguration(BuildPageInfo pageInfo, String groupConfigurationId) {
        java.util.function.Predicate<BuildTask> predicate = t -> t.getBuildSetTask() != null && t.getBuildSetTask()
                .getBuildConfigSetRecord()
                .map(gc -> Integer.valueOf(groupConfigurationId).equals(gc.getBuildConfigurationSet().getId()))
                .orElse(false);
        try {
            return getBuildList(pageInfo, predicate, withBuildConfigSetId(Integer.valueOf(groupConfigurationId)));
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Build> getBuildsForGroupBuild(BuildPageInfo pageInfo, String groupBuildId) {
        java.util.function.Predicate<BuildTask> predicate = t -> t.getBuildConfigSetRecordId() != null
                && t.getBuildConfigSetRecordId().equals(groupBuildMapper.getIdMapper().toEntity(groupBuildId));
        try {
            return getBuildList(
                    pageInfo,
                    predicate,
                    withBuildConfigSetRecordId(groupBuildMapper.getIdMapper().toEntity(groupBuildId)));
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Graph<Build> getBuildGraphForGroupBuild(String groupBuildId) {
        Base32LongID id = groupBuildMapper.getIdMapper().toEntity(groupBuildId);
        Set<Base32LongID> buildIDs = buildFetcher.getGroupBuildContent(id);
        buildFetcher.precacheAllBuildsDeps(buildIDs);

        org.jboss.util.graph.Graph<BuildWithDeps> buildGraph = new org.jboss.util.graph.Graph<>();
        for (Base32LongID buildId : buildIDs) {
            org.jboss.util.graph.Graph<BuildWithDeps> dependencyGraph = createBuildDependencyGraph(buildId.getId());
            GraphUtils.merge(buildGraph, dependencyGraph);
            logger.trace(
                    "Merged graph from buildRecordId {} to BuildConfigSetRecordGraph {}; Edges {},",
                    buildId,
                    buildGraph,
                    buildGraph.getEdges());
        }

        return GraphDtoBuilder.from(buildGraph, Build.class, vertex -> vertex.getData().getBuild());
    }

    @Override
    public Graph<Build> getDependencyGraph(String buildId) {
        Base32LongID id = buildMapper.getIdMapper().toEntity(buildId);
        if (!buildFetcher.buildExists(id)) {
            throw new EmptyEntityException("there is no record for given buildId.");
        }
        buildFetcher.precacheAllBuildsDeps(id);

        org.jboss.util.graph.Graph<BuildWithDeps> buildGraph = createBuildDependencyGraph(buildId);
        return GraphDtoBuilder.from(buildGraph, Build.class, vertex -> vertex.getData().getBuild());
    }

    private org.jboss.util.graph.Graph<BuildWithDeps> createBuildDependencyGraph(String buildId) {
        org.jboss.util.graph.Graph<BuildWithDeps> graph = new org.jboss.util.graph.Graph<>();
        GraphBuilder<BuildWithDeps, String> graphBuilder = new GraphBuilder<>(
                id -> buildFetcher.getBuildWithDeps(id),
                BuildWithDeps::getDependencies,
                BuildWithDeps::getDependants);

        graphBuilder.buildDependencyGraph(graph, buildId);
        graphBuilder.buildDependentGraph(graph, buildId);
        return graph;
    }

    @Override
    public URI getInternalScmArchiveLink(String buildId) {

        BuildRecord buildRecord = buildRecordRepository.findByIdFetchProperties(parseId(buildId));

        if (buildRecord.getScmRevision() == null) {
            return null;
        } else {
            try {
                var provider = determineScmProvider(
                        buildRecord.getScmRepoURL(),
                        buildRecord.getBuildConfigurationAudited().getRepositoryConfiguration().getInternalUrl());

                return new URI(
                        getScmUrlGenerator(provider)
                                .generateTarballDownloadUrl(buildRecord.getScmRepoURL(), buildRecord.getScmRevision()));
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

    @Override
    public Build getSpecific(String buildId) {
        // use findByIdFetchProperties instead of super.getSpecific to get 'BuildConfigurationAudited' object
        Build build = mapper.toDTO(buildRecordRepository.findByIdFetchProperties(parseId(buildId)));

        // if build is not in DB, check running builds
        if (build == null) {
            try {
                build = buildCoordinator.getSubmittedBuildTask(buildId).map(buildMapper::fromBuildTask).orElse(null);
            } catch (RemoteRequestException | MissingDataException e) {
                throw new RuntimeException(e);
            }
        }

        return build;
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

        List<BuildTask> x;
        try {
            x = buildCoordinator.getSubmittedBuildTasks();
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }

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
        try {
            java.util.function.Predicate<BuildTask> none = _t -> false; // Running builds don't have attributes
            return getBuildList(buildPageInfo, none, Predicate.and(predicates));
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
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
            Predicate<BuildRecord> dbPredicate) throws RemoteRequestException, MissingDataException {

        if (pageInfo.isRunning()) {
            if (pageInfo.isLatest()) {
                return getLatestRunningBuild(pageInfo.getQ(), predicate);
            } else {
                return getRunningBuilds(pageInfo, predicate);
            }
        } else {
            if (pageInfo.isLatest()) {
                return getLatestBuild(pageInfo.getQ(), predicate, dbPredicate);
            } else {
                return getBuilds(pageInfo, predicate, dbPredicate);
            }
        }
    }

    /**
     * Returns the page of Latest Running build filtered by given predicate.
     */
    private Page<Build> getLatestRunningBuild(String query, java.util.function.Predicate<BuildTask> predicate)
            throws RemoteRequestException, MissingDataException {
        List<Build> build = readLatestRunningBuild(query, predicate).map(Collections::singletonList)
                .orElse(Collections.emptyList());
        return new Page<>(0, 1, build.size(), build.size(), build);
    }

    /**
     * Returns the page of Running builds filtered by given BuildPageInfo and predicate.
     */
    private Page<Build> getRunningBuilds(BuildPageInfo pageInfo, java.util.function.Predicate<BuildTask> predicate)
            throws RemoteRequestException, MissingDataException {
        List<Build> runningBuilds = readRunningBuilds(pageInfo, predicate);

        List<Build> builds = runningBuilds.stream()
                .skip((long) pageInfo.getPageIndex() * pageInfo.getPageSize())
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
            String query,
            java.util.function.Predicate<BuildTask> predicate,
            Predicate<BuildRecord> dbPredicate) throws RemoteRequestException, MissingDataException {
        TreeSet<Build> sorted = new TreeSet<>(Comparator.comparing(Build::getSubmitTime).reversed());

        readLatestRunningBuild(query, predicate).ifPresent(sorted::add);
        readLatestFinishedBuild(query, dbPredicate).ifPresent(sorted::add);

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
            Predicate<BuildRecord> dbPredicate) throws RemoteRequestException, MissingDataException {
        List<Build> runningBuilds = readRunningBuilds(pageInfo, predicate);

        Predicate<BuildRecord>[] predicates = preparePredicates(
                dbPredicate,
                pageInfo.getQ(),
                pageInfo.getBuildConfigName());
        Comparator<Build> comparing = Comparator.comparing(Build::getSubmitTime).reversed();
        SortInfo<BuildRecord> sortInfo = DefaultSortInfo.desc(BuildRecord_.submitTime);
        if (!StringUtils.isEmpty(pageInfo.getSort())) {
            comparing = rsqlPredicateProducer.getComparator(pageInfo.getSort());
            sortInfo = rsqlPredicateProducer.getSortInfo(type, pageInfo.getSort());
        }

        // NCL-8156 SECONDARY sort by unique column like long ID to achieve determinism
        comparing = thenCompareByLongID(comparing);
        sortInfo = sortInfo.thenOrderBy(DefaultOrderInfo.desc(BuildRecord_.id));

        List<Build> resultList = buildFetcher.getBuildPage(
                pageInfo.getPageIndex(),
                pageInfo.getPageSize(),
                runningBuilds,
                predicates,
                sortInfo,
                comparing);

        int hits = repository.count(predicates) + runningBuilds.size();

        return new Page<>(
                pageInfo.getPageIndex(),
                pageInfo.getPageSize(),
                (int) Math.ceil((double) hits / pageInfo.getPageSize()),
                hits,
                resultList);
    }

    private Comparator<Build> thenCompareByLongID(Comparator<Build> comparing) {
        ToLongFunction<Build> longId = build -> parseId(build.getId()).getLongId();
        Comparator<Build> sortByLongId = Comparator.comparingLong(longId).reversed(); // id DESC

        return comparing.thenComparing(sortByLongId);
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

    private List<Build> readRunningBuilds(BuildPageInfo pageInfo, java.util.function.Predicate<BuildTask> predicate)
            throws RemoteRequestException, MissingDataException {
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

        // NCL-8156 SECONDARY sort by unique column like long ID to achieve determinism
        comparing = thenCompareByLongID(comparing);

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

    private Optional<Build> readLatestRunningBuild(String query, java.util.function.Predicate<BuildTask> predicate)
            throws RemoteRequestException, MissingDataException {
        java.util.function.Predicate<Build> streamPredicate;
        if (StringUtils.isEmpty(query)) {
            streamPredicate = (f) -> true;
        } else {
            streamPredicate = rsqlPredicateProducer.getStreamPredicate(query);
        }

        Comparator<Build> comparator = Comparator.comparing(Build::getSubmitTime)
                .thenComparingLong(build -> parseId(build.getId()).getLongId());

        return nullableStreamOf(buildCoordinator.getSubmittedBuildTasks()).filter(Objects::nonNull)
                .filter(predicate)
                .map(buildMapper::fromBuildTask)
                .filter(streamPredicate)
                .max(comparator); // submitTime DESC, id DESC
    }

    private Optional<Build> readLatestFinishedBuild(String query, Predicate<BuildRecord> predicate) {
        Predicate<BuildRecord> criteriaPredicate = (_r, _q, cb) -> cb.conjunction();
        if (!StringUtils.isEmpty(query)) {
            criteriaPredicate = rsqlPredicateProducer.getCriteriaPredicate(BuildRecord.class, query);
        }

        PageInfo pageInfo = this.pageInfoProducer.getPageInfo(0, 1);
        SortInfo<BuildRecord> sortInfo = DefaultSortInfo.desc(BuildRecord_.submitTime, BuildRecord_.id);
        List<BuildRecord> buildRecords = repository
                .queryWithPredicates(pageInfo, sortInfo, criteriaPredicate, predicate);

        return buildRecords.stream().map(mapper::toDTO).findFirst();
    }

}
