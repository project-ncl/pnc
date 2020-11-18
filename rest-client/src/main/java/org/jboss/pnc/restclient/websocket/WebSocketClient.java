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

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.dto.notification.BuildConfigurationCreation;
import org.jboss.pnc.dto.notification.BuildPushResultNotification;
import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.dto.notification.Notification;
import org.jboss.pnc.dto.notification.ProductMilestoneCloseResultNotification;
import org.jboss.pnc.dto.notification.RepositoryCreationFailure;
import org.jboss.pnc.dto.notification.SCMRepositoryCreationSuccess;

/**
 * WebSocket client interface that provides functionality for connecting/disconnecting to the WebSocket server specified
 * by its URL. Furthermore, the client provides API for adding listeners that intercept notifications and API for
 * catching single a notification.
 *
 * The client has an ability to automatically reconnect in the background to achieve resiliency. Moreover, the client
 * gives a user an option to include a secondary way to produce a message through other means than WebSockets(f.e REST).
 * This secondary way is used when client reconnects and ensures the message wasn't lost during period of lost
 * connection.
 *
 * The user can manipulate this feature with a set of attributes which can be set on creation of the client:
 *
 * - initialDelay: amount of milliseconds client initially waits before attempting to reconnect
 *
 * - delayMultiplier: a multiplier that increases delay between reconnect attempts
 *
 * - upperLimitForRetry: maximum time between retries
 *
 * - pingDelays: delays between ping to a connected server
 *
 * - maxUnresponsivenessTime: amount of we allow the server to be unresponsive
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
public interface WebSocketClient extends AutoCloseable {

    /**
     * Connects to the WebSocket server specified by webSocketServerUrl. The operation is asynchronous and returns
     * CompletableFuture, with an option to wait until this operation concludes.
     *
     * If the operation fails, the CompletableFuture will complete exceptionally. If the client is already connected to
     * a WebSocket server, the method immediately returns.
     *
     * @param webSocketServerUrl the web socket server url
     * @return the completable future
     */
    CompletableFuture<Void> connect(String webSocketServerUrl);

    /**
     * Disconnects the client from WebSocket server. The operation is asynchronous and returns CompletableFuture, with
     * an option to wait until this operation concludes.
     *
     * If the operation fails, the CompletableFuture will complete exceptionally. If the client is already disconnected,
     * the method immediately returns.
     *
     * @return the completable future
     */
    CompletableFuture<Void> disconnect();

    /**
     * This method registers a listener for a Notification. This listener continually intercepts WebSocket messages of a
     * specific type of Notification defined by notificationClass. Filters are used to filter out messages that are not
     * meant for the listener to intercept.
     *
     * The listener can't be blocking.
     *
     * The ListenerUnsubscriber is a Runnable that should be run after the user has no need for this listener to
     * intercept further messages.
     *
     * @param <T> the notification generic parameter
     * @param notificationClass the notification class
     * @param listener the listener
     * @param filters the filters
     * @return the listener unsubscriber
     * @throws ConnectionClosedException The exception is thrown, when attempting to register a listener on closed
     *         connection.
     */
    <T extends Notification> ListenerUnsubscriber onMessage(
            Class<T> notificationClass,
            Consumer<T> listener,
            Predicate<T>... filters) throws ConnectionClosedException;

    /**
     * This method is used to intercept one specific Notification defined by notificationClass. Filters are used to
     * filter out unnecessary Notifications. Reconnect check is used to get a notification by secondary means to ensure
     * message was not lost on reconnection. The method is asynchronous and returns CompletableFuture with the caught
     * notification as payload.
     *
     * If the connection is closed when invoking this method, the CompletableFuture will complete exceptionally.
     *
     * It is recommended to use timeouts with CompletableFuture as the Notification may have arrived before the
     * connection was created or the Notification may never arrive.
     *
     * @param <T> the notification generic parameter
     * @param notificationClass the notification class
     * @param reconnectCheck supplier for a secondary means to retrieve a notification
     * @param filters the filters
     * @return the completable future
     */
    <T extends Notification> CompletableFuture<T> catchSingleNotification(
            Class<T> notificationClass,
            Supplier<T> reconnectCheck,
            Predicate<T>... filters);

    /**
     * Specific version of {@link #onMessage} method for {@link BuildChangedNotification}.
     *
     * @param onNotification listener invoked on notification
     * @param filters the filters
     * @return the listener unsubscriber
     * @throws ConnectionClosedException The exception is thrown, when attempting to register a listener on closed
     *         connection.
     * @see #onMessage
     */
    ListenerUnsubscriber onBuildChangedNotification(
            Consumer<BuildChangedNotification> onNotification,
            Predicate<BuildChangedNotification>... filters) throws ConnectionClosedException;

    /**
     * Specific version of {@link #onMessage} method for {@link BuildConfigurationCreation}.
     *
     * @param onNotification listener invoked on notification
     * @param filters the filters
     * @return the listener unsubscriber
     * @throws ConnectionClosedException The exception is thrown, when attempting to register a listener on closed
     *         connection.
     * @see #onMessage
     */
    ListenerUnsubscriber onBuildConfigurationCreation(
            Consumer<BuildConfigurationCreation> onNotification,
            Predicate<BuildConfigurationCreation>... filters) throws ConnectionClosedException;

    /**
     * Specific version of {@link #onMessage} method for {@link BuildPushResultNotification}.
     *
     * @param onNotification listener invoked on notification
     * @param filters the filters
     * @return the listener unsubscriber
     * @throws ConnectionClosedException The exception is thrown, when attempting to register a listener on closed
     *         connection.
     * @see #onMessage
     */
    ListenerUnsubscriber onBuildPushResult(
            Consumer<BuildPushResultNotification> onNotification,
            Predicate<BuildPushResultNotification>... filters) throws ConnectionClosedException;

    /**
     * Specific version of {@link #onMessage} method for {@link GroupBuildChangedNotification}.
     *
     * @param onNotification listener invoked on notification
     * @param filters the filters
     * @return the listener unsubscriber
     * @throws ConnectionClosedException The exception is thrown, when attempting to register a listener on closed
     *         connection.
     * @see #onMessage
     */
    ListenerUnsubscriber onGroupBuildChangedNotification(
            Consumer<GroupBuildChangedNotification> onNotification,
            Predicate<GroupBuildChangedNotification>... filters) throws ConnectionClosedException;

    /**
     * Specific version of {@link #onMessage} method for {@link RepositoryCreationFailure}.
     *
     * @param onNotification listener invoked on notification
     * @param filters the filters
     * @return the listener unsubscriber
     * @throws ConnectionClosedException The exception is thrown, when attempting to register a listener on closed
     *         connection.
     * @see #onMessage
     */
    ListenerUnsubscriber onRepositoryCreationFailure(
            Consumer<RepositoryCreationFailure> onNotification,
            Predicate<RepositoryCreationFailure>... filters) throws ConnectionClosedException;

    /**
     * Specific version of {@link #onMessage} method for {@link SCMRepositoryCreationSuccess}.
     *
     * @param onNotification listener invoked on notification
     * @param filters the filters
     * @return the listener unsubscriber
     * @throws ConnectionClosedException The exception is thrown, when attempting to register a listener on closed
     *         connection.
     * @see #onMessage
     */
    ListenerUnsubscriber onSCMRepositoryCreationSuccess(
            Consumer<SCMRepositoryCreationSuccess> onNotification,
            Predicate<SCMRepositoryCreationSuccess>... filters) throws ConnectionClosedException;

    /**
     * Specific version of {@link #onMessage} method for {@link ProductMilestoneCloseResultNotification}.
     *
     * @param onNotification listener invoked on notification
     * @param filters the filters
     * @return the listener unsubscriber
     * @throws ConnectionClosedException The exception is thrown, when attempting to register a listener on closed
     *         connection.
     * @see #onMessage
     */
    ListenerUnsubscriber onProductMilestoneCloseResult(
            Consumer<ProductMilestoneCloseResultNotification> onNotification,
            Predicate<ProductMilestoneCloseResultNotification>... filters) throws ConnectionClosedException;

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link BuildChangedNotification}.
     *
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<BuildChangedNotification> catchBuildChangedNotification(
            Predicate<BuildChangedNotification>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link BuildConfigurationCreation}.
     *
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<BuildConfigurationCreation> catchBuildConfigurationCreation(
            Predicate<BuildConfigurationCreation>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link BuildPushResultNotification}.
     *
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<BuildPushResultNotification> catchBuildPushResult(
            Predicate<BuildPushResultNotification>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link GroupBuildChangedNotification}.
     *
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<GroupBuildChangedNotification> catchGroupBuildChangedNotification(
            Predicate<GroupBuildChangedNotification>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link RepositoryCreationFailure}.
     *
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<RepositoryCreationFailure> catchRepositoryCreationFailure(
            Predicate<RepositoryCreationFailure>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link SCMRepositoryCreationSuccess}.
     *
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<SCMRepositoryCreationSuccess> catchSCMRepositoryCreationSuccess(
            Predicate<SCMRepositoryCreationSuccess>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link ProductMilestoneCloseResultNotification}.
     *
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<ProductMilestoneCloseResultNotification> catchProductMilestoneCloseResult(
            Predicate<ProductMilestoneCloseResultNotification>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link BuildChangedNotification} with a supplier
     * invoked on reconnect.
     *
     * The reconnectSupplier must retrieve a {@link Build} contextually compatible with the {@param filters}. The
     * supplier must contain the same {@link Build} that the WS message would have.
     * 
     * @param reconnectSupplier Build supplier on reconnect
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<BuildChangedNotification> catchBuildChangedNotification(
            FallbackRequestSupplier<Build> reconnectSupplier,
            Predicate<BuildChangedNotification>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link BuildPushResultNotification} with a
     * supplier invoked on reconnect.
     * 
     * The reconnectSupplier must retrieve a {@link BuildPushResult} contextually compatible with the {@param filters}.
     * The supplier must contain the same {@link BuildPushResult} that the WS message would have.
     *
     * @param reconnectSupplier BuildPushResult supplier on reconnect
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<BuildPushResultNotification> catchBuildPushResult(
            FallbackRequestSupplier<BuildPushResult> reconnectSupplier,
            Predicate<BuildPushResultNotification>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link GroupBuildChangedNotification} with a
     * supplier invoked on reconnect.
     * 
     * The reconnectSupplier must retrieve a {@link GroupBuild} contextually compatible with the {@param filters}. The
     * supplier must contain the same {@link GroupBuild} that the WS message would have.
     *
     * @param reconnectSupplier GroupBuild supplier on reconnect
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<GroupBuildChangedNotification> catchGroupBuildChangedNotification(
            FallbackRequestSupplier<GroupBuild> reconnectSupplier,
            Predicate<GroupBuildChangedNotification>... filters);

    /**
     * Specific version of {@link #catchSingleNotification} method for {@link ProductMilestoneCloseResultNotification}
     * with a supplier invoked on reconnect.
     *
     * The reconnectSupplier must retrieve a {@link ProductMilestoneCloseResult} contextually compatible with the
     * {@param filters}. The supplier must contain the same {@link ProductMilestoneCloseResult} that the WS message
     * would have.
     * 
     * @param reconnectSupplier ProductMilestoneCloseResult supplier on reconnect
     * @param filters the filters
     * @return the completable future
     * @see #catchSingleNotification
     */
    CompletableFuture<ProductMilestoneCloseResultNotification> catchProductMilestoneCloseResult(
            FallbackRequestSupplier<ProductMilestoneCloseResult> reconnectSupplier,
            Predicate<ProductMilestoneCloseResultNotification>... filters);

    /**
     * Safely disconnects and closes the WebSocket client.
     *
     * @throws Exception exception
     */
    @Override
    void close() throws Exception;
}
