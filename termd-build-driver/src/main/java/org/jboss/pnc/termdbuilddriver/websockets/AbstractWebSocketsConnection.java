/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.termdbuilddriver.websockets;

import io.undertow.websockets.jsr.UndertowContainerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;

public abstract class AbstractWebSocketsConnection implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected final URI uri;
    protected volatile Optional<Session> session = Optional.empty();

    public AbstractWebSocketsConnection(URI uri) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uri);
        uriBuilder.scheme("ws");
        this.uri = uriBuilder.build();
    }

    public void connect() {
        try {
            logger.debug("Connecting to Web Sockets URI {}", uri);

            WebSocketContainer webSocketContainer = new WSContainerProvider().getContainer();
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();
            logger.debug("Connecting to WebSocket server using [{}] provider.", webSocketContainer.getClass());
            Session session = webSocketContainer.connectToServer(getClientEndpoint(), config, uri);
            logger.debug("Connected session [{}] to Web Sockets URI {}", session.getId(), uri);
        } catch (Exception e) {
            throw new TermdConnectionException("Could not connect to Web Sockets " + uri, e);
        }
    }

    protected abstract ClientEndpoint getClientEndpoint();

    public void disconnect() {
        logger.debug("Disconnecting from Web Sockets URI {}", uri);
        getClientEndpoint().close();
    }

    public void sendAsBinary(ByteBuffer data) throws IOException {
        getClientEndpoint().sendBinary(data);
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }

    private class WSContainerProvider extends UndertowContainerProvider { //TODO do we need to extend implementation specific ?
        @Override
        protected WebSocketContainer getContainer() {
            return super.getContainer();
        }
    }

}
