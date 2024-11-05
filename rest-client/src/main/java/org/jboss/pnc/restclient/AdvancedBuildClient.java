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
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.notification.BuildPushResultNotification;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.jboss.pnc.restclient.websocket.predicates.BuildPushResultNotificationPredicates.withBuildId;
import static org.jboss.pnc.restclient.websocket.predicates.BuildPushResultNotificationPredicates.withPushCompleted;

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

    public CompletableFuture<BuildPushResult> waitForBrewPush(String buildId) {

        webSocketClient
                .connect(configuration.getWSProtocol() + "://" + configuration.getHost() + BASE_PATH + "/notifications")
                .join();

        return webSocketClient
                .catchBuildPushResult(() -> fallbackSupplier(buildId), withBuildId(buildId), withPushCompleted())
                .thenApply(BuildPushResultNotification::getBuildPushResult);
    }

    /**
     * Used to retrieve BuildPushResult through REST when WS Client loses connection and reconnects
     *
     * @param buildId Id of the build where the push build was run
     * @return
     * @throws RemoteResourceException
     */
    private BuildPushResult fallbackSupplier(String buildId) throws RemoteResourceException {
        BuildPushResult result = null;
        try (BuildClient client = new BuildClient(configuration)) {
            result = client.getPushResult(buildId);
        }
        return result;
    }

    public CompletableFuture<BuildPushResult> executeBrewPush(String buildId, BuildPushParameters parameters)
            throws RemoteResourceException {
        CompletableFuture<BuildPushResult> future = waitForBrewPush(buildId);
        super.push(buildId, parameters);
        return future;
    }

    public BuildPushResult executeBrewPush(
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
