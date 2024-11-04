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

import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.ProductMilestoneClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.notification.ProductMilestoneCloseResultNotification;
import org.jboss.pnc.rest.api.parameters.ProductMilestoneCloseParameters;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.jboss.pnc.restclient.websocket.predicates.ProductMilestoneCloseResultNotificationPredicates.isFinished;
import static org.jboss.pnc.restclient.websocket.predicates.ProductMilestoneCloseResultNotificationPredicates.withMilestoneId;

/**
 * AdvancedProductMilestoneClient that provides additional features to wait for a ProductMilestone to finish closing.
 * 
 * It is necessary to use the class inside a try-with-resources statement to properly cleanup the websocket client.
 * Otherwise the program using this class may hang indefinitely.
 */
public class AdvancedProductMilestoneClient extends ProductMilestoneClient implements AutoCloseable {

    private WebSocketClient webSocketClient = new VertxWebSocketClient();

    public AdvancedProductMilestoneClient(Configuration configuration) {
        super(configuration);
    }

    public CompletableFuture<ProductMilestoneCloseResult> waitForMilestoneClose(String milestoneId) {
        webSocketClient.connect("wss://" + configuration.getHost() + BASE_PATH + "/notifications").join();
        return webSocketClient
                .catchProductMilestoneCloseResult(
                        () -> fallbackSupplier(milestoneId),
                        withMilestoneId(milestoneId),
                        isFinished())
                .thenApply(ProductMilestoneCloseResultNotification::getProductMilestoneCloseResult);
    }

    /**
     * Used to retrieve latest close result through REST when WS Client loses connection and reconnects
     *
     * @param milestoneId Id of the ProductMilestone which was closed
     * @return
     * @throws RemoteResourceException
     */
    private ProductMilestoneCloseResult fallbackSupplier(String milestoneId) throws RemoteResourceException {
        ProductMilestoneCloseParameters parameters = new ProductMilestoneCloseParameters();
        parameters.setLatest(true);

        ProductMilestoneCloseResult result = null;
        try (ProductMilestoneClient client = new ProductMilestoneClient(configuration)) {
            result = client.getCloseResults(milestoneId, parameters).iterator().next();
        }
        return result;
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
