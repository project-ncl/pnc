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
package org.jboss.pnc.integrationrex;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.auth.KeycloakClient;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integrationrex.mock.BPMResultsMock;
import org.jboss.pnc.integrationrex.mock.LogJsonAction;
import org.jboss.pnc.integrationrex.mock.TriggeringWebhook;
import org.jboss.pnc.integrationrex.utils.BuildUtils;
import org.jboss.pnc.restclient.websocket.ConnectionClosedException;
import org.jboss.pnc.restclient.websocket.ListenerUnsubscriber;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.common.Assert;
import org.wiremock.webhooks.WebhookDefinition;
import org.wiremock.webhooks.Webhooks;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integrationrex.setup.RestClientConfiguration.NOTIFICATION_PATH;
import static org.jboss.pnc.integrationrex.setup.RestClientConfiguration.withBearerToken;
import static org.jboss.pnc.restclient.websocket.predicates.BuildChangedNotificationPredicates.withBuildConfiguration;

@RunAsClient
@RunWith(Arquillian.class)
@Category({ ContainerTest.class })
public class WaitingBuildTest extends RemoteServices {

    private static final Logger logger = LoggerFactory.getLogger(WaitingBuildTest.class);

    private BuildConfigurationClient buildConfigurationClient;

    BuildUtils buildUtils;

    private static final String PNC_SOCKET_URL = "ws://localhost:8080" + NOTIFICATION_PATH;
    WebSocketClient wsClient = new VertxWebSocketClient();

    private BPMWireMock bpm;

    @Before
    public void beforeEach() throws ExecutionException, InterruptedException {
        bpm = new BPMWireMock(8088);

        String token = KeycloakClient
                .getAuthTokensBySecret(authServerUrl, keycloakRealm, "test-user", "test-pass", "pnc", "", false)
                .getToken();

        buildConfigurationClient = new BuildConfigurationClient(withBearerToken(token));
        buildUtils = new BuildUtils(
                new BuildClient(withBearerToken(token)),
                new GroupBuildClient(withBearerToken(token)));

        wsClient.connect(PNC_SOCKET_URL).get();
    }

    @After
    public void afterEach() throws IOException {
        bpm.close();
        wsClient.disconnect();
    }

    @Test
    public void shouldNotStartParentBuildWhenDependencyIsRunning()
            throws RemoteResourceException, InterruptedException {
        BlockingQueue<BuildChangedNotification> childBuildChanges = new ArrayBlockingQueue<>(100);
        BlockingQueue<BuildChangedNotification> parentBuildChanges = new ArrayBlockingQueue<>(100);

        BuildConfiguration buildConfigurationParent = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==dependency-analysis-1.3"))
                .iterator()
                .next();

        // Update dependency
        BuildConfiguration buildConfigurationChild = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==pnc-1.0.0.DR1"))
                .iterator()
                .next();

        Consumer<BuildChangedNotification> onChildChange = (e -> {
            logger.info("Child build status changed: {}", e.getBuild().getStatus());
            childBuildChanges.add(e);
        });
        Consumer<BuildChangedNotification> onParentChange = (e -> {
            logger.info("Parent build status changed: {}", e.getBuild().getStatus());
            parentBuildChanges.add(e);
        });
        onBuildChangedNotification(buildConfigurationChild.getId(), onChildChange);
        onBuildChangedNotification(buildConfigurationParent.getId(), onParentChange);

        // start child
        buildConfigurationClient.trigger(buildConfigurationChild.getId(), buildUtils.getBuildParameters(false, true));
        // wait for the child to start building
        Assert.assertNotNull(childBuildChanges.poll(5, TimeUnit.SECONDS)); // ENQUEUED
        Assert.assertNotNull(childBuildChanges.poll(5, TimeUnit.SECONDS)); // BUILDING (could be different order -
                                                                           // async)

        buildConfigurationClient.trigger(buildConfigurationParent.getId(), buildUtils.getBuildParameters(false, true));
        logger.info("Triggered parent build.");

        // make sure child is still running and parent did not start yet
        assertThat(childBuildChanges.size()).isEqualTo(0);

        BuildChangedNotification parentStatus = parentBuildChanges.poll(100, TimeUnit.MILLISECONDS);
        assertThat(parentStatus.getBuild().getStatus()).isEqualTo(BuildStatus.WAITING_FOR_DEPENDENCIES);

        // parent should not be building yet as the child should be still running
        assertThat(parentBuildChanges.size()).isEqualTo(0);

        bpm.callbackNow(); // complete child build
        BuildChangedNotification childStatus = childBuildChanges.poll(5, TimeUnit.SECONDS);// SUCCESS
        logger.info("Child status: {}", childStatus.getBuild().getStatus());
        assertThat(childStatus.getBuild().getStatus()).isEqualTo(BuildStatus.SUCCESS);

        // check if parent has started
        parentBuildChanges.poll(5, TimeUnit.SECONDS); // ENQUEUED
        parentBuildChanges.poll(5, TimeUnit.SECONDS); // BUILDING (could be different order - async)

        // parent should complete
        bpm.callbackNow();
        parentStatus = parentBuildChanges.poll(5, TimeUnit.SECONDS);
        logger.info("Parent status: {}.", parentStatus.getBuild().getStatus());
        assertThat(parentStatus.getBuild().getStatus()).isEqualTo(BuildStatus.SUCCESS);
    }

    public ListenerUnsubscriber onBuildChangedNotification(
            String buildConfigId,
            Consumer<BuildChangedNotification> onChange) {
        // wsClient.connect(PNC_SOCKET_URL).join();
        try {
            return wsClient.onBuildChangedNotification(onChange, withBuildConfiguration(buildConfigId));
        } catch (ConnectionClosedException e) {
            throw new RuntimeException(e);
        } finally {
            // wsClient.disconnect();
        }
    }

    @Slf4j
    public static class BPMWireMock implements Closeable {

        private final WireMockServer wireMockServer;
        private final TriggeringWebhook triggeringWebhook;

        public BPMWireMock(int port) {
            triggeringWebhook = new TriggeringWebhook();
            wireMockServer = new WireMockServer(
                    WireMockConfiguration.options()
                            .networkTrafficListener(new ConsoleNotifyingWiremockNetworkTrafficListener())
                            .port(port)
                            .extensions(LogJsonAction.class)
                            .extensions(ResponseTemplateTransformer.builder().global(false).maxCacheEntries(0L).build())
                            .extensions(Webhooks.class)
                            .extensions(triggeringWebhook));

            wireMockServer.stubFor(
                    any(urlMatching(".*"))
                            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json"))
                            .withPostServeAction(
                                    "triggering-webhook",
                                    new WebhookDefinition().withMethod(RequestMethod.POST)
                                            .withHeader("Content-Type", "application/json")
                                            .withHeader("Authorization", "{{originalRequest.headers.Authorization}}")
                                            .withUrl("{{jsonPath originalRequest.body '$.callback'}}")
                                            .withBody(
                                                    "{ \"status\":true, \"response\": "
                                                            + BPMResultsMock.mockBuildResult() + "}")));
            wireMockServer.start();
        }

        public void callbackNow() {
            triggeringWebhook.callbackNow();
        }

        @Override
        public void close() throws IOException {
            wireMockServer.stop();
        }

    }

}
