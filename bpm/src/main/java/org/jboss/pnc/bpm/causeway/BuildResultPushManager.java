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
package org.jboss.pnc.bpm.causeway;

import org.commonjava.atlas.npm.ident.ref.NpmPackageRef;
import org.jboss.pnc.api.bifrost.rest.FinalLogRest;
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
import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.bpm.InvalidReferenceException;
import org.jboss.pnc.bpm.MissingInternalReferenceException;
import org.jboss.pnc.causewayclient.CausewayClient;
import org.jboss.pnc.common.scm.ScmUrlGeneratorProvider;
import org.jboss.pnc.common.scm.ScmException;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.maven.Gav;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.BuildPushResultMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.api.constants.Attributes.BUILD_BREW_NAME;
import static org.jboss.pnc.api.constants.BuildConfigurationParameterKeys.BREW_BUILD_NAME;
import static org.jboss.pnc.common.scm.ScmUrlGeneratorProvider.determineScmProvider;
import static org.jboss.pnc.common.scm.ScmUrlGeneratorProvider.getScmUrlGenerator;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Stateless
public class BuildResultPushManager {

    private static final String PNC_BUILD_RECORD_PATH = "/pnc-rest/v2/builds/%s";
    private static final String PNC_BUILD_LOG_PATH = "/pnc-rest/v2/builds/%s/logs/build";
    private static final String PNC_REPOUR_LOG_PATH = "/pnc-rest/v2/builds/%s/logs/align";

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BuildRecordPushResultRepository buildRecordPushResultRepository;
    private ArtifactRepository artifactRepository;
    private BuildPushResultMapper mapper;

    private InProgress inProgress;

    private CausewayClient causewayClient;

    private Event<BuildPushResult> buildPushResultEvent;

    private FinalLogRest finalLog;

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
            CausewayClient causewayClient,
            FinalLogRest finalLog) {
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildRecordPushResultRepository = buildRecordPushResultRepository;
        this.mapper = mapper;
        this.inProgress = inProgress;
        this.buildPushResultEvent = buildPushResultEvent;
        this.artifactRepository = artifactRepository;
        this.causewayClient = causewayClient;
        this.finalLog = finalLog;
    }

    public Result push(BuildPushOperation buildPushOperation, String authToken) {
        logger.info("Pushing to causeway {}", buildPushOperation.toString());
        boolean added = inProgress.add(
                buildPushOperation.getBuildRecord().getId(),
                buildPushOperation.getTagPrefix(),
                buildPushOperation.getPushResultId().toString());
        String externalBuildId = BuildMapper.idMapper.toDto(buildPushOperation.getBuildRecord().getId());
        if (!added) {
            logger.warn("Push for build.id {} already running.", externalBuildId);
            return new Result(
                    buildPushOperation.getPushResultId().toString(),
                    externalBuildId,
                    BuildPushStatus.REJECTED,
                    "A push for this buildRecord is already running.");
        }

        BuildPushStatus pushStatus;
        String message;
        try {
            BuildImportRequest buildImportRequest = createCausewayPushRequest(
                    buildPushOperation.getBuildRecord(),
                    buildPushOperation.getTagPrefix(),
                    URI.create(String.format(buildPushOperation.getCompleteCallbackUrlTemplate(), externalBuildId)),
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
        return new Result(buildPushOperation.getPushResultId().toString(), externalBuildId, pushStatus, message);
    }

    private BuildImportRequest createCausewayPushRequest(
            BuildRecord buildRecord,
            String tagPrefix,
            URI callBackUrl,
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

        Request callbackTarget = Request.builder()
                .method(Request.Method.POST)
                .uri(callBackUrl)
                .header(MDCUtils.HEADER_KEY_MAPPING.get(MDCKeys.PROCESS_CONTEXT_KEY), pushResultId.toString())
                .build();

        String executionRootName = buildRecord.getExecutionRootName();
        // prefer execution root name from generic parameters
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                .queryById(buildRecord.getBuildConfigurationAuditedIdRev());
        Map<String, String> genericParameters = buildConfigurationAudited.getGenericParameters();
        if (executionRootName == null) {
            if (genericParameters.containsKey(BREW_BUILD_NAME.name())) {
                executionRootName = genericParameters.get(BREW_BUILD_NAME.name());
            } else {
                throw new IllegalArgumentException(
                        "Provided build " + buildRecord.getId() + " is missing brew name. Please set build attribute "
                                + BUILD_BREW_NAME + ".");
            }
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
            var provider = determineScmProvider(
                    buildRecord.getScmRepoURL(),
                    buildRecord.getBuildConfigurationAudited().getRepositoryConfiguration().getInternalUrl());
            return getScmUrlGenerator(provider)
                    .generateDownloadUrlWithGitweb(buildRecord.getScmRepoURL(), buildRecord.getScmRevision());
        } catch (ScmException e) {
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

        String externalBuildID = BuildMapper.idMapper.toDto(buildRecord.getId());
        return MavenBuild.builder()
                .groupId(rootGav.getGroupId())
                .artifactId(rootGav.getArtifactId())
                .version(rootGav.getVersion())
                .buildName(executionRootName)
                .buildVersion(buildRecord.getExecutionRootVersion())
                .externalBuildSystem("PNC")
                .externalBuildID(externalBuildID)
                .externalBuildURL(String.format(PNC_BUILD_RECORD_PATH, externalBuildID))
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

        String externalBuildID = buildRecord.getId().toString();
        return NpmBuild.builder()
                .name(nv.getName())
                .version(nv.getVersionString())
                .buildName(executionRootName)
                .buildVersion(buildRecord.getExecutionRootVersion())
                .externalBuildSystem("PNC")
                .externalBuildID(externalBuildID)
                .externalBuildURL(String.format(PNC_BUILD_RECORD_PATH, externalBuildID))
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
        String externalBuildID = buildRecord.getId().toString();
        long buildLogSize = finalLog.getFinalLogSize(buildRecord.getId().toString(), "build-log");
        if (buildLogSize > 0) {
            logs.add(
                    Logfile.builder()
                            .filename("build.log")
                            .deployPath(getBuildLogPath(externalBuildID))
                            .size((int) buildLogSize)
                            .md5(buildRecord.getbuildOutputChecksum())
                            .build());
        } else {
            logger.warn("Missing build log for BR.id: {}.", externalBuildID);
        }
        long alignmentLogSize = finalLog.getFinalLogSize(buildRecord.getId().toString(), "alignment-log");
        if (alignmentLogSize > 0) {
            logs.add(
                    Logfile.builder()
                            .filename("repour.log")
                            .deployPath(getRepourLogPath(externalBuildID))
                            .size((int) alignmentLogSize)
                            .md5(buildRecord.getbuildOutputChecksum())
                            .build());
        } else {
            logger.warn("Missing repour log for BR.id: {}.", externalBuildID);
        }
        // TODO respond with error if logs are missing
    }

    private String getRepourLogPath(String id) {
        return String.format(PNC_REPOUR_LOG_PATH, id);
    }

    private String getBuildLogPath(String id) {
        return String.format(PNC_BUILD_LOG_PATH, id);
    }

    private Gav buildRootToGAV(String brewBuildName, String executionRootVersion) {
        if (brewBuildName == null) {
            throw new IllegalArgumentException("Build attribute " + BREW_BUILD_NAME + " can't be missing");
        }

        String[] splittedName = brewBuildName.split(":");
        if (splittedName.length != 2) {
            throw new IllegalArgumentException(
                    BREW_BUILD_NAME + " attribute '" + brewBuildName + "' doesn't seem to be maven G:A.");
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
                    ArtifactMapper.idMapper.toDto(artifact.getId()),
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
                    ArtifactMapper.idMapper.toDto(artifact.getId()),
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

    public Long complete(Base32LongID buildRecordId, BuildRecordPushResult buildRecordPushResult) {
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

    public boolean cancelInProgressPush(Base32LongID buildRecordId) {
        InProgress.Context pushContext = inProgress.remove(buildRecordId);
        BuildPushResult buildRecordPushResultRest = BuildPushResult.builder()
                .status(BuildPushStatus.CANCELED)
                .buildId(buildRecordId.toString())
                .build();
        buildPushResultEvent.fire(buildRecordPushResultRest);
        return pushContext != null;
    }

    public Optional<InProgress.Context> getContext(Base32LongID buildId) {
        return inProgress.getAll().stream().filter(c -> c.getId().equals(buildId)).findAny();
    }
}
