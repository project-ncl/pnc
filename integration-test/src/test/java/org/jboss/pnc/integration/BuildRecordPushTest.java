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
package org.jboss.pnc.integration;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;

import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.notification.BuildPushResultNotification;
import org.jboss.pnc.integration.client.BuildRecordPushRestClient;
import org.jboss.pnc.integration.client.BuildRecordRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.mock.CausewayClientMock;
import org.jboss.pnc.integration_new.endpoint.notifications.WsUpdatesClient;
import org.jboss.pnc.rest.restmodel.BuildRecordPushRequestRest;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.response.ResultRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.util.Wait;

import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
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
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.jboss.pnc.enums.BuildPushStatus;

import static org.jboss.pnc.integration.deployments.Deployments.addBuildExecutorMock;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordPushTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(BuildRecordPushTest.class);

    private static final String PUSH_LOG = "Push it high!";

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        JavaArchive processManager = enterpriseArchive.getAsType(JavaArchive.class, AbstractTest.CAUSEWAY_CLIENT_JAR);
        processManager.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");
        processManager.addClass(CausewayClientMock.class);

        addBuildExecutorMock(enterpriseArchive);

        return enterpriseArchive;
    }

    @Test
    @Ignore // TODO: unignore after NCL-4964, fails because of switching to BuildPushResult in BuildResultPushManager
    public void shouldPushBuildRecord()
            throws IOException, DeploymentException, TimeoutException, InterruptedException {
        List<BuildPushResultNotification> results = new ArrayList<>();
        Consumer<BuildPushResultNotification> onMessage = (result) -> {
            logger.debug("Received notification result {}.", result);
            results.add(result);
        };

        BuildRecordRestClient buildRecordRestClient = new BuildRecordRestClient();
        BuildRecordRest buildRecordRest = buildRecordRestClient.firstNotNull().getValue();
        logger.info("Using BuildRecord {}.", buildRecordRest);
        Integer buildRecordId = buildRecordRest.getId();

        connectToWsNotifications(buildRecordId, onMessage);

        BuildRecordPushRestClient pushRestClient = new BuildRecordPushRestClient();

        // when push BR
        BuildRecordPushRequestRest pushRequest = new BuildRecordPushRequestRest("tagPrefix", buildRecordId, false);
        RestResponse<ResultRest[]> restResponse = pushRestClient.push(pushRequest);

        // then make sure the request has been accepted
        ResultRest[] responseValue = restResponse.getValue();
        ResultRest result = getById(responseValue, buildRecordId.toString());
        Assert.assertTrue(result.getStatus().isSuccess());
        Assertions.assertThat(result.getStatus()).isEqualTo(ResultRest.Status.ACCEPTED);

        // when the same BuildRecord pushed again
        RestResponse<ResultRest[]> secondResponse = pushRestClient.push(pushRequest);

        // then it should be rejected
        ResultRest[] secondValue = secondResponse.getValue();
        ResultRest secondResult = getById(secondValue, buildRecordId.toString());
        Assert.assertFalse(secondResult.getStatus().isSuccess());
        Assertions.assertThat(secondResult.getStatus()).isEqualTo(ResultRest.Status.REJECTED);

        // when completed
        mockCompletedFromCauseway(pushRestClient, buildRecordId);

        // test the completion notification
        Wait.forCondition(() -> results.size() > 0, 5, ChronoUnit.SECONDS);
        Assertions.assertThat(results.get(0).getBuildPushResult().getLog()).isEqualTo(PUSH_LOG);

        // test DB entry
        BuildRecordPushResultRest status = pushRestClient.getStatus(buildRecordId);
        Assertions.assertThat(status.getLog()).isEqualTo(PUSH_LOG);

        // when the same BuildRecord pushed again
        RestResponse<ResultRest[]> thirdResponse = pushRestClient.push(pushRequest);

        // then it should be accepted again
        ResultRest[] thirdValue = thirdResponse.getValue();
        ResultRest thirdResult = getById(responseValue, buildRecordId.toString());
        Assert.assertTrue(thirdResult.isSuccess());

    }

    private ResultRest getById(ResultRest[] results, String id) {
        for (ResultRest result : results) {
            if (result.getId().equals(id)) {
                return result;
            }
        }
        return null;
    }

    private void mockCompletedFromCauseway(BuildRecordPushRestClient pushRestClient, Integer buildRecordId) {
        BuildRecordPushResultRest pushResultRest = BuildRecordPushResultRest.builder()
                .status(BuildPushStatus.SUCCESS)
                .log(PUSH_LOG)
                .buildRecordId(buildRecordId)
                .build();

        pushRestClient.complete(pushResultRest);
    }

    private void connectToWsNotifications(Integer buildRecordId, Consumer<BuildPushResultNotification> onMessage)
            throws IOException, DeploymentException {

        Consumer<String> onMessageInternal = (message) -> {
            try {
                BuildPushResultNotification buildRecordPushResultRest = JsonOutputConverterMapper
                        .readValue(message, BuildPushResultNotification.class);
                onMessage.accept(buildRecordPushResultRest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        WsUpdatesClient wsUpdatesClient = new WsUpdatesClient();
        wsUpdatesClient.subscribeBlocking("causeway-push", buildRecordId.toString(), onMessageInternal);
    }

}
