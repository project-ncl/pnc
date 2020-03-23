/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import static org.jboss.pnc.restclient.websocket.predicates.BuildPushResultNotificationPredicates.withBuildId;
import static org.jboss.pnc.restclient.websocket.predicates.BuildPushResultNotificationPredicates.withPushCompleted;

import java.util.concurrent.CompletableFuture;

import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.notification.BuildPushResultNotification;
import org.jboss.pnc.dto.requests.BuildPushRequest;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

public class AdvancedBuildClient extends BuildClient {

    public AdvancedBuildClient(Configuration configuration) {
        super(configuration);
    }

    public CompletableFuture<BuildPushResult> waitForBrewPush(String buildId) {
        WebSocketClient webSocketClient = new VertxWebSocketClient();

        webSocketClient.connect("ws://" + configuration.getHost() + "/pnc-rest-new/notifications").join();

        return webSocketClient.catchBuildPushResult(withBuildId(buildId), withPushCompleted())
                .thenApply(BuildPushResultNotification::getBuildPushResult)
                .whenComplete((x, y) -> webSocketClient.disconnect().join());
    }

    public CompletableFuture<BuildPushResult> executeBrewPush(String buildConfigId, BuildPushRequest parameters)
            throws RemoteResourceException {
        BuildPushResult push = super.push(buildConfigId, parameters);
        return waitForBrewPush(push.getBuildId());
    }
}
