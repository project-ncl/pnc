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
package org.jboss.pnc.restclient.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.dto.notification.BuildConfigurationCreation;
import org.jboss.pnc.dto.notification.BuildPushResultNotification;
import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.dto.notification.Notification;
import org.jboss.pnc.dto.notification.ProductMilestoneCloseResultNotification;
import org.jboss.pnc.dto.notification.RepositoryCreationFailure;
import org.jboss.pnc.dto.notification.SCMRepositoryCreationSuccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
public class VertxWebSocketClient implements WebSocketClient, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(VertxWebSocketClient.class);

    private static final ObjectMapper objectMapper = JsonOutputConverterMapper.getMapper();

    private Vertx vertx;

    private HttpClient httpClient;

    private WebSocket webSocketConnection;

    // use concurrent version since we may modify that list concurrently
    private Set<Dispatcher> dispatchers = ConcurrentHashMap.newKeySet();

    private Set<CompletableFuture<Notification>> singleNotificationFutures = ConcurrentHashMap.newKeySet();

    /**
     * maximum amount of time in milliseconds taken between retries
     * 
     * default: 10 min
     */
    private int upperLimitForRetry = 600000;

    private int numberOfRetries = 0;

    /**
     * a multiplier that increases delay between reconnect attempts
     */
    private float delayMultiplier = 1.5F;

    /**
     * amount of milliseconds client waits before attempting to reconnect
     */
    private int initialDelay = 250;

    private int reconnectDelay;

    public VertxWebSocketClient() {
        reconnectDelay = initialDelay;
    }

    public VertxWebSocketClient(int upperLimitForRetry, int initialDelay, int delayMultiplier) {
        this.delayMultiplier = delayMultiplier;
        this.upperLimitForRetry = upperLimitForRetry;
        this.initialDelay = initialDelay;
        reconnectDelay = initialDelay;
    }

    @Override
    public CompletableFuture<Void> connect(String webSocketServerUrl) {
        if (webSocketServerUrl == null) {
            throw new IllegalArgumentException("WebSocketServerUrl is null");
        }

        final URI serverURI;
        try {
            serverURI = new URI(webSocketServerUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("WebSocketServerUrl is not valid URI", e);
        }

        if (this.vertx == null) {
            this.vertx = Vertx.vertx();
            this.httpClient = vertx.createHttpClient();
        }

        if (webSocketConnection != null && !webSocketConnection.isClosed()) {
            log.trace("Already connected.");
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        // in case no port was given, default to http port 80
        int port = serverURI.getPort() == -1 ? 80 : serverURI.getPort();
        httpClient.webSocket(port, serverURI.getHost(), serverURI.getPath(), result -> {
            if (result.succeeded()) {
                log.debug("Connection to WebSocket server: " + webSocketServerUrl + " successful.");
                resetDefaults();
                webSocketConnection = result.result();
                webSocketConnection.textMessageHandler(this::dispatch);
                webSocketConnection.closeHandler((ignore) -> connectionClosed(webSocketServerUrl));
                // Async operation complete
                future.complete(null);
            } else {
                log.error("Connection to WebSocket server: " + webSocketServerUrl + " unsuccessful.", result.cause());
                // if there was a request to reconnect through retries, try to reconnect for possible network issues
                if (numberOfRetries > 0) {
                    connectionLost(webSocketServerUrl);
                }
                future.completeExceptionally(result.cause());
            }
        });
        return future;
    }

    private void dispatch(String message) {
        dispatchers.forEach((dispatcher) -> dispatcher.accept(message));
    }

    private void connectionClosed(String webSocketServerUrl) {
        log.warn("WebSocket connection was remotely closed, will retry in: " + reconnectDelay + " milliseconds.");
        retryConnection(webSocketServerUrl);
    }

    private void connectionLost(String webSocketServerUrl) {
        log.warn(
                "WebSocket connection lost. Possible VPN/Network issues, will retry in: " + reconnectDelay
                        + " milliseconds.");
        retryConnection(webSocketServerUrl);
    }

    private void retryConnection(String webSocketServerUrl) {
        numberOfRetries++;
        vertx.setTimer(reconnectDelay, (timerId) -> connectAndReset(webSocketServerUrl));
        // don't exceed upper limit for retry
        if (reconnectDelay * delayMultiplier > upperLimitForRetry)
            reconnectDelay = upperLimitForRetry;
        else
            reconnectDelay *= delayMultiplier;
    }

    private CompletableFuture<Void> connectAndReset(String webSocketServerUrl) {
        log.warn("Trying to reconnect. Number of retries: " + numberOfRetries);
        return connect(webSocketServerUrl).thenRun(this::resetDefaults);
    }

    private void resetDefaults() {
        reconnectDelay = initialDelay;
        numberOfRetries = 0;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        if (webSocketConnection == null || webSocketConnection.isClosed()) {
            // already disconnected
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        webSocketConnection.closeHandler(null);
        webSocketConnection.close((result) -> {
            if (result.succeeded()) {
                log.debug("Connection to WebSocket server successfully closed.");
                future.complete(null);
            } else {
                log.error("Connection to WebSocket server unsuccessfully closed.", result.cause());
                future.completeExceptionally(result.cause());
            }
        });
        return future.whenComplete((x, y) -> vertx.close());
    }

    @Override
    public <T extends Notification> ListenerUnsubscriber onMessage(
            Class<T> notificationClass,
            Consumer<T> listener,
            Predicate<T>... filters) throws ConnectionClosedException {
        if (webSocketConnection == null || webSocketConnection.isClosed()) {
            throw new ConnectionClosedException("Connection to WebSocket is closed.");
        }
        // add JSON message mapping before executing the listener
        Dispatcher dispatcher = (stringMessage) -> {
            T notification;
            try {
                notification = objectMapper.readValue(stringMessage, notificationClass);
                for (Predicate<T> filter : filters) {
                    if (filter != null && !filter.test(notification)) {
                        // does not satisfy a predicate
                        return;
                    }
                }
            } catch (JsonProcessingException e) {
                // could not parse to particular class of notification, unknown or different type of notification
                // ignoring the message
                return;
            }
            listener.accept(notification);
        };
        dispatchers.add(dispatcher);
        return () -> dispatchers.remove(dispatcher);
    }

    @Override
    public <T extends Notification> CompletableFuture<T> catchSingleNotification(
            Class<T> notificationClass,
            Predicate<T>... filters) {
        CompletableFuture<T> future = new CompletableFuture<>();
        ListenerUnsubscriber unsubscriber = null;
        singleNotificationFutures.add((CompletableFuture<Notification>) future);

        try {
            unsubscriber = onMessage(notificationClass, future::complete, filters);
        } catch (ConnectionClosedException e) {
            future.completeExceptionally(e);
            // in this case we have to set unsubscriber manually to avoid NPE
            unsubscriber = () -> {};
        }

        final ListenerUnsubscriber finalUnsubscriber = unsubscriber;
        return future.whenComplete((notification, throwable) -> finalUnsubscriber.run());
    }

    @Override
    public ListenerUnsubscriber onBuildChangedNotification(
            Consumer<BuildChangedNotification> onNotification,
            Predicate<BuildChangedNotification>... filters) throws ConnectionClosedException {
        return onMessage(BuildChangedNotification.class, onNotification, filters);
    }

    @Override
    public ListenerUnsubscriber onBuildConfigurationCreation(
            Consumer<BuildConfigurationCreation> onNotification,
            Predicate<BuildConfigurationCreation>... filters) throws ConnectionClosedException {
        return onMessage(BuildConfigurationCreation.class, onNotification, filters);
    }

    @Override
    public ListenerUnsubscriber onBuildPushResult(
            Consumer<BuildPushResultNotification> onNotification,
            Predicate<BuildPushResultNotification>... filters) throws ConnectionClosedException {
        return onMessage(BuildPushResultNotification.class, onNotification, filters);
    }

    @Override
    public ListenerUnsubscriber onGroupBuildChangedNotification(
            Consumer<GroupBuildChangedNotification> onNotification,
            Predicate<GroupBuildChangedNotification>... filters) throws ConnectionClosedException {
        return onMessage(GroupBuildChangedNotification.class, onNotification, filters);
    }

    @Override
    public ListenerUnsubscriber onRepositoryCreationFailure(
            Consumer<RepositoryCreationFailure> onNotification,
            Predicate<RepositoryCreationFailure>... filters) throws ConnectionClosedException {
        return onMessage(RepositoryCreationFailure.class, onNotification, filters);
    }

    @Override
    public ListenerUnsubscriber onSCMRepositoryCreationSuccess(
            Consumer<SCMRepositoryCreationSuccess> onNotification,
            Predicate<SCMRepositoryCreationSuccess>... filters) throws ConnectionClosedException {
        return onMessage(SCMRepositoryCreationSuccess.class, onNotification, filters);
    }

    @Override
    public ListenerUnsubscriber onProductMilestoneCloseResult(
            Consumer<ProductMilestoneCloseResultNotification> onNotification,
            Predicate<ProductMilestoneCloseResultNotification>... filters) throws ConnectionClosedException {
        return onMessage(ProductMilestoneCloseResultNotification.class, onNotification, filters);
    }

    @Override
    public CompletableFuture<BuildChangedNotification> catchBuildChangedNotification(
            Predicate<BuildChangedNotification>... filters) {
        return catchSingleNotification(BuildChangedNotification.class, filters);
    }

    @Override
    public CompletableFuture<BuildConfigurationCreation> catchBuildConfigurationCreation(
            Predicate<BuildConfigurationCreation>... filters) {
        return catchSingleNotification(BuildConfigurationCreation.class, filters);
    }

    @Override
    public CompletableFuture<BuildPushResultNotification> catchBuildPushResult(
            Predicate<BuildPushResultNotification>... filters) {
        return catchSingleNotification(BuildPushResultNotification.class, filters);
    }

    @Override
    public CompletableFuture<GroupBuildChangedNotification> catchGroupBuildChangedNotification(
            Predicate<GroupBuildChangedNotification>... filters) {
        return catchSingleNotification(GroupBuildChangedNotification.class, filters);
    }

    @Override
    public CompletableFuture<RepositoryCreationFailure> catchRepositoryCreationFailure(
            Predicate<RepositoryCreationFailure>... filters) {
        return catchSingleNotification(RepositoryCreationFailure.class, filters);
    }

    @Override
    public CompletableFuture<SCMRepositoryCreationSuccess> catchSCMRepositoryCreationSuccess(
            Predicate<SCMRepositoryCreationSuccess>... filters) {
        return catchSingleNotification(SCMRepositoryCreationSuccess.class, filters);
    }

    @Override
    public CompletableFuture<ProductMilestoneCloseResultNotification> catchProductMilestoneCloseResult(
            Predicate<ProductMilestoneCloseResultNotification>... filters) {
        return catchSingleNotification(ProductMilestoneCloseResultNotification.class, filters);
    }

    @Override
    public void close() throws Exception {
        disconnect().join();
        if (vertx != null)
            vertx.close();
    }
}
