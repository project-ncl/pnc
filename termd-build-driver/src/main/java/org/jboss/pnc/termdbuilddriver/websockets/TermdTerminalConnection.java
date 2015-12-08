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

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TermdTerminalConnection extends AbstractWebSocketsConnection {

    private final Logger logger = LoggerFactory.getLogger(TermdTerminalConnection.class);


    private static final String WEB_SOCKET_TERMINAL_PATH = "socket/term";
    ClientEndpoint clientEndpoint;

    public TermdTerminalConnection(URI serverBaseUri) {
        super(serverBaseUri.resolve(WEB_SOCKET_TERMINAL_PATH));
        clientEndpoint = new ClientEndpoint(new TerminalConnectionMessageHandler());
    }

    @Override
    protected ClientEndpoint getClientEndpoint() {
        return clientEndpoint;
    }

    public URI getLogsURI() {
        return URI.create(uri.toString() + "?sessionId=reconnect");
    }

    private class TerminalConnectionMessageHandler implements ClientMessageHandler {

        StringBuilder responseBuffer = new StringBuilder();

        @Override
        public void onMessage(byte[] bytes) {
            if (logger.isTraceEnabled()) {
                String string = new String(bytes);
                if (string.equals("\r\n")) {
                    logger.trace(responseBuffer.toString());
                    responseBuffer = new StringBuilder();
                } else {
                    responseBuffer.append(string);
                }
            }
        }

        @Override
        public void onMessage(String message) {
        }
    }
}
