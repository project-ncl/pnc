/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration.websockets;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.rest.notifications.websockets.MessageType;
import org.jboss.pnc.rest.notifications.websockets.NotificationsEndpoint;
import org.jboss.pnc.rest.notifications.websockets.ProgressUpdatesRequest;
import org.jboss.pnc.rest.notifications.websockets.TypedMessage;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class WsUpdatesClient {

    public void subscribeBlocking(String topic, String filter, Consumer<String> onMessage) throws IOException, DeploymentException {

        ProgressUpdatesRequest progressUpdatesRequest = ProgressUpdatesRequest.subscribe(topic, filter);

        UpdatesMessageHandler updatesMessageHandler = new UpdatesMessageHandler(onMessage);

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://localhost:8080/pnc-rest/" + NotificationsEndpoint.ENDPOINT_PATH;
        Session session = container.connectToServer(updatesMessageHandler, URI.create(uri));

        RemoteEndpoint.Basic asyncRemote = session.getBasicRemote();
        asyncRemote.sendText(toJson(progressUpdatesRequest));
    }

    public String toJson(ProgressUpdatesRequest progressUpdatesRequest) {
        return JsonOutputConverterMapper.apply(
                new TypedMessage<ProgressUpdatesRequest>(MessageType.PROCESS_UPDATES, progressUpdatesRequest));
    }

}
