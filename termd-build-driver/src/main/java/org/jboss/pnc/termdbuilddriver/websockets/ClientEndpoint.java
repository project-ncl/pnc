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

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

import static javax.websocket.CloseReason.CloseCodes;

/**
* @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
*/
public class ClientEndpoint extends Endpoint {
    private final Logger logger = LoggerFactory.getLogger(ClientEndpoint.class);

    Session session;
    private ClientMessageHandler messageHandler;

    public ClientEndpoint(ClientMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        logger.debug("Client received open.");
        this.session = session;

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                logger.trace("Client received text MESSAGE: {}", message);
                messageHandler.onMessage(message);
            }
        });
        session.addMessageHandler(new MessageHandler.Whole<byte[]>() {
            @Override
            public void onMessage(byte[] bytes) {
                logger.trace("Client received binary MESSAGE: {}", new String(bytes));
                messageHandler.onMessage(bytes);
            }
        });
    }

    public void close() {
        try {
            session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Bye."));
        } catch (IOException e) {
            logger.error("Cannot close web socket session.", e);
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        logger.debug("Closing client session [{}].", session.getId());
    }

    @Override
    public void onError(Session session, Throwable thr) {
        logger.error("Error in session " + session.getId(), thr);
    }

    public void sendBinary(ByteBuffer data) throws IOException {
        session.getBasicRemote().sendBinary(data);
    }
}
