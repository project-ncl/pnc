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

import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.ProductMilestoneClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.notification.ProductMilestoneCloseResultNotification;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

import java.util.concurrent.CompletableFuture;

import static org.jboss.pnc.restclient.websocket.predicates.ProductMilestoneCloseResultNotificationPredicates.withMilestoneId;

public class AdvancedProductMilestoneClient extends ProductMilestoneClient {

    private WebSocketClient webSocketClient = new VertxWebSocketClient();

    public AdvancedProductMilestoneClient(Configuration configuration) {
        super(configuration);
    }

    public CompletableFuture<ProductMilestoneCloseResult> waitForMilestoneClose(String milestoneId) {
        webSocketClient.connect("ws://" + configuration.getHost() + BASE_PATH + "/notifications").join();
        return webSocketClient.catchProductMilestoneCloseResult(withMilestoneId(milestoneId))
                .thenApply(ProductMilestoneCloseResultNotification::getProductMilestoneCloseResult)
                .whenComplete((x, y) -> webSocketClient.disconnect());
    }

    public CompletableFuture<ProductMilestoneCloseResult> executeMilestoneClose(String milestoneId)
            throws RemoteResourceException {
        CompletableFuture<ProductMilestoneCloseResult> future = waitForMilestoneClose(milestoneId);
        super.closeMilestone(milestoneId);
        return future;
    }

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
