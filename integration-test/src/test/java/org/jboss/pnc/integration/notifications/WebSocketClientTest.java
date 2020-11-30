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

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.RemoteResourceNotFoundException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.integration.utils.ResponseUtils;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.rest.api.parameters.GroupBuildParameters;
import org.jboss.pnc.restclient.AdvancedBuildConfigurationClient;
import org.jboss.pnc.restclient.AdvancedGroupConfigurationClient;
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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.jboss.pnc.integration.setup.RestClientConfiguration.NOTIFICATION_PATH;
import static org.jboss.pnc.restclient.websocket.predicates.BuildChangedNotificationPredicates.withBuildCompleted;
import static org.jboss.pnc.restclient.websocket.predicates.BuildChangedNotificationPredicates.withBuildConfiguration;
import static org.jboss.pnc.restclient.websocket.predicates.GroupBuildChangedNotificationPredicates.withGBuildCompleted;
import static org.jboss.pnc.restclient.websocket.predicates.GroupBuildChangedNotificationPredicates.withGConfigId;

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

    @Test
    public void shouldReconnectAfterClosingConnection() throws Exception {
        // with
        WebSocketSessionHandler handler = new WebSocketSessionHandler();
        Undertow wsServer = withHandler(handler);
        wsServer.start();

        // when
        WebSocketClient wsClient = new VertxWebSocketClient();
        wsClient.connect("ws://localhost:8082" + NOTIFICATION_PATH).join();

        // wait a little for Undertow accept the client and increment the value
        Thread.sleep(50);
        assertThat(handler.getSessionCounter()).hasValue(1);

        // disconnect wsClient and force him to reconnect
        handler.closeSession();

        Thread.sleep(500);
        // then
        assertThat(handler.getSessionCounter()).hasValue(2);

        wsClient.close();
        wsServer.stop();
    }

    @Test
    public void shouldReceivePings() throws Exception {
        // with
        WebSocketSessionHandler handler = new WebSocketSessionHandler();
        Undertow wsServer = withHandler(handler);
        wsServer.start();

        // when
        int pingDelays = 100;
        WebSocketClient wsClient = withPingDelay(pingDelays);
        wsClient.connect("ws://localhost:8082" + NOTIFICATION_PATH).get();

        // let the client ping in the background
        Thread.sleep(pingDelays * 12);
        // then
        assertThat(handler.getPingCounter()).hasValueGreaterThanOrEqualTo(10);

        wsClient.close();
        wsServer.stop();
    }

    @Test
    public void shouldReconnectAfterBeingUnresponsive() throws Exception {
        // with
        WebSocketSessionHandler handler = new WebSocketSessionHandler();
        Undertow wsServer = withHandler(handler);
        wsServer.start();

        // set max server unresponsiveness
        int maxUnresponsiveness = 1000;
        WebSocketClient wsClient = withMaxUnresponsiveness(1000);

        // make server unresponsive
        handler.blockPongs();

        // when
        wsClient.connect("ws://localhost:8082" + NOTIFICATION_PATH).get();

        // wait maxUnresponsiveness time plus a little more to give client time to reconnect
        Thread.sleep(maxUnresponsiveness + 500);

        // then
        assertThat(handler.getSessionCounter()).hasValue(2);

        wsClient.close();
        wsServer.stop();
    }

    @Test
    public void testRestBuildFallback() throws Exception {
        // with
        WebSocketSessionHandler handler = new WebSocketSessionHandler();
        Undertow wsServer = withHandler(handler);
        wsServer.start();

        WebSocketClient wsClient = new VertxWebSocketClient();
        wsClient.connect("ws://localhost:8082" + NOTIFICATION_PATH).join();

        AdvancedBuildConfigurationClient buildConfigurationClient = new AdvancedBuildConfigurationClient(
                RestClientConfiguration.asUser());

        // test the actual fallbackSupplier (it's private -> reflection unfortunately)
        Method supplier = buildConfigurationClient.getClass().getDeclaredMethod("fallbackSupplier", String.class);
        supplier.setAccessible(true);

        // when
        BuildConfiguration bc = buildConfigurationClient.getAll().iterator().next();
        CompletableFuture<BuildChangedNotification> future = wsClient.catchBuildChangedNotification(
                () -> invokeMethod(supplier, Build.class, buildConfigurationClient, bc.getId()),
                withBuildConfiguration(bc.getId()),
                withBuildCompleted());
        buildConfigurationClient.trigger(bc.getId(), new BuildParameters());
        // make client reconnect and use REST fallback
        handler.closeSession();

        // then
        assertThat(future).succeedsWithin(1000, TimeUnit.MILLISECONDS);
        wsClient.close();
        wsServer.stop();
    }

    @Test
    public void testRestGroupBuildFallback() throws Exception {
        // with
        WebSocketSessionHandler handler = new WebSocketSessionHandler();
        Undertow wsServer = withHandler(handler);
        wsServer.start();

        WebSocketClient wsClient = new VertxWebSocketClient();
        wsClient.connect("ws://localhost:8082" + NOTIFICATION_PATH).join();

        AdvancedGroupConfigurationClient groupConfigurationClient = new AdvancedGroupConfigurationClient(
                RestClientConfiguration.asUser());
        GroupConfiguration gc = groupConfigurationClient.getAll().iterator().next();

        // test the actual fallbackSupplier (it's private -> reflection unfortunately)
        Method supplier = groupConfigurationClient.getClass().getDeclaredMethod("fallbackSupplier", String.class);
        supplier.setAccessible(true);

        // when
        CompletableFuture<GroupBuildChangedNotification> future = wsClient.catchGroupBuildChangedNotification(
                () -> invokeMethod(supplier, GroupBuild.class, groupConfigurationClient, gc.getId()),
                withGConfigId(gc.getId()),
                withGBuildCompleted());
        GroupBuild groupBuild = groupConfigurationClient
                .trigger(gc.getId(), new GroupBuildParameters(), GroupBuildRequest.builder().build());

        // wait for GroupBuild to finish
        ResponseUtils.waitSynchronouslyFor(() -> groupBuildToFinish(groupBuild.getId()), 15, TimeUnit.SECONDS);

        // make client reconnect and use REST fallback
        handler.closeSession();

        // then
        assertThat(future).succeedsWithin(500, TimeUnit.MILLISECONDS);
        wsClient.close();
        wsServer.stop();
    }

    private Boolean groupBuildToFinish(String groupBuildId) {
        GroupBuild build = null;
        try {
            build = new GroupBuildClient(RestClientConfiguration.asUser()).getSpecific(groupBuildId);
            assertThat(build).isNotNull();
            if (!build.getStatus().isFinal())
                return false;
        } catch (RemoteResourceNotFoundException e) {
            fail(String.format("Group Build with id:%s not present", groupBuildId), e);
        } catch (ClientException e) {
            fail("Client has failed in an unexpected way.", e);
        }
        return true;
    }

    private <T> T invokeMethod(Method method, Class<T> returnType, Object instance, Object... params) {
        System.out.println("INVOKEED " + returnType.getCanonicalName());
        T result = null;
        try {
            result = (T) method.invoke(instance, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static Undertow withHandler(WebSocketSessionHandler handler) {
        return Undertow.builder()
                .addHttpListener(8082, "localhost")
                .setHandler(Handlers.path().addPrefixPath(NOTIFICATION_PATH, Handlers.websocket(handler)))
                .build();
    }

    private static VertxWebSocketClient withPingDelay(int pingDelays) {
        return new VertxWebSocketClient(60000, 250, 1.5F, pingDelays, 2000);
    }

    private static VertxWebSocketClient withMaxUnresponsiveness(int unresponsiveness) {
        return new VertxWebSocketClient(60000, 250, 1.5F, 100, unresponsiveness);
    }

    /**
     * Custom WebSocket connection handler for Undertow server
     */
    private class WebSocketSessionHandler implements WebSocketConnectionCallback {
        private final AtomicInteger sessionCounter = new AtomicInteger(0);
        private final AtomicInteger pingCounter = new AtomicInteger(0);
        private WebSocketChannel channel;
        private boolean blockPongs = false;

        @Override
        public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
            sessionCounter.incrementAndGet();
            this.channel = channel;
            channel.getReceiveSetter().set(new AbstractReceiveListener() {
                @Override
                protected void onFullPingMessage(WebSocketChannel channel, BufferedBinaryMessage message)
                        throws IOException {
                    pingCounter.incrementAndGet();
                    if (!blockPongs)
                        super.onFullPingMessage(channel, message);
                }
            });
            channel.resumeReceives();
        }

        public AtomicInteger getSessionCounter() {
            return sessionCounter;
        }

        public AtomicInteger getPingCounter() {
            return pingCounter;
        }

        public void blockPongs() {
            this.blockPongs = true;
        }

        public void closeSession() {
            try {
                channel.sendClose();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
