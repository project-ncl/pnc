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
package org.jboss.pnc.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.util.Headers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.buildagent.api.Status;
import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.buildagent.common.http.HttpClient;
import org.jboss.pnc.common.security.Md5;
import org.jboss.pnc.integration.setup.Credentials;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.mock.executor.BuildExecutionSessionMock;
import org.jboss.pnc.mock.executor.BuildExecutorMock;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.jboss.pnc.integration.setup.Deployments.EXECUTOR_JAR;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildExecutionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Deployment
    public static EnterpriseArchive deploy() {
        final EnterpriseArchive ear = Deployments.testEarForInContainerTest(BuildTest.class);
        Deployments.addBuildExecutorMock(ear);
        JavaArchive jar = ear.getAsType(JavaArchive.class, EXECUTOR_JAR);
        jar.addClass(BuildExecutionTest.class);
        jar.addClass(Credentials.class);
        return ear;
    }

    @Inject
    BuildExecutor buildExecutor;

    @Test(timeout = 10000L)
    public void shouldReceiveBuildResultViaHttpCallback() throws IOException, NoSuchAlgorithmException,
            URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
        // given
        TaskStatusUpdateEvent.Builder updateEventBuilder = TaskStatusUpdateEvent.newBuilder();
        updateEventBuilder.taskId(UUID.randomUUID().toString())
                .newStatus(Status.COMPLETED)
                .outputChecksum(Md5.digest("black"));
        Credentials user = Credentials.USER;
        List<Request.Header> headers = new ArrayList<>();
        headers.add(new Request.Header(Headers.CONTENT_TYPE_STRING, MediaType.APPLICATION_JSON));
        user.createAuthHeader((k, v) -> {
            headers.add(new Request.Header(k, v));
            return null;
        });
        String data = objectMapper.writeValueAsString(updateEventBuilder.build());

        BlockingQueue<TaskStatusUpdateEvent> events = new ArrayBlockingQueue<>(10);
        Consumer<TaskStatusUpdateEvent> statusChangeConsumer = (e) -> {
            events.add(e);
        };
        BuildExecutionSession session = createFakeExectionSession(statusChangeConsumer);

        // when
        String executionId = "11";
        ((BuildExecutorMock) buildExecutor).addRunningExecution(executionId, session);
        HttpClient httpClient = new HttpClient();

        Request request = new Request(
                Request.Method.POST,
                URI.create("http://localhost:8080/pnc-rest/v2/build-execution/" + executionId + "/completed"),
                headers);
        CompletableFuture<HttpClient.Response> responseFuture = httpClient.invoke(request, data);

        // then
        TaskStatusUpdateEvent event = events.take();// event received
        Assert.assertEquals(Status.COMPLETED, event.getNewStatus());
        Assert.assertEquals(200, responseFuture.get(5, TimeUnit.SECONDS).getCode());
    }

    private BuildExecutionSession createFakeExectionSession(Consumer<TaskStatusUpdateEvent> statusChangeConsumer) {
        BuildExecutionSessionMock buildExecutionSession = new BuildExecutionSessionMock(null, (v) -> {});
        buildExecutionSession.setBuildStatusUpdateConsumer(statusChangeConsumer);
        return buildExecutionSession;
    }
}
