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
package org.jboss.pnc.integration;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.integration.client.BuildRecordPushRestClient;
import org.jboss.pnc.integration.client.BuildRecordRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.mock.CausewayClientMock;
import org.jboss.pnc.integration.websockets.WsUpdatesClient;
import org.jboss.pnc.managers.causeway.CausewayClient;
import org.jboss.pnc.mock.executor.BuildExecutorMock;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.rest.restmodel.BuildRecordPushRequestRest;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordPushTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(BuildRecordPushTest.class);

    private static final String PUSH_LOG = "Push it high!";

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        JavaArchive processManager = enterpriseArchive.getAsType(JavaArchive.class, AbstractTest.PROCESS_MANAGERS_JAR);
        processManager.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");
        processManager.addClass(CausewayClientMock.class);
        processManager.addClass(BuildExecutorMock.class);

        return enterpriseArchive;
    }

    @Test
    public void shouldPushBuildRecord() throws IOException, DeploymentException, TimeoutException, InterruptedException {
        List<BuildRecordPushResultRest> results = new ArrayList<>();
        Consumer<BuildRecordPushResultRest> onMessage = (result) -> {
            results.add(result);
        };

        BuildRecordRestClient buildRecordRestClient = new BuildRecordRestClient();
        BuildRecordRest buildRecordRest = buildRecordRestClient.firstNotNull().getValue();
        Integer buildRecordId = buildRecordRest.getId();

        connectToWsNotifications(buildRecordId, onMessage);

        BuildRecordPushRestClient pushRestClient = new BuildRecordPushRestClient();

        //when push BR
        BuildRecordPushRequestRest pushRequest = new BuildRecordPushRequestRest("tagPrefix", buildRecordId);
        RestResponse<Map> restResponse = pushRestClient.push(pushRequest);

        //then make sure the request has been accepted
        Map<String, Boolean> responseValue = restResponse.getValue();
        Assertions.assertThat(responseValue.get(buildRecordId.toString())).isTrue();

        //when the same BuildRecord pushed again
        RestResponse<Map> secondResponse = pushRestClient.push(pushRequest);

        //then it should be rejected
        Map<String, Boolean> secondValue = secondResponse.getValue();
        Assertions.assertThat(secondValue.get(buildRecordId.toString())).isFalse();

        //when completed
        mockCompletedFromCauseway(pushRestClient, buildRecordId);

        //test the completion notification
        Wait.forCondition(() -> results.size() > 0, 5, ChronoUnit.SECONDS);
        Assertions.assertThat(results.get(0).getLog()).isEqualTo(PUSH_LOG);

        //test DB entry
        BuildRecordPushResultRest result = pushRestClient.getStatus(buildRecordId);
        Assertions.assertThat(result.getLog()).isEqualTo(PUSH_LOG);

        //when the same BuildRecord pushed again
        RestResponse<Map> thirdResponse = pushRestClient.push(pushRequest);

        //then it should be accepted again
        Map<String, Boolean> thirdValue = thirdResponse.getValue();
        Assertions.assertThat(thirdValue.get(buildRecordId.toString())).isTrue();

    }

    private void mockCompletedFromCauseway(BuildRecordPushRestClient pushRestClient, Integer buildRecordId) {
        BuildRecordPushResultRest pushResultRest = BuildRecordPushResultRest.builder()
                .status(BuildRecordPushResult.Status.SUCCESS)
                .log(PUSH_LOG)
                .buildRecordId(buildRecordId)
                .build();

        pushRestClient.complete(pushResultRest);
    }

    private void connectToWsNotifications(Integer buildRecordId, Consumer<BuildRecordPushResultRest> onMessage)
            throws IOException, DeploymentException {

        Consumer<String> onMessageInternal = (message) -> {
            try {
                BuildRecordPushResultRest buildRecordPushResultRest = JsonOutputConverterMapper.readValue(message, BuildRecordPushResultRest.class);
                onMessage.accept(buildRecordPushResultRest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        WsUpdatesClient wsUpdatesClient = new WsUpdatesClient();
        wsUpdatesClient.subscribeBlocking("causeway-push", buildRecordId.toString(), onMessageInternal);
    }

    private class TestCausewayClient implements CausewayClient {
        @Override
        public boolean push(String jsonMessage, String authToken) {
            return true;
        }
    }

}
