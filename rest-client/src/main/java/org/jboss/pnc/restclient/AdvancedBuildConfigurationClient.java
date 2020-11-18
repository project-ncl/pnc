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
package org.jboss.pnc.restclient;

import static org.jboss.pnc.restclient.websocket.predicates.BuildChangedNotificationPredicates.withBuildCompleted;
import static org.jboss.pnc.restclient.websocket.predicates.BuildChangedNotificationPredicates.withBuildConfiguration;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

/**
 * AdvancedBuildConfigurationClient that provides additional features to wait for a build to finish.
 *
 * It is highly recommended to use the class inside a try-with-resources statement to properly cleanup the websocket
 * client. Otherwise the program using this class may hang indefinitely
 */
public class AdvancedBuildConfigurationClient extends BuildConfigurationClient implements AutoCloseable {

    private WebSocketClient webSocketClient = new VertxWebSocketClient();

    public AdvancedBuildConfigurationClient(Configuration configuration) {
        super(configuration);
    }

    public CompletableFuture<Build> waitForBuild(String buildConfigId) {
        webSocketClient.connect("ws://" + configuration.getHost() + BASE_PATH + "/notifications").join();

        return webSocketClient
                .catchBuildChangedNotification(
                        () -> fallbackSupplier(buildConfigId),
                        withBuildConfiguration(buildConfigId),
                        withBuildCompleted())
                .thenApply(BuildChangedNotification::getBuild)
                .whenComplete((x, y) -> webSocketClient.disconnect());
    }

    /**
     * Used to retrieve build through through REST when WS Client loses connection and reconnects
     *
     * @param bcId Id of the BuildConfig where the build was run
     * @return
     * @throws RemoteResourceException
     */
    private Build fallbackSupplier(String bcId) throws RemoteResourceException {
        BuildsFilterParameters parameters = new BuildsFilterParameters();
        parameters.setLatest(true);

        Build build = null;
        try (BuildConfigurationClient client = new BuildConfigurationClient(configuration)) {
            build = client.getBuilds(bcId, parameters).iterator().next();
        }
        return build;
    }

    public CompletableFuture<Build> executeBuild(String buildConfigId, BuildParameters parameters)
            throws RemoteResourceException {
        CompletableFuture<Build> future = waitForBuild(buildConfigId);
        super.trigger(buildConfigId, parameters);
        return future;
    }

    public CompletableFuture<Build> executeBuild(String buildConfigId, int revision, BuildParameters parameters)
            throws RemoteResourceException {
        CompletableFuture<Build> future = waitForBuild(buildConfigId);
        super.triggerRevision(buildConfigId, revision, parameters);
        return future;
    }

    public Build executeBuild(
            String buildConfigId,
            int revision,
            BuildParameters parameters,
            long timeout,
            TimeUnit timeUnit) throws RemoteResourceException {
        try {
            return executeBuild(buildConfigId, revision, parameters).get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            webSocketClient.disconnect();
        }
    }

    public Build executeBuild(String buildConfigId, BuildParameters parameters, long timeout, TimeUnit timeUnit)
            throws RemoteResourceException {
        try {
            return executeBuild(buildConfigId, parameters).get(timeout, timeUnit);
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
        super.close();
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
