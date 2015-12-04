/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;

@ClientEndpoint
public class AbstractWebSocketsConnection implements AutoCloseable {

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
            this.session = Optional.of(ContainerProvider.getWebSocketContainer().connectToServer(this, uri));
            logger.debug("Connected to Web Sockets URI {}", uri);
        } catch (Exception e) {
            throw new TermdConnectionException("Could not connect to Web Sockets " + uri, e);
        }
    }

    public void disconnect() {
        logger.debug("Disconnecting from Web Sockets URI {}", uri);
        if (session.isPresent()) {
            try {
                session.get().close();
                logger.debug("Session {} closed.", uri);
            } catch (IOException e) {
                logger.warn("Unable to closeSession WebSockets session", e);
            }
        }
        session = Optional.empty();
    }

    public void sendAsBinary(ByteBuffer data) throws IOException {
        Session currentSession = session.orElseThrow(() -> new TermdConnectionException("Not connected"));
        currentSession.getBasicRemote().sendBinary(data);
    }

    @OnClose
    public void onClose() {
        this.session = Optional.empty();
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
