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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.maven.Gav;
import org.jboss.pnc.managers.restmodel.CausewayPushRequest;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class BuildResultPushManager {

    private BuildRecordRepository buildRecordRepository;
    private BuildRecordPushResultRepository buildRecordPushResultRepository;

    private InProgress inProgress;
    private Notifier notifier;

    private Logger logger = LoggerFactory.getLogger(BuildResultPushManager.class);
    private String causewayEndpoint;
    private String indyBaseUrl;

    private static final String PNC_BUILD_RECORD_PATH = "/pnc-rest/rest/build-records/%d";

    @Inject
    public BuildResultPushManager(BuildRecordRepository buildRecordRepository,
            BuildRecordPushResultRepository buildRecordPushResultRepository,
            InProgress inProgress,
            Notifier notifier,
            Configuration configuration) {
        this.buildRecordRepository = buildRecordRepository;
        this.buildRecordPushResultRepository = buildRecordPushResultRepository;
        this.inProgress = inProgress;
        this.notifier = notifier;

        try {
            String causewayBaseUrl = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class))
                    .getCausewayBaseUrl();
            causewayEndpoint = causewayBaseUrl + "BR_PUSH_PATH"; //TODO set Causeway path
        } catch (ConfigurationParseException e) {
            logger.error("There is a problem while parsing system configuration. Using defaults.", e);
        }
    }

    /**
     *
     * @param buildRecordIds
     * @param authToken
     * @param callBackUrlTemplate %d in the template will be replaced with BuildRecord.id
     * @return
     * @throws ProcessException
     */
    public Map<Integer, Boolean> push(Set<Integer> buildRecordIds, String authToken, String callBackUrlTemplate) throws ProcessException {
        Map<Integer, Boolean> result = new HashMap<>();
        for (Integer buildRecordId : buildRecordIds) {
            boolean success = pushToCauseway(authToken, buildRecordId, String.format(callBackUrlTemplate, buildRecordId));
            result.put(buildRecordId, success);
        }
        return result;
    }

    private boolean pushToCauseway(String authToken, Integer buildRecordId, String callBackUrl) throws ProcessException {
        if (!inProgress.add(buildRecordId)) {
            throw new ProcessException("Push for this BR already running.");
        }

        BuildRecord buildRecord = buildRecordRepository.queryById(buildRecordId);

        Header authHeader = new BasicHeader("Authorization", authToken);
        Header callBackHeader = new BasicHeader("Completion-callback", callBackUrl);
        CausewayPushRequest causewayPushRequest = createCausewayPushRequest(buildRecord);

        String jsonMessage = causewayPushRequest.toString();

        try {
            HttpResponse response = Request.Post(causewayEndpoint)
                    .addHeader(authHeader)
                    .addHeader(callBackHeader)
                    .bodyString(jsonMessage, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                inProgress.remove(buildRecordId);
                logger.error("Trying to invoke remote Causeway push failed with http code {}.", statusCode);
                return false;
            }

        } catch (IOException e) {
            logger.error("Trying to invoke remote Causeway push failed.", e);
            inProgress.remove(buildRecordId);
            return false;
        }
        return true;
    }

    private CausewayPushRequest createCausewayPushRequest(BuildRecord buildRecord) {
        CausewayPushRequest.BuildRoot buildRoot = new CausewayPushRequest.BuildRoot(
                buildRecord.getBuildEnvironment().getAttributes()
        );

        Set<CausewayPushRequest.Dependency> dependencies = collectDependencies(buildRecord.getDependencies());
        Set<CausewayPushRequest.BuiltArtifact> builtArtifacts = collectBuiltArtifacts(buildRecord.getBuiltArtifacts());

        return new CausewayPushRequest(
                buildRecord.getExecutionRootName(),
                buildRecord.getExecutionRootVersion(),
                buildRecord.getId().toString(),
                String.format(PNC_BUILD_RECORD_PATH, buildRecord.getId()),
                buildRecord.getStartTime(),
                buildRecord.getEndTime(),
                buildRecord.getScmRepoURL(),
                buildRecord.getScmRevision(),
                buildRoot,
                dependencies,
                builtArtifacts
        );
    }

    private Set<CausewayPushRequest.BuiltArtifact> collectBuiltArtifacts(Set<Artifact> builtArtifacts) {
        return builtArtifacts.stream().map(artifact -> {
                Gav gav = Gav.parse(artifact.getIdentifier());
                return new CausewayPushRequest.BuiltArtifact(
                        artifact.getRepoType(),
                        artifact.getFilename(),
                        artifact.getMd5(),
                        artifact.getSha256(),
                        artifact.getSize(),
                        getDeployUrl(artifact),
                        gav.getGroupId(),
                        gav.getArtifactId(),
                        gav.getVersion()
                    );
                })
            .collect(Collectors.toSet());
    }

    private String getDeployUrl(Artifact artifact) {
        return artifact.getDeployPath();
    }

    private Set<CausewayPushRequest.Dependency> collectDependencies(Set<Artifact> dependencies) {
        return dependencies.stream().map(artifact -> new CausewayPushRequest.Dependency(
                        artifact.getRepoType(),
                        artifact.getFilename(),
                        artifact.getMd5(),
                        artifact.getSha256(),
                        artifact.getSize()))
                .collect(Collectors.toSet());
    }

    public Integer pushCompleted(Integer buildRecordId, BuildRecordPushResult buildRecordPushResult) throws ProcessException {
        //accept only listed elements otherwise a new request might be wrongly completed from response of an older one
        if (!inProgress.remove(buildRecordId)) {
            throw new ProcessException("Did not find the referenced element.");
        }

        BuildRecordPushResult saved = buildRecordPushResultRepository.save(buildRecordPushResult);

        notifier.sendToSubscribers(saved, "causeway-push", buildRecordId.toString());
        return saved.getId();
    }

    public boolean cancelInProgressPush(Integer buildRecordId) {
        BuildRecordPushResultRest buildRecordPushResultRest = BuildRecordPushResultRest.builder()
                .buildRecordPushResultStatus(BuildRecordPushResult.Status.CANCELED)
                .buildRecordId(buildRecordId)
                .log("Canceled.")
                .build();
        boolean canceled = inProgress.remove(buildRecordId);
        notifier.sendToSubscribers(buildRecordPushResultRest, "causeway-push", buildRecordId.toString());
        return canceled;
    }

    public Set<Integer> getInProgress() {
        return inProgress.getAll();
    }
}
