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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.dto.notification.BuildConfigurationCreation;
import org.jboss.pnc.dto.notification.BuildPushResultNotification;
import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.dto.notification.Notification;
import org.jboss.pnc.dto.notification.ProductMilestoneCloseResultNotification;
import org.jboss.pnc.dto.notification.RepositoryCreationFailure;
import org.jboss.pnc.dto.notification.SCMRepositoryCreationSuccess;
import org.jboss.pnc.enums.BuildStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
public class VertxWebSocketClient implements WebSocketClient, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(VertxWebSocketClient.class);

    private static final ObjectMapper objectMapper = getObjectMapper();

    /**
     * Almost identical version of {@link JsonOutputConverterMapper} but without constant error messages on missing
     * openshift class
     * 
     * @return JSON mapper
     */
    private static ObjectMapper getObjectMapper() {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
    }

    private Vertx vertx;

    private HttpClient httpClient;

    private WebSocket webSocketConnection;

    /**
     * vert.x timer id which periodically sends pings to the WS server
     */
    private long periodicPingTimerId = -1;

    /**
     * delay between individual pings in ms.
     *
     * default: 2 sec
     */
    private int pingDelays = 2000;

    /**
     * amount of time we allow the WS server to be unresponsive to the pings until we consider reconnection
     *
     * default: 20 sec
     */
    private int maxUnresponsivenessTime = 20000;

    /**
     * how many pings were left unanswered.
     *
     * if pingPongDifference > (maxUnresponsivenessTime/pingDelays) is true, we start reconnecting.
     * (maxUnresponsivenessTime/pingDelays) equals to upper limit of unanswered pings.
     */
    private AtomicLong pingPongDifference = new AtomicLong(0);

    /**
     * use concurrent version since we may modify that list concurrently
     */
    private Set<Dispatcher> dispatchers = ConcurrentHashMap.newKeySet();

    private Map<CompletableFuture<Notification>, Supplier<Notification>> singleNotificationFutures = new ConcurrentHashMap<>();

    /**
     * maximum amount of time in milliseconds taken between retries
     * <p>
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

    /**
     * timeout we wait on first connection to WS server or on reconnection
     */
    private final int connectTimeout = 5000;

    public VertxWebSocketClient() {
        reconnectDelay = initialDelay;
    }

    public VertxWebSocketClient(
            int upperLimitForRetry,
            int initialDelay,
            float delayMultiplier,
            int pingDelays,
            int maxUnresponsivenessTime) {
        this.delayMultiplier = delayMultiplier;
        this.upperLimitForRetry = upperLimitForRetry;
        this.initialDelay = initialDelay;
        reconnectDelay = initialDelay;
        this.pingDelays = pingDelays;
        this.maxUnresponsivenessTime = maxUnresponsivenessTime;
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
            HttpClientOptions options = new HttpClientOptions();
            options.setKeepAlive(false).setConnectTimeout(connectTimeout);
            this.httpClient = vertx.createHttpClient(options);
        }

        if (webSocketConnection != null && !webSocketConnection.isClosed()) {
            log.trace("Already connected.");
            return CompletableFuture.completedFuture(null);
        }
        // in case no port was given, default to http port 80
        int port = serverURI.getPort() == -1 ? 80 : serverURI.getPort();

        CompletableFuture<Void> future = new CompletableFuture<>();
        httpClient.webSocket(port, serverURI.getHost(), serverURI.getPath(), result -> {
            if (result.succeeded()) {
                log.debug("Connection to WebSocket server: " + webSocketServerUrl + " successful.");
                resetDefaults();
                webSocketConnection = result.result();
                webSocketConnection.textMessageHandler(this::dispatch);
                webSocketConnection.closeHandler((ignore) -> connectionClosed(webSocketServerUrl));
                startPingPong(webSocketServerUrl);
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

    private void connectionUnreachable(String webSocketServerUrl) {
        log.warn(
                "WebSocket server is unreachable. Possible VPN/Network issues, will retry in: " + reconnectDelay
                        + " milliseconds.");
        retryConnection(webSocketServerUrl);
    }

    private void manuallyCloseConnection() {
        if (webSocketConnection != null && !webSocketConnection.isClosed()) {
            log.trace("Manually closing WS connection.");
            webSocketConnection.close();
        }
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
        return connect(webSocketServerUrl).thenRun(this::runReconnectChecksOnSingles).thenRun(this::resetDefaults);
    }

    /**
     * Run reconnect checks (f.e. invoke REST) on associated notifications and complete them if check succeeds (returns
     * non-null value)
     */
    private void runReconnectChecksOnSingles() {
        singleNotificationFutures.forEach((key, value) -> {
            if (!key.isDone()) {
                Notification notification = value.get();
                if ((notification != null)) {
                    key.complete(notification);
                }
            }
        });
    }

    private void startPingPong(String webSocketServerUrl) {
        webSocketConnection.pongHandler(this::handlePong);
        periodicPingTimerId = vertx.setPeriodic(pingDelays, (timerId) -> ping(timerId, webSocketServerUrl));
    }

    private void ping(long timerId, String webSocketServerUrl) {
        if (pingPongDifference.get() > (maxUnresponsivenessTime / pingDelays)) {
            // cancel itself to avoid sending pings during reconnections
            if (vertx.cancelTimer(timerId)) {
                periodicPingTimerId = -1;
            }
            // unresponsive server still has opened connection even though it does not respond
            manuallyCloseConnection();
            connectionUnreachable(webSocketServerUrl);
            return;
        }
        log.trace("Sending ping to WS server: " + webSocketServerUrl);
        pingPongDifference.incrementAndGet();
        webSocketConnection.writePing(Buffer.buffer());
    }

    private void handlePong(Buffer ignore) {
        log.trace("Received pong from WS server.");
        pingPongDifference.decrementAndGet();
    }

    private void resetDefaults() {
        pingPongDifference.set(0);
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
        vertx.cancelTimer(periodicPingTimerId);
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
        return future.whenComplete((x, y) -> vertx.close((nothing) -> clearVertx()));
    }

    private void clearVertx() {
        this.vertx = null;
        this.httpClient = null;
        this.webSocketConnection = null;
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
            Supplier<T> reconnectCheck,
            Predicate<T>... filters) {
        CompletableFuture<T> future = new CompletableFuture<>();

        // returns null on incorrect message
        Supplier<T> reconnectWithTestCheck = () -> {
            T t = reconnectCheck.get();
            for (Predicate<T> filter : filters) {
                if (t == null || !filter.test(t)) {
                    return null;
                }
            }
            return t;
        };

        singleNotificationFutures
                .put((CompletableFuture<Notification>) future, (Supplier<Notification>) reconnectWithTestCheck);

        ListenerUnsubscriber unsubscriber = null;
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

    // NOTIFICATION LISTENERS

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

    // NO RECONNECTS

    private <T extends Notification> CompletableFuture<T> catchSingleNotification(
            Class<T> notificationClass,
            Predicate<T>... filters) {
        return catchSingleNotification(notificationClass, () -> null, filters);
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
    public CompletableFuture<ProductMilestoneCloseResultNotification> catchProductMilestoneCloseResult(
            Predicate<ProductMilestoneCloseResultNotification>... filters) {
        return catchSingleNotification(ProductMilestoneCloseResultNotification.class, filters);
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

    // WITH RECONNECTS

    @Override
    public CompletableFuture<BuildChangedNotification> catchBuildChangedNotification(
            FallbackRequestSupplier<Build> reconnectSupplier,
            Predicate<BuildChangedNotification>... filters) {
        return catchSingleNotification(
                BuildChangedNotification.class,
                () -> mockBuildNotification(reconnectSupplier),
                filters);
    }

    private BuildChangedNotification mockBuildNotification(FallbackRequestSupplier<Build> fallback) {
        Build build = null;
        try {
            build = fallback.get();
        } catch (RemoteResourceException exception) {
            log.warn("Failsafe reconnection failed.", exception);
            return null;
        }
        return build == null ? null : new BuildChangedNotification(BuildStatus.NEW, build);
    }

    @Override
    public CompletableFuture<GroupBuildChangedNotification> catchGroupBuildChangedNotification(
            FallbackRequestSupplier<GroupBuild> reconnectSupplier,
            Predicate<GroupBuildChangedNotification>... filters) {
        return catchSingleNotification(
                GroupBuildChangedNotification.class,
                () -> mockGroupNotification(reconnectSupplier),
                filters);
    }

    private GroupBuildChangedNotification mockGroupNotification(FallbackRequestSupplier<GroupBuild> fallback) {
        GroupBuild groupBuild = null;
        try {
            groupBuild = fallback.get();
        } catch (RemoteResourceException exception) {
            log.warn("Failsafe reconnection failed.", exception);
            return null;
        }
        return groupBuild == null ? null : new GroupBuildChangedNotification(groupBuild);
    }

    @Override
    public CompletableFuture<BuildPushResultNotification> catchBuildPushResult(
            FallbackRequestSupplier<BuildPushResult> reconnectSupplier,
            Predicate<BuildPushResultNotification>... filters) {
        return catchSingleNotification(
                BuildPushResultNotification.class,
                () -> mockBuildPushNotification(reconnectSupplier),
                filters);
    }

    private BuildPushResultNotification mockBuildPushNotification(FallbackRequestSupplier<BuildPushResult> fallback) {
        BuildPushResult pushResult = null;
        try {
            pushResult = fallback.get();
        } catch (RemoteResourceException exception) {
            log.warn("Failsafe reconnection failed.", exception);
            return null;
        }
        return pushResult == null ? null : new BuildPushResultNotification(pushResult);
    }

    @Override
    public CompletableFuture<ProductMilestoneCloseResultNotification> catchProductMilestoneCloseResult(
            FallbackRequestSupplier<ProductMilestoneCloseResult> reconnectSupplier,
            Predicate<ProductMilestoneCloseResultNotification>... filters) {
        return catchSingleNotification(
                ProductMilestoneCloseResultNotification.class,
                () -> mockMilestoneCloseNotification(reconnectSupplier),
                filters);
    }

    private ProductMilestoneCloseResultNotification mockMilestoneCloseNotification(
            FallbackRequestSupplier<ProductMilestoneCloseResult> fallback) {
        ProductMilestoneCloseResult pushResult = null;
        try {
            pushResult = fallback.get();
        } catch (RemoteResourceException exception) {
            log.warn("Failsafe reconnection failed.", exception);
            return null;
        }
        return pushResult == null ? null : new ProductMilestoneCloseResultNotification(pushResult);
    }

    @Override
    public void close() throws Exception {
        disconnect().join();
        if (vertx != null)
            vertx.close();
    }

    /**
     * TODO: Use Java 11 Cleaner class instead after Orchestrator migration to Java 11
     *
     * @throws Throwable throwable
     * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/ref/Cleaner.html">Java 11
     *      Cleaner</a>
     */
    @Override
    @Deprecated
    protected void finalize() throws Throwable {
        try {
            try {
                // attempt to properly disconnect
                disconnect().get();
            } finally {
                // always close vertx
                if (vertx != null)
                    vertx.close();
            }
        } finally {
            // always finalize
            super.finalize();
        }
    }
}
