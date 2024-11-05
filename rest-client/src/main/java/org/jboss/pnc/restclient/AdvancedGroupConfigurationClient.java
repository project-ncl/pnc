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

import static org.jboss.pnc.restclient.websocket.predicates.GroupBuildChangedNotificationPredicates.withGBuildCompleted;
import static org.jboss.pnc.restclient.websocket.predicates.GroupBuildChangedNotificationPredicates.withGConfigId;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.rest.api.parameters.GroupBuildParameters;
import org.jboss.pnc.rest.api.parameters.GroupBuildsFilterParameters;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

/**
 * AdvancedGroupConfigurationClient that provides additional features to wait for a group build to finish.
 *
 * It is necessary to use the class inside a try-with-resources statement to properly cleanup the websocket client.
 * Otherwise the program using this class may hang indefinitely.
 */
public class AdvancedGroupConfigurationClient extends GroupConfigurationClient implements AutoCloseable {

    private WebSocketClient webSocketClient = new VertxWebSocketClient();

    public AdvancedGroupConfigurationClient(Configuration configuration) {
        super(configuration);
    }

    public CompletableFuture<GroupBuild> waitForGroupBuild(String groupConfigId) {
        webSocketClient
                .connect(configuration.getWSProtocol() + "://" + configuration.getHost() + BASE_PATH + "/notifications")
                .join();

        return webSocketClient
                .catchGroupBuildChangedNotification(
                        () -> fallbackSupplier(groupConfigId),
                        withGConfigId(groupConfigId),
                        withGBuildCompleted())
                .thenApply(GroupBuildChangedNotification::getGroupBuild);
    }

    /**
     * Used to retrieve group build through REST when WS Client loses connection and reconnects
     *
     * @param gcId Id of the GroupConfig where the build was run
     * @return
     * @throws RemoteResourceException
     */
    private GroupBuild fallbackSupplier(String gcId) throws RemoteResourceException {
        GroupBuild build = null;
        try (GroupConfigurationClient client = new GroupConfigurationClient(configuration)) {
            build = client
                    .getAllGroupBuilds(
                            gcId,
                            new GroupBuildsFilterParameters(),
                            Optional.of("=desc=startTime"),
                            Optional.empty())
                    .iterator()
                    .next();
        }
        return build;
    }

    public CompletableFuture<GroupBuild> executeGroupBuild(String groupConfigId, GroupBuildParameters parameters)
            throws RemoteResourceException {
        CompletableFuture<GroupBuild> future = waitForGroupBuild(groupConfigId);
        super.trigger(groupConfigId, parameters, GroupBuildRequest.builder().build());
        return future;
    }

    public GroupBuild executeGroupBuild(
            String groupConfigId,
            GroupBuildParameters parameters,
            long timeout,
            TimeUnit timeUnit) throws RemoteResourceException {
        try {
            return executeGroupBuild(groupConfigId, parameters).get(timeout, timeUnit);
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
