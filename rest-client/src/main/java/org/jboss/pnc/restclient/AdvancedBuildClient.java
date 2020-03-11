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

    private WebSocketClient webSocketClient;

    public AdvancedBuildClient(Configuration configuration) {
        super(configuration);
        this.webSocketClient = new VertxWebSocketClient();
        webSocketClient.connect("ws://" + configuration.getHost() + "/pnc-rest-new/notification");
    }

    public CompletableFuture<BuildPushResult> waitForBrewPush(String buildId) {
        return webSocketClient.catchBuildPushResult(withBuildId(buildId), withPushCompleted())
                .thenApply(BuildPushResultNotification::getBuildPushResult);
    }

    public CompletableFuture<BuildPushResult> executeBrewPush(String buildConfigId, BuildPushRequest parameters)
            throws RemoteResourceException {
        BuildPushResult push = super.push(buildConfigId, parameters);
        return waitForBrewPush(push.getBuildId());
    }
}
