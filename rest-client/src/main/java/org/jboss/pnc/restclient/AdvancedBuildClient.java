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
package org.jboss.pnc.restclient;

import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.OperationClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.dto.notification.OperationNotification;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.jboss.pnc.restclient.websocket.predicates.OperationNotificationPredicates.withOperationFinished;
import static org.jboss.pnc.restclient.websocket.predicates.OperationNotificationPredicates.withOperationID;

/**
 * AdvancedBuildClient that provides additional features to wait for a build to finish.
 *
 * It is highly recommended to use the class inside a try-with-resources statement to properly cleanup the websocket
 * client. Otherwise the program using this class may hang indefinitely
 */
public class AdvancedBuildClient extends BuildClient implements AutoCloseable {

    private WebSocketClient webSocketClient = new VertxWebSocketClient();

    public AdvancedBuildClient(Configuration configuration) {
        super(configuration);
    }

    public CompletableFuture<BuildPushReport> waitForBrewPush(String operationId) {

        webSocketClient
                .connect(configuration.getWSProtocol() + "://" + configuration.getHost() + BASE_PATH + "/notifications")
                .join();

        return webSocketClient
                .catchBuildPushResult(
                        () -> fallbackSupplier(operationId),
                        withOperationID(operationId),
                        withOperationFinished())
                .thenApply(this::getBuildPushReport);
    }

    private BuildPushReport getBuildPushReport(OperationNotification operationNotification) {
        try (BuildClient client = new BuildClient(configuration)) {
            try {
                return client.getPushReport(operationNotification.getOperationId());
            } catch (RemoteResourceException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Used to retrieve BuildPushReport through REST when WS Client loses connection and reconnects
     *
     * @param operationId Id of the operation that performs the push
     * @return
     * @throws RemoteResourceException
     */
    private BuildPushOperation fallbackSupplier(String operationId) throws RemoteResourceException {
        try (OperationClient client = new OperationClient(configuration)) {
            return client.getSpecificBuildPush(operationId);
        }
    }

    public CompletableFuture<BuildPushReport> executeBrewPush(String buildId, BuildPushParameters parameters)
            throws RemoteResourceException {
        BuildPushOperation push = super.push(buildId, parameters);
        return waitForBrewPush(push.getId());
    }

    public BuildPushReport executeBrewPush(
            String buildConfigId,
            BuildPushParameters parameters,
            long timeout,
            TimeUnit timeUnit) throws RemoteResourceException {
        try {
            return executeBrewPush(buildConfigId, parameters).get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            webSocketClient.disconnect();
        }
    }

    /**
     * Run this auto-close to make sure all vertx event loops are closed
     */
    @Override
    public void close() {
        if (webSocketClient != null) {
            try {
                super.close();
                webSocketClient.close();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't close websocket", e);
            }
        }
    }
}
