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
package org.jboss.pnc.bpm.causeway;

import org.commonjava.atlas.npm.ident.ref.NpmPackageRef;
import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.api.causeway.dto.push.Build;
import org.jboss.pnc.api.causeway.dto.push.BuildImportRequest;
import org.jboss.pnc.api.causeway.dto.push.BuildRoot;
import org.jboss.pnc.api.causeway.dto.push.BuiltArtifact;
import org.jboss.pnc.api.causeway.dto.push.Dependency;
import org.jboss.pnc.api.causeway.dto.push.Logfile;
import org.jboss.pnc.api.causeway.dto.push.MavenBuild;
import org.jboss.pnc.api.causeway.dto.push.MavenBuiltArtifact;
import org.jboss.pnc.api.causeway.dto.push.NpmBuild;
import org.jboss.pnc.api.causeway.dto.push.NpmBuiltArtifact;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.bpm.InvalidReferenceException;
import org.jboss.pnc.bpm.MissingInternalReferenceException;
import org.jboss.pnc.causewayclient.CausewayClient;
import org.jboss.pnc.common.gerrit.Gerrit;
import org.jboss.pnc.common.gerrit.GerritException;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.maven.Gav;
import org.jboss.pnc.constants.MDCKeys;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.mapper.api.BuildPushResultMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Stateless
public class BuildResultPushManager {

    private static final String PNC_BUILD_RECORD_PATH = "/pnc-rest/v2/builds/%d";
    private static final String PNC_BUILD_LOG_PATH = "/pnc-rest/v2/builds/%d/logs/build";
    private static final String PNC_REPOUR_LOG_PATH = "/pnc-rest/v2/builds/%d/logs/align";

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BuildRecordPushResultRepository buildRecordPushResultRepository;
    private ArtifactRepository artifactRepository;
    private BuildPushResultMapper mapper;

    private InProgress inProgress;

    private CausewayClient causewayClient;

    private Event<BuildPushResult> buildPushResultEvent;

    private Gerrit gerrit;

    private Logger logger = LoggerFactory.getLogger(BuildResultPushManager.class);

    @Deprecated // required by EJB
    public BuildResultPushManager() {
    }

    @Inject
    public BuildResultPushManager(
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            BuildRecordPushResultRepository buildRecordPushResultRepository,
            BuildPushResultMapper mapper,
            InProgress inProgress,
            Event<BuildPushResult> buildPushResultEvent,
            ArtifactRepository artifactRepository,
            Gerrit gerrit,
            CausewayClient causewayClient) {
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildRecordPushResultRepository = buildRecordPushResultRepository;
        this.mapper = mapper;
        this.inProgress = inProgress;
        this.buildPushResultEvent = buildPushResultEvent;
        this.artifactRepository = artifactRepository;
        this.gerrit = gerrit;
        this.causewayClient = causewayClient;
    }

    public Result push(BuildPushOperation buildPushOperation, String authToken) {
        logger.info("Pushing to causeway {}", buildPushOperation.toString());
        boolean added = inProgress.add(
                buildPushOperation.getBuildRecord().getId(),
                buildPushOperation.getTagPrefix(),
                buildPushOperation.getPushResultId().toString());
        if (!added) {
            logger.warn("Push for build.id {} already running.", buildPushOperation.getBuildRecord().getId());
            return new Result(
                    buildPushOperation.getPushResultId().toString(),
                    buildPushOperation.getBuildRecord().getId().toString(),
                    BuildPushStatus.REJECTED,
                    "A push for this buildRecord is already running.");
        }

        BuildPushStatus pushStatus;
        String message;
        try {
            BuildImportRequest buildImportRequest = createCausewayPushRequest(
                    buildPushOperation.getBuildRecord(),
                    buildPushOperation.getTagPrefix(),
                    URI.create(
                            String.format(
                                    buildPushOperation.getCompleteCallbackUrlTemplate(),
                                    buildPushOperation.getBuildRecord().getId())),
                    authToken,
                    buildPushOperation.getPushResultId(),
                    buildPushOperation.isReImport());
            boolean successfullyStarted = causewayClient.importBuild(buildImportRequest, authToken);
            if (successfullyStarted) {
                pushStatus = BuildPushStatus.ACCEPTED;
                message = "";
            } else {
                pushStatus = BuildPushStatus.SYSTEM_ERROR;
                message = "Failed to push to Causeway.";
            }
        } catch (RuntimeException ex) {
            logger.error("Failed to push to Causeway.", ex);
            pushStatus = BuildPushStatus.SYSTEM_ERROR;
            message = "Failed to push to Causeway: " + ex.getMessage();
        }

        if (!BuildPushStatus.ACCEPTED.equals(pushStatus)) {
            inProgress.remove(buildPushOperation.getBuildRecord().getId());
        }
        return new Result(
                buildPushOperation.getPushResultId().toString(),
                buildPushOperation.getBuildRecord().getId().toString(),
                pushStatus,
                message);
    }

    private BuildImportRequest createCausewayPushRequest(
            BuildRecord buildRecord,
            String tagPrefix,
            URI callBackUrl,
            String authToken,
            Long pushResultId,
            boolean reimport) {
        BuildEnvironment buildEnvironment = buildRecord.getBuildConfigurationAudited().getBuildEnvironment();
        logger.debug("BuildRecord: {}", buildRecord.getId());
        logger.debug("BuildEnvironment: {}", buildEnvironment);

        BuildRoot buildRoot = BuildRoot.builder()
                .container("DOCKER_IMAGE")
                .containerArchitecture("x86_64") // TODO set based on env, some env has native build tools
                .host("rhel")
                .hostArchitecture("x86_64")
                .tools(buildEnvironment.getAttributes())
                .build();

        List<Artifact> builtArtifactEntities = artifactRepository
                .queryWithPredicates(ArtifactPredicates.withBuildRecordId(buildRecord.getId()));
        List<Artifact> dependencyEntities = artifactRepository
                .queryWithPredicates(ArtifactPredicates.withDependantBuildRecordId(buildRecord.getId()));

        logger.debug(
                "Preparing BuildImportRequest containing {} built artifacts and {} dependencies.",
                builtArtifactEntities.size(),
                dependencyEntities.size());

        BuildType buildType = buildRecord.getBuildConfigurationAudited().getBuildType();

        Set<Dependency> dependencies = collectDependencies(dependencyEntities);
        Set<BuiltArtifact> builtArtifacts = collectBuiltArtifacts(builtArtifactEntities, buildType);

        Map<String, String> callbackHeaders = Collections.singletonMap(
                MDCUtils.getMDCToHeaderMappings().get(MDCKeys.PROCESS_CONTEXT_KEY),
                pushResultId.toString());
        Request callbackTarget = Request.builder()
                .method(Request.Method.POST)
                .authTokenHeader(authToken)
                .uri(callBackUrl)
                .header(MDCUtils.getMDCToHeaderMappings().get(MDCKeys.PROCESS_CONTEXT_KEY), pushResultId.toString())
                .build();

        String executionRootName = null;
        // prefer execution root name from generic parameters
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                .queryById(buildRecord.getBuildConfigurationAuditedIdRev());
        Map<String, String> genericParameters = buildConfigurationAudited.getGenericParameters();
        String brewBuildNameKey = BuildConfigurationParameterKeys.BREW_BUILD_NAME.name();
        if (genericParameters.containsKey(brewBuildNameKey)) {
            executionRootName = genericParameters.get(brewBuildNameKey);
        }
        if (executionRootName == null) {
            executionRootName = buildRecord.getExecutionRootName();
        }

        Build build = getBuild(
                buildRecord,
                tagPrefix,
                buildRoot,
                dependencies,
                builtArtifacts,
                executionRootName,
                buildType);

        return new BuildImportRequest(callbackTarget, build, reimport);
    }

    private Build getBuild(
            BuildRecord buildRecord,
            String tagPrefix,
            BuildRoot buildRoot,
            Set<Dependency> dependencies,
            Set<BuiltArtifact> builtArtifacts,
            String executionRootName,
            BuildType buildType) {
        switch (buildType) {
            case MVN:
            case GRADLE:
                return getMavenBuild(
                        buildRecord,
                        tagPrefix,
                        buildRoot,
                        dependencies,
                        builtArtifacts,
                        executionRootName);
            case NPM:
                return getNpmBuild(buildRecord, tagPrefix, buildRoot, dependencies, builtArtifacts, executionRootName);
            default:
                throw new IllegalArgumentException("Unknown buildType: " + buildType);
        }
    }

    private String getSourcesUrl(BuildRecord buildRecord) {
        try {
            return gerrit
                    .generateDownloadUrlWithGerritGitweb(buildRecord.getScmRepoURL(), buildRecord.getScmRevision());
        } catch (GerritException e) {
            throw new RuntimeException("Failed to get SCM url from gerrit", e);
        }
    }

    private Build getMavenBuild(
            BuildRecord buildRecord,
            String tagPrefix,
            BuildRoot buildRoot,
            Set<Dependency> dependencies,
            Set<BuiltArtifact> builtArtifacts,
            String executionRootName) {
        Gav rootGav = buildRootToGAV(executionRootName, buildRecord.getExecutionRootVersion());
        Set<Logfile> logs = new HashSet<>();

        addLogs(buildRecord, logs);

        return MavenBuild.builder()
                .groupId(rootGav.getGroupId())
                .artifactId(rootGav.getArtifactId())
                .version(rootGav.getVersion())
                .buildName(executionRootName)
                .buildVersion(buildRecord.getExecutionRootVersion())
                .externalBuildSystem("PNC")
                .externalBuildID(buildRecord.getId().toString())
                .externalBuildURL(String.format(PNC_BUILD_RECORD_PATH, buildRecord.getId()))
                .startTime(buildRecord.getStartTime())
                .endTime(buildRecord.getEndTime())
                .scmURL(buildRecord.getScmRepoURL())
                .scmRevision(buildRecord.getScmRevision())
                .scmTag(buildRecord.getScmTag())
                .buildRoot(buildRoot)
                .logs(logs)
                .sourcesURL(getSourcesUrl(buildRecord))
                .dependencies(dependencies)
                .builtArtifacts(builtArtifacts)
                .tagPrefix(tagPrefix)
                .build();
    }

    private Build getNpmBuild(
            BuildRecord buildRecord,
            String tagPrefix,
            BuildRoot buildRoot,
            Set<Dependency> dependencies,
            Set<BuiltArtifact> builtArtifacts,
            String executionRootName) {
        NpmPackageRef nv = new NpmPackageRef(executionRootName, buildRecord.getExecutionRootVersion());
        Set<Logfile> logs = new HashSet<>();

        addLogs(buildRecord, logs);

        return NpmBuild.builder()
                .name(nv.getName())
                .version(nv.getVersionString())
                .buildName(executionRootName)
                .buildVersion(buildRecord.getExecutionRootVersion())
                .externalBuildSystem("PNC")
                .externalBuildID(buildRecord.getId().toString())
                .externalBuildURL(String.format(PNC_BUILD_RECORD_PATH, buildRecord.getId()))
                .startTime(buildRecord.getStartTime())
                .endTime(buildRecord.getEndTime())
                .scmURL(buildRecord.getScmRepoURL())
                .scmRevision(buildRecord.getScmRevision())
                .scmTag(buildRecord.getScmTag())
                .buildRoot(buildRoot)
                .logs(logs)
                .sourcesURL(getSourcesUrl(buildRecord))
                .dependencies(dependencies)
                .builtArtifacts(builtArtifacts)
                .tagPrefix(tagPrefix)
                .build();
    }

    private void addLogs(BuildRecord buildRecord, Set<Logfile> logs) {
        if (buildRecord.getBuildLogSize() != null && buildRecord.getBuildLogSize() > 0) {
            logs.add(
                    Logfile.builder()
                            .filename("build.log")
                            .deployPath(getBuildLogPath(buildRecord.getId()))
                            .size(buildRecord.getBuildLogSize())
                            .md5(buildRecord.getBuildLogMd5())
                            .build());
        } else {
            logger.warn("Missing build log for BR.id: {}.", buildRecord.getId());
        }
        if (buildRecord.getRepourLogSize() != null && buildRecord.getRepourLogSize() > 0) {
            logs.add(
                    Logfile.builder()
                            .filename("repour.log")
                            .deployPath(getRepourLogPath(buildRecord.getId()))
                            .size(buildRecord.getRepourLogSize())
                            .md5(buildRecord.getRepourLogMd5())
                            .build());
        } else {
            logger.warn("Missing repour log for BR.id: {}.", buildRecord.getId());
        }
        // TODO respond with error if logs are missing
    }

    private String getRepourLogPath(Long id) {
        return String.format(PNC_REPOUR_LOG_PATH, id);
    }

    private String getBuildLogPath(Long id) {
        return String.format(PNC_BUILD_LOG_PATH, id);
    }

    private Gav buildRootToGAV(String executionRootName, String executionRootVersion) {
        if (executionRootName == null) {
            throw new IllegalArgumentException("ExecutionRootName must be defined.");
        }

        String[] splittedName = executionRootName.split(":");
        if (splittedName.length != 2) {
            throw new IllegalArgumentException(
                    "Execution root '" + executionRootName + "' doesn't seem to be maven G:A.");
        }
        return new Gav(splittedName[0], splittedName[1], executionRootVersion);
    }

    private Set<BuiltArtifact> collectBuiltArtifacts(Collection<Artifact> builtArtifacts, BuildType buildType) {
        switch (buildType) {
            case MVN:
            case GRADLE:
                return getMavenArtifacts(builtArtifacts);
            case NPM:
                return getNpmArtifacts(builtArtifacts);
            default:
                throw new IllegalArgumentException("Unknown buildType: " + buildType);
        }
    }

    private Set<BuiltArtifact> getNpmArtifacts(Collection<Artifact> builtArtifacts) {
        return builtArtifacts.stream().map(artifact -> {
            NpmPackageRef nv = NpmPackageRef.parse(artifact.getIdentifier());
            return new NpmBuiltArtifact(
                    nv.getName(),
                    nv.getVersionString(),
                    artifact.getId(),
                    artifact.getFilename(),
                    artifact.getTargetRepository().getRepositoryType().toString(),
                    artifact.getMd5(),
                    artifact.getDeployPath(),
                    artifact.getTargetRepository().getRepositoryPath(),
                    artifact.getSize().intValue());
        }).collect(Collectors.toSet());
    }

    private Set<BuiltArtifact> getMavenArtifacts(Collection<Artifact> builtArtifacts) {
        return builtArtifacts.stream().map(artifact -> {
            Gav gav = Gav.parse(artifact.getIdentifier());
            return new MavenBuiltArtifact(
                    gav.getGroupId(),
                    gav.getArtifactId(),
                    gav.getVersion(),
                    artifact.getId(),
                    artifact.getFilename(),
                    artifact.getTargetRepository().getRepositoryType().toString(),
                    artifact.getMd5(),
                    artifact.getDeployPath(),
                    artifact.getTargetRepository().getRepositoryPath(),
                    artifact.getSize().intValue());
        }).collect(Collectors.toSet());
    }

    private Set<Dependency> collectDependencies(Collection<Artifact> dependencies) {
        return dependencies.stream()
                .map(artifact -> new Dependency(artifact.getFilename(), artifact.getMd5(), artifact.getSize()))
                .collect(Collectors.toSet());
    }

    public Long complete(Long buildRecordId, BuildRecordPushResult buildRecordPushResult) {
        // accept only listed elements otherwise a new request might be wrongly completed from response of an older one
        // get context for validation
        InProgress.Context pushContext = inProgress.get(buildRecordId);
        if (pushContext == null) {
            throw new MissingInternalReferenceException("Did not find referenced element.");
        }
        Long expectedPushResultId = BuildPushResultMapper.idMapper.toEntity(pushContext.getPushResultId());
        // if the result id is set it must match the id generated when the remote operation has been triggered
        if (buildRecordPushResult.getId() != null && !buildRecordPushResult.getId().equals(expectedPushResultId)) {
            throw new InvalidReferenceException("Unexpected result id: " + buildRecordPushResult.getId());
        }
        // get and remove the context atomically
        pushContext = inProgress.remove(buildRecordId);
        if (pushContext == null) {
            throw new MissingInternalReferenceException("Referenced element has gone.");
        }
        buildRecordPushResult.setId(expectedPushResultId);
        buildRecordPushResult.setTagPrefix(pushContext.getTagPrefix());
        BuildRecordPushResult saved = buildRecordPushResultRepository.save(buildRecordPushResult);
        buildPushResultEvent.fire(mapper.toDTO(saved));
        return saved.getId();
    }

    public boolean cancelInProgressPush(Long buildRecordId) {
        InProgress.Context pushContext = inProgress.remove(buildRecordId);
        BuildPushResult buildRecordPushResultRest = BuildPushResult.builder()
                .status(BuildPushStatus.CANCELED)
                .buildId(buildRecordId.toString())
                .build();
        buildPushResultEvent.fire(buildRecordPushResultRest);
        return pushContext != null;
    }

    public Optional<InProgress.Context> getContext(long buildId) {
        return inProgress.getAll().stream().filter(c -> c.getId().equals(buildId)).findAny();
    }
}
