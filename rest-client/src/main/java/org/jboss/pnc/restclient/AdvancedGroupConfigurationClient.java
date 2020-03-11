package org.jboss.pnc.restclient;

import static org.jboss.pnc.restclient.websocket.predicates.GroupBuildChangedNotificationPredicates.withGBuildCompleted;
import static org.jboss.pnc.restclient.websocket.predicates.GroupBuildChangedNotificationPredicates.withGBuildId;

import java.util.concurrent.CompletableFuture;

import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.rest.api.parameters.GroupBuildParameters;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

public class AdvancedGroupConfigurationClient extends GroupConfigurationClient {
    private WebSocketClient webSocketClient;

    public AdvancedGroupConfigurationClient(Configuration configuration) {
        super(configuration);
        this.webSocketClient = new VertxWebSocketClient();
        webSocketClient.connect("ws://" + configuration.getHost() + "/pnc-rest-new/notification");
    }

    public CompletableFuture<GroupBuild> waitForGroupBuild(String buildId) {
        return webSocketClient.catchGroupBuildChangedNotification(withGBuildId(buildId), withGBuildCompleted())
                .thenApply(GroupBuildChangedNotification::getGroupBuild);
    }

    public CompletableFuture<GroupBuild> executeGroupBuild(String groupConfigId, GroupBuildParameters parameters)
            throws RemoteResourceException {
        GroupBuild groupBuild = super.trigger(groupConfigId, parameters, GroupBuildRequest.builder().build());
        return waitForGroupBuild(groupBuild.getId());
    }
}
