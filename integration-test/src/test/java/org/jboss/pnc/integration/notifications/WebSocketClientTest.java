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
package org.jboss.pnc.integration.notifications;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.restclient.websocket.ListenerUnsubscriber;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.setup.RestClientConfiguration.NOTIFICATION_PATH;
import static org.jboss.pnc.restclient.websocket.predicates.BuildChangedNotificationPredicates.withBuildCompleted;
import static org.jboss.pnc.restclient.websocket.predicates.BuildChangedNotificationPredicates.withBuildConfiguration;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class WebSocketClientTest {

    private static final String PNC_SOCKET_URL = "ws://localhost:8080" + NOTIFICATION_PATH;

    public static final Logger logger = LoggerFactory.getLogger(WebSocketClientTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void testConnection() throws Exception {
        // with
        WebSocketClient wsClient = new VertxWebSocketClient();

        // when
        CompletableFuture<Void> future = wsClient.connect(PNC_SOCKET_URL);

        // then
        assertThat(future).succeedsWithin(500, TimeUnit.MILLISECONDS);
        wsClient.disconnect();
    }

    @Test
    public void testDisconnect() throws Exception {
        // with
        WebSocketClient wsClient = new VertxWebSocketClient();
        CompletableFuture<Void> connect = wsClient.connect(PNC_SOCKET_URL);
        connect.get();

        // when
        CompletableFuture<Void> disconnect = wsClient.disconnect();

        // then
        assertThat(disconnect).succeedsWithin(500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testBuildListener() throws Exception {
        // with
        WebSocketClient wsClient = new VertxWebSocketClient();
        wsClient.connect(PNC_SOCKET_URL).get();
        BuildConfigurationClient buildConfigurationClient = new BuildConfigurationClient(
                RestClientConfiguration.asUser());
        BuildConfiguration bc = buildConfigurationClient.getAll().iterator().next();
        AtomicInteger notificationCounter = new AtomicInteger(0);

        // when
        ListenerUnsubscriber unsubscriber = wsClient.onBuildChangedNotification((notification -> {
            notificationCounter.incrementAndGet();
            assertThat(notification).isNotNull();
            assertThat(notification.getBuild()).isNotNull();
        }), withBuildConfiguration(bc.getId()));
        buildConfigurationClient.trigger(bc.getId(), new BuildParameters());

        // then
        Thread.sleep(1000);
        unsubscriber.run();
        wsClient.disconnect().get();
        assertThat(notificationCounter).hasValueGreaterThanOrEqualTo(2);
    }

    @Test
    public void testNotificationCatcher() throws Exception {
        // with
        WebSocketClient wsClient = new VertxWebSocketClient();
        wsClient.connect(PNC_SOCKET_URL).get();
        BuildConfigurationClient buildConfigurationClient = new BuildConfigurationClient(
                RestClientConfiguration.asUser());
        BuildConfiguration bc = buildConfigurationClient.getAll().iterator().next();

        // when
        CompletableFuture<BuildChangedNotification> future = wsClient
                .catchBuildChangedNotification(withBuildConfiguration(bc.getId()), withBuildCompleted());
        buildConfigurationClient.trigger(bc.getId(), new BuildParameters());

        // then
        assertThat(future).succeedsWithin(500, TimeUnit.MILLISECONDS);
        BuildChangedNotification bcn = future.get();
        assertThat(bcn).isNotNull();
        assertThat(bcn.getBuild()).isNotNull();
        assertThat(bcn.getBuild().getBuildConfigRevision().getId()).isEqualTo(bc.getId());
        assertThat(bcn.getBuild().getStatus().isFinal()).isTrue();
        wsClient.disconnect();
    }
}
