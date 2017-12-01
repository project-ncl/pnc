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
package org.jboss.pnc.managers;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.maven.Gav;
import org.jboss.pnc.managers.causeway.CausewayClient;
import org.jboss.pnc.managers.causeway.remotespi.Build;
import org.jboss.pnc.managers.causeway.remotespi.BuildImportRequest;
import org.jboss.pnc.managers.causeway.remotespi.BuildRoot;
import org.jboss.pnc.managers.causeway.remotespi.BuiltArtifact;
import org.jboss.pnc.managers.causeway.remotespi.CallbackMethod;
import org.jboss.pnc.managers.causeway.remotespi.CallbackTarget;
import org.jboss.pnc.managers.causeway.remotespi.Dependency;
import org.jboss.pnc.managers.causeway.remotespi.Logfile;
import org.jboss.pnc.managers.causeway.remotespi.MavenBuild;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Stateless
public class BuildResultPushManager {

    private BuildRecordRepository buildRecordRepository;
    private BuildRecordPushResultRepository buildRecordPushResultRepository;

    private InProgress inProgress;

    private CausewayClient causewayClient;

    private Event<BuildRecordPushResultRest> buildRecordPushResultRestEvent;

    private Logger logger = LoggerFactory.getLogger(BuildResultPushManager.class);

    private static final String PNC_BUILD_RECORD_PATH = "/pnc-rest/rest/build-records/%d";
    private static final String PNC_BUILD_LOG_PATH = "/pnc-rest/rest/build-records/%d/log";
    private static final String PNC_REPOUR_LOG_PATH = "/pnc-rest/rest/build-records/%d/repour-log";

    @Deprecated //required by EJB
    public BuildResultPushManager() {
    }

    @Inject
    public BuildResultPushManager(BuildRecordRepository buildRecordRepository,
            BuildRecordPushResultRepository buildRecordPushResultRepository,
            InProgress inProgress,
            CausewayClient causewayClient,
            Event<BuildRecordPushResultRest> buildRecordPushResultRestEvent) {
        this.buildRecordRepository = buildRecordRepository;
        this.buildRecordPushResultRepository = buildRecordPushResultRepository;
        this.inProgress = inProgress;
        this.causewayClient = causewayClient;
        this.buildRecordPushResultRestEvent = buildRecordPushResultRestEvent;
    }

    /**
     *
     * @param buildRecordIds
     * @param authToken
     * @param callBackUrlTemplate %d in the template will be replaced with BuildRecord.id
     * @param tagPrefix
     * @return
     * @throws ProcessException
     */
    public Map<Integer, Boolean> push(
            Set<Integer> buildRecordIds,
            String authToken,
            String callBackUrlTemplate, String tagPrefix) throws ProcessException {

        Map<Integer, Boolean> result = new HashMap<>();
        for (Integer buildRecordId : buildRecordIds) {
            boolean success = pushToCauseway(
                    authToken, buildRecordId,
                    String.format(callBackUrlTemplate, buildRecordId),
                    tagPrefix);
            result.put(buildRecordId, success);
        }
        return result;
    }

    private boolean pushToCauseway(String authToken, Integer buildRecordId, String callBackUrl, String tagPrefix) throws ProcessException {
        if (!inProgress.add(buildRecordId)) {
            logger.warn("Push for BR.id {} already running.", buildRecordId);
            return false;
        }

        BuildRecord buildRecord = buildRecordRepository.findByIdFetchProperties(buildRecordId);
        if (buildRecord == null) {
            logger.warn("Did not find build record by id: " + buildRecordId);
            //TODO response with failure description
            return false;
        }

        BuildImportRequest buildImportRequest = createCausewayPushRequest(buildRecord, tagPrefix, callBackUrl);
        String jsonMessage = JsonOutputConverterMapper.apply(buildImportRequest);

        boolean successfullyPushed = causewayClient.push(jsonMessage, authToken);
        if (!successfullyPushed) {
            inProgress.remove(buildRecordId);
        }
        return successfullyPushed;
    }

    private BuildImportRequest createCausewayPushRequest(BuildRecord buildRecord, String tagPrefix, String callBackUrl) {
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

        Set<Dependency> dependencies = collectDependencies(buildRecord.getDependencies());
        Set<BuiltArtifact> builtArtifacts = collectBuiltArtifacts(buildRecord.getBuiltArtifacts());

        CallbackTarget callbackTarget = new CallbackTarget(callBackUrl, CallbackMethod.POST);
        ProjectVersionRef projectVersionRef = buildRootToGAV(
                buildRecord.getExecutionRootName(),
                buildRecord.getExecutionRootVersion());
        Set<Logfile> logs = new HashSet<>();
        logs.add(new Logfile("build.log", getBuildLogPath(buildRecord.getId()), buildRecord.getBuildLogSize(), buildRecord.getBuildLogMd5()));
        logs.add(new Logfile("repour.log", getRepourLogPath(buildRecord.getId()), buildRecord.getRepourLogSize(), buildRecord.getRepourLogMd5()));

        Build build = new MavenBuild(
                projectVersionRef.getGroupId(),
                projectVersionRef.getArtifactId(),
                projectVersionRef.getVersionString(),
                buildRecord.getExecutionRootName(),
                buildRecord.getExecutionRootVersion(),
                "PNC",
                buildRecord.getId(),
                String.format(PNC_BUILD_RECORD_PATH, buildRecord.getId()),
                buildRecord.getStartTime(),
                buildRecord.getEndTime(),
                buildRecord.getScmRepoURL(),
                buildRecord.getScmRevision(),
                buildRoot,
                logs,
                dependencies,
                builtArtifacts,
                tagPrefix
        );

        return new BuildImportRequest(callbackTarget, build);
    }

    private String getRepourLogPath(Integer id) {
        return String.format(PNC_REPOUR_LOG_PATH, id);
    }

    private String getBuildLogPath(Integer id) {
        return String.format(PNC_BUILD_LOG_PATH, id);
    }

    private ProjectVersionRef buildRootToGAV(String executionRootName, String executionRootVersion) {
        String[] splittedName = executionRootName.split(":");
        if(splittedName.length != 2)
            throw new IllegalArgumentException("Execution root '" + executionRootName + "' doesnt seem to be maven G:A.");

        return new SimpleProjectVersionRef(
                splittedName[0],
                splittedName.length < 2 ? null : splittedName[1],
                executionRootVersion);
    }

    private Set<BuiltArtifact> collectBuiltArtifacts(Set<Artifact> builtArtifacts) {
        return builtArtifacts.stream().map(artifact -> {
                Gav gav = Gav.parse(artifact.getIdentifier());
                return new BuiltArtifact(
                        artifact.getId(),
                        artifact.getFilename(),
                        artifact.getTargetRepository().getRepositoryType().toString(),
                        artifact.getMd5(),
                        getDeployUrl(artifact),
                        artifact.getSize().intValue()
                        );
                })
            .collect(Collectors.toSet());
    }

    private String getDeployUrl(Artifact artifact) {
        return artifact.getDeployPath();
    }

    private Set<Dependency> collectDependencies(Set<Artifact> dependencies) {
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
        if (!inProgress.remove(buildRecordId)) {
            throw new ProcessException("Did not find the referenced element.");
        }

        BuildRecordPushResult saved = buildRecordPushResultRepository.save(buildRecordPushResult);

        buildRecordPushResultRestEvent.fire(new BuildRecordPushResultRest(saved));
        return saved.getId();
    }

    public boolean cancelInProgressPush(Integer buildRecordId) {
        BuildRecordPushResultRest buildRecordPushResultRest = BuildRecordPushResultRest.builder()
                .status(BuildRecordPushResult.Status.CANCELED)
                .buildRecordId(buildRecordId)
                .log("Canceled.")
                .build();
        boolean canceled = inProgress.remove(buildRecordId);
        buildRecordPushResultRestEvent.fire(buildRecordPushResultRest);
        return canceled;
    }

    public Set<Integer> getInProgress() {
        return inProgress.getAll();
    }
}
