package org.jboss.pnc.restclient;

import static org.jboss.pnc.restclient.websocket.predicates.BuildChangedNotificationPredicates.withBuildCompleted;
import static org.jboss.pnc.restclient.websocket.predicates.BuildChangedNotificationPredicates.withBuildId;

import java.util.concurrent.CompletableFuture;

import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

public class AdvancedBuildConfigurationClient extends BuildConfigurationClient {

    private WebSocketClient webSocketClient;

    public AdvancedBuildConfigurationClient(Configuration configuration) {
        super(configuration);
        this.webSocketClient = new VertxWebSocketClient();
        webSocketClient.connect("ws://" + configuration.getHost() + "/pnc-rest-new/notification");
    }

    public CompletableFuture<Build> waitForBuild(String buildId) {
        return webSocketClient.catchBuildChangedNotification(withBuildId(buildId), withBuildCompleted())
                .thenApply(BuildChangedNotification::getBuild);
    }

    public CompletableFuture<Build> executeBuild(String buildConfigId, BuildParameters parameters)
            throws RemoteResourceException {
        Build build = super.trigger(buildConfigId, parameters);
        return waitForBuild(build.getId());
    }

}
