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
package org.jboss.pnc.integration.remote;

import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.ConnectionInfo;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.notifications.websockets.NotificationsEndpoint;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.notifications.model.BuildChangedPayload;
import org.jboss.pnc.spi.notifications.model.EventType;
import org.jboss.pnc.spi.notifications.model.Notification;
import org.jboss.pnc.test.category.DebugTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;


/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Category(DebugTest.class)
public class RemoteBuildTest {

    private final String HOST = "localhost";
    private final String BEARER_TOKEN = "--valid-o-auth-token-goes-here-";

    private static BuildConfigurationRestClient buildConfigurationRestClient;

    Semaphore semaphore = new Semaphore(0);

    @Before
    public void before() {

        ConnectionInfo connectionInfo = ConnectionInfo.builder()
                .bearerToken(BEARER_TOKEN)
                .host(HOST)
                .port(80)
                .build();
        buildConfigurationRestClient = new BuildConfigurationRestClient(connectionInfo);
    }


    @Test
    public void runMultipleBuilds() throws Exception {
        String uri = "ws://" + HOST + "/pnc-rest/" + NotificationsEndpoint.ENDPOINT_PATH;
        Consumer<String> onMessage = (message) -> {
            try {
                Notification notification = new Notification(message);

                if (EventType.BUILD_STATUS_CHANGED.equals(notification.getEventType())) {
                    BuildChangedPayload buildStatusUpdate = (BuildChangedPayload) notification.getPayload();
                    if (buildStatusUpdate.getBuildCoordinationStatus().isCompleted()) {
                        notifyCompleted(buildStatusUpdate.getBuildConfigurationId(), buildStatusUpdate.getBuildCoordinationStatus());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        WebsocketListener listener = new WebsocketListener(URI.create(uri), onMessage);

        for (int i = 0; i < 20; i++) {
            int configurationId = runBuildConfiguration("test-build-execution");
            waitToComplete(configurationId);
        }
    }

    private void waitToComplete(int configurationId) throws InterruptedException {
        semaphore.acquire();
    }

    private void notifyCompleted(Integer buildConfigurationId, BuildCoordinationStatus buildCoordinationStatus) {
        semaphore.release();
    }

    public int runBuildConfiguration(String buildConfigurationName) throws Exception {
        Map<String, String> genericParameters = new HashMap<>();
        genericParameters.put("CUSTOM_PME_PARAMETERS", "-Dmanipulation.disable=true");

        String buildScript =
                "set +x\n"
                        + "for i in {1..100}; do\n"
                        + "    echo $i \"- 0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\"\n"
                        + "done\n"
                        + "echo \"Need to rest for a while ...\"\n"
                        + "sleep 30";

        BuildConfigurationRest buildConfiguration = getOrCreateBuildConfiguration(
                buildConfigurationName,
                buildScript,
                "master",
                genericParameters,
                8,
                9,
                3
        );

        BuildOptions options = new BuildOptions(false, true, false, false, false);
        RestResponse<BuildConfigurationRest> triggered = buildConfigurationRestClient.trigger(buildConfiguration.getId(), options);
        return buildConfiguration.getId();
    }

    BuildConfigurationRest getOrCreateBuildConfiguration(
            String buildConfigurationName,
            String buildScript,
            String scmRevision,
            Map<String, String> genericParameters,
            Integer projectId,
            Integer repositoryConfigurationId,
            Integer buildEnvironmentId) throws Exception {

        RestResponse<BuildConfigurationRest> existing = buildConfigurationRestClient.getByName(buildConfigurationName);

        if (!existing.hasValue()) {
            Project project = Project.Builder.newBuilder().id(projectId).build();
            RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.Builder.newBuilder().id(repositoryConfigurationId).build();
            BuildEnvironment buildEnvironment = BuildEnvironment.Builder.newBuilder().id(buildEnvironmentId).build();
            BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder()
                    .name(buildConfigurationName)
                    .project(project)
                    .repositoryConfiguration(repositoryConfiguration)
                    .scmRevision(scmRevision)
                    .buildEnvironment(buildEnvironment)
                    .buildScript(buildScript)
                    .genericParameters(genericParameters)
                    .build();
            BuildConfigurationRest buildConfigurationRest = new BuildConfigurationRest(buildConfiguration);
            RestResponse<BuildConfigurationRest> aNew = buildConfigurationRestClient.createNew(buildConfigurationRest);
            return aNew.getValue();
        } else {
            return existing.getValue();
        }
    }
}
