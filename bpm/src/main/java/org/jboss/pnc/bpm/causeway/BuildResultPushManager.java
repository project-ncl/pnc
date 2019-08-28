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
package org.jboss.pnc.bpm.causeway;

import org.commonjava.atlas.npm.ident.ref.NpmPackageRef;
import org.jboss.pnc.causewayclient.CausewayClient;
import org.jboss.pnc.causewayclient.remotespi.Build;
import org.jboss.pnc.causewayclient.remotespi.BuildImportRequest;
import org.jboss.pnc.causewayclient.remotespi.BuildRoot;
import org.jboss.pnc.causewayclient.remotespi.BuiltArtifact;
import org.jboss.pnc.causewayclient.remotespi.CallbackTarget;
import org.jboss.pnc.causewayclient.remotespi.Dependency;
import org.jboss.pnc.causewayclient.remotespi.Logfile;
import org.jboss.pnc.causewayclient.remotespi.MavenBuild;
import org.jboss.pnc.causewayclient.remotespi.MavenBuiltArtifact;
import org.jboss.pnc.causewayclient.remotespi.NpmBuild;
import org.jboss.pnc.causewayclient.remotespi.NpmBuiltArtifact;
import org.jboss.pnc.common.maven.Gav;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.mapper.api.BuildPushResultMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Stateless
public class BuildResultPushManager {

    /** Generic parameter name for overriding the executionRootName value received from Repour. */
    private static final String BREW_BUILD_NAME = "BREW_BUILD_NAME";

    private static final String PNC_BUILD_RECORD_PATH = "/pnc-rest/rest/build-records/%d";
    private static final String PNC_BUILD_LOG_PATH = "/pnc-rest/rest/build-records/%d/log";
    private static final String PNC_REPOUR_LOG_PATH = "/pnc-rest/rest/build-records/%d/repour-log";

    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildRecordRepository buildRecordRepository;
    private BuildRecordPushResultRepository buildRecordPushResultRepository;
    private ArtifactRepository artifactRepository;
    private BuildPushResultMapper mapper;

    private InProgress inProgress;

    private CausewayClient causewayClient;

    private Event<BuildPushResult> buildPushResultEvent;

    private Logger logger = LoggerFactory.getLogger(BuildResultPushManager.class);

    @Deprecated //required by EJB
    public BuildResultPushManager() {
    }

    @Inject
    public BuildResultPushManager(BuildConfigurationRepository buildConfigurationRepository,
            BuildRecordRepository buildRecordRepository, BuildRecordPushResultRepository buildRecordPushResultRepository,
            BuildPushResultMapper mapper, InProgress inProgress,
            Event<BuildPushResult> buildPushResultEvent, ArtifactRepository artifactRepository, CausewayClient causewayClient) {
        this.buildConfigurationRepository =  buildConfigurationRepository;
        this.buildRecordRepository = buildRecordRepository;
        this.buildRecordPushResultRepository = buildRecordPushResultRepository;
        this.mapper = mapper;
        this.inProgress = inProgress;
        this.buildPushResultEvent = buildPushResultEvent;
        this.artifactRepository = artifactRepository;
        this.causewayClient = causewayClient;
    }

    /**
     *
     * @param buildRecordIds
     * @param authToken
     * @param callBackUrlTemplate %d in the template will be replaced with BuildRecord.id
     * @param tagPrefix
     * @param reimport Wherather the build should be reimported with new revision number if it already exists in Brew
     * @return
     * @throws ProcessException
     */
    public Set<Result> push(
            Set<String> buildRecordIds,
            String authToken,
            String callBackUrlTemplate, String tagPrefix,
            boolean reimport) throws ProcessException {

        Set<Result> result = new HashSet<>();
        for (String buildRecordId : buildRecordIds) {
            //check is the status is NO_REBUILD_REQUIRED, if it is replace with the last BuildRecord with status SUCCESS for the same idRev.
            BuildRecord buildRecord = buildRecordRepository.queryById(Integer.valueOf(buildRecordId));
            Integer pushBuildRecordId = null;
            if (BuildStatus.NO_REBUILD_REQUIRED.equals(buildRecord.getStatus())) {
                IdRev idRev = buildRecord.getBuildConfigurationAuditedIdRev();
                buildRecord = buildRecordRepository.getLatestSuccessfulBuildRecord(idRev, buildRecord.isTemporaryBuild());
                if (buildRecord != null) {
                    pushBuildRecordId = buildRecord.getId();
                } else {
                    logger.warn("Trying to push a BuildRecord.id: {} with status NO_REBUILD_REQUIRED and there is no successful result for the configuration.idRev: {}.",
                            buildRecordId, idRev);
                }
            } else {
                pushBuildRecordId = Integer.valueOf(buildRecordId);
            }
            if (pushBuildRecordId != null) {
                Result pushResult = pushToCauseway(
                        authToken,
                        pushBuildRecordId,
                        String.format(callBackUrlTemplate, buildRecordId),
                        tagPrefix,
                        reimport);
                result.add(pushResult);
            }
        }
        return result;
    }

    private Result pushToCauseway(String authToken, Integer buildRecordId, String callBackUrl, String tagPrefix, boolean reimport) throws ProcessException {
        logger.info("Pushing to causeway BR.id: {}", buildRecordId);

        if (!inProgress.add(buildRecordId, tagPrefix)) {
            logger.warn("Push for BR.id {} already running.", buildRecordId);
            return new Result(buildRecordId.toString(), BuildPushStatus.REJECTED, "A push for this buildRecord is already running.");
        }

        BuildRecord buildRecord = buildRecordRepository.findByIdFetchProperties(buildRecordId);
        if (buildRecord == null) {
            logger.warn("Did not find build record by id: " + buildRecordId);
            return new Result(buildRecordId.toString(), BuildPushStatus.REJECTED, "Did not find build record by given id.");
        }

        if (!buildRecord.getStatus().completedSuccessfully()) {
            logger.warn("Not pushing record id: " + buildRecordId + " because it is a failed build.");
            return new Result(buildRecordId.toString(), BuildPushStatus.REJECTED, "Cannot push failed build.");
        }

        BuildImportRequest buildImportRequest = createCausewayPushRequest(buildRecord, tagPrefix, callBackUrl, authToken, reimport);
        boolean successfullyPushed = causewayClient.importBuild(buildImportRequest, authToken);
        if (!successfullyPushed) {
            inProgress.remove(buildRecordId);
            return new Result(buildRecordId.toString(), BuildPushStatus.REJECTED, "Failed to push to Causeway.");
        } else {
            return new Result(buildRecordId.toString(), BuildPushStatus.ACCEPTED);
        }

    }

    private BuildImportRequest createCausewayPushRequest(
            BuildRecord buildRecord,
            String tagPrefix,
            String callBackUrl,
            String authToken,
            boolean reimport) {
        BuildEnvironment buildEnvironment = buildRecord.getBuildConfigurationAudited().getBuildEnvironment();
        logger.debug("BuildRecord: {}", buildRecord.getId());
        logger.debug("BuildEnvironment: {}", buildEnvironment);

        BuildRoot buildRoot = new BuildRoot(
                "DOCKER_IMAGE",
                "x86_64", //TODO set based on env, some env has native build tools
                "rhel",
                "x86_64",
                 buildEnvironment.getAttributes()
        );

        List<Artifact> builtArtifactEntities = artifactRepository.queryWithPredicates(ArtifactPredicates.withBuildRecordId(buildRecord.getId()));
        List<Artifact> dependencyEntities = artifactRepository.queryWithPredicates(ArtifactPredicates.withDependantBuildRecordId(buildRecord.getId()));

        logger.debug("Preparing BuildImportRequest containing {} built artifacts and {} dependencies.", builtArtifactEntities.size(), dependencyEntities.size());

        BuildType buildType = buildRecord.getBuildConfigurationAudited().getBuildType();

        Set<Dependency> dependencies = collectDependencies(dependencyEntities);
        Set<BuiltArtifact> builtArtifacts = collectBuiltArtifacts(builtArtifactEntities, buildType);

        CallbackTarget callbackTarget = CallbackTarget.callbackPost(callBackUrl, authToken);

        String executionRootName = null;
        // prefer execution root name from generic parameters
        BuildConfiguration bc = buildConfigurationRepository.queryById(buildRecord.getBuildConfigurationId());
        Map<String, String> genericParameters = bc.getGenericParameters();
        if (genericParameters.containsKey(BREW_BUILD_NAME)) {
            executionRootName = genericParameters.get(BREW_BUILD_NAME);
        }
        if (executionRootName == null) {
            executionRootName = buildRecord.getExecutionRootName();
        }

        Build build = getBuild(buildRecord, tagPrefix, buildRoot, dependencies, builtArtifacts, executionRootName, buildType);

        return new BuildImportRequest(callbackTarget, build, reimport);
    }

    private Build getBuild(BuildRecord buildRecord, String tagPrefix, BuildRoot buildRoot, Set<Dependency> dependencies,
            Set<BuiltArtifact> builtArtifacts, String executionRootName, BuildType buildType) {
        switch (buildType) {
            case MVN:
            case GRADLE:
                return getMavenBuild(buildRecord, tagPrefix, buildRoot, dependencies, builtArtifacts, executionRootName);
            case NPM:
                return getNpmBuild(buildRecord, tagPrefix, buildRoot, dependencies, builtArtifacts, executionRootName);
            default:
                throw new IllegalArgumentException("Unknown buildType: " + buildType);
        }
    }

    private Build getMavenBuild(BuildRecord buildRecord, String tagPrefix, BuildRoot buildRoot, Set<Dependency> dependencies,
            Set<BuiltArtifact> builtArtifacts, String executionRootName) {
        Gav rootGav = buildRootToGAV(
                executionRootName,
                buildRecord.getExecutionRootVersion());
        Set<Logfile> logs = new HashSet<>();

        addLogs(buildRecord, logs);

        return new MavenBuild(
                rootGav.getGroupId(),
                rootGav.getArtifactId(),
                rootGav.getVersion(),
                executionRootName,
                buildRecord.getExecutionRootVersion(),
                "PNC",
                buildRecord.getId(),
                String.format(PNC_BUILD_RECORD_PATH, buildRecord.getId()),
                buildRecord.getStartTime(),
                buildRecord.getEndTime(),
                buildRecord.getScmRepoURL(),
                buildRecord.getScmRevision(),
                buildRecord.getScmTag(),
                buildRoot,
                logs,
                dependencies,
                builtArtifacts,
                tagPrefix
        );
    }

    private Build getNpmBuild(BuildRecord buildRecord, String tagPrefix, BuildRoot buildRoot, Set<Dependency> dependencies,
            Set<BuiltArtifact> builtArtifacts, String executionRootName) {
        NpmPackageRef nv = new NpmPackageRef(executionRootName, buildRecord.getExecutionRootVersion());
        Set<Logfile> logs = new HashSet<>();

        addLogs(buildRecord, logs);

        return new NpmBuild(
                nv.getName(),
                nv.getVersionString(),
                executionRootName,
                buildRecord.getExecutionRootVersion(),
                "PNC",
                buildRecord.getId(),
                String.format(PNC_BUILD_RECORD_PATH, buildRecord.getId()),
                buildRecord.getStartTime(),
                buildRecord.getEndTime(),
                buildRecord.getScmRepoURL(),
                buildRecord.getScmRevision(),
                buildRecord.getScmTag(),
                buildRoot,
                logs,
                dependencies,
                builtArtifacts,
                tagPrefix
        );
    }

    private void addLogs(BuildRecord buildRecord, Set<Logfile> logs) {
        if (buildRecord.getBuildLogSize() != null && buildRecord.getBuildLogSize() > 0) {
            logs.add(new Logfile("build.log", getBuildLogPath(buildRecord.getId()), buildRecord.getBuildLogSize(), buildRecord.getBuildLogMd5()));
        } else {
            logger.warn("Missing build log for BR.id: {}.", buildRecord.getId());
        }
        if (buildRecord.getRepourLogSize() != null && buildRecord.getRepourLogSize() > 0) {
            logs.add(new Logfile("repour.log", getRepourLogPath(buildRecord.getId()), buildRecord.getRepourLogSize(), buildRecord.getRepourLogMd5()));
        } else {
            logger.warn("Missing repour log for BR.id: {}.", buildRecord.getId());
        }
        //TODO respond with error if logs are missing
    }

    private String getRepourLogPath(Integer id) {
        return String.format(PNC_REPOUR_LOG_PATH, id);
    }

    private String getBuildLogPath(Integer id) {
        return String.format(PNC_BUILD_LOG_PATH, id);
    }

    private Gav buildRootToGAV(String executionRootName, String executionRootVersion) {
        if (executionRootName == null) {
            throw new IllegalArgumentException("ExecutionRootName must be defined.");
        }

        String[] splittedName = executionRootName.split(":");
        if(splittedName.length != 2) {
            throw new IllegalArgumentException("Execution root '" + executionRootName + "' doesn't seem to be maven G:A.");
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
                    artifact.getSize().intValue()
            );
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
                    artifact.getSize().intValue()
            );
        }).collect(Collectors.toSet());
    }

    private Set<Dependency> collectDependencies(Collection<Artifact> dependencies) {
        return dependencies.stream()
                .map(artifact -> new Dependency(
                        artifact.getFilename(),
                        artifact.getMd5(),
                        artifact.getSize())
                )
                .collect(Collectors.toSet());
    }

    public Integer complete(Integer buildRecordId, BuildRecordPushResult buildRecordPushResult) throws ProcessException {
        //accept only listed elements otherwise a new request might be wrongly completed from response of an older one
        String completedTag = inProgress.remove(buildRecordId);
        if (completedTag == null) {
            throw new ProcessException("Did not find the referenced element.");
        }

        buildRecordPushResult.setTagPrefix(completedTag);
        BuildRecordPushResult saved = buildRecordPushResultRepository.save(buildRecordPushResult);
        buildPushResultEvent.fire(mapper.toDTO(saved));
        return saved.getId();
    }

    public boolean cancelInProgressPush(Integer buildRecordId) {
        BuildPushResult buildRecordPushResultRest = BuildPushResult.builder()
                .status(BuildPushStatus.CANCELED)
                .buildId(Integer.toString(buildRecordId))
                .log("Canceled.")
                .build();
        boolean canceled = inProgress.remove(buildRecordId) != null;
        buildPushResultEvent.fire(buildRecordPushResultRest);
        return canceled;
    }

    public Set<Integer> getInProgress() {
        return inProgress.getAllIds();
    }
}
