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
package org.jboss.pnc.integration.remote;

import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.ByteBufferSlicePool;
import org.xnio.IoFuture;
import org.xnio.OptionMap;
import org.xnio.Xnio;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class WebsocketListener {

    private Logger logger = LoggerFactory.getLogger(WebsocketListener.class);

    public WebsocketListener(URI uri, Consumer<String> onMessage) throws IOException {
        OptionMap optionMap = OptionMap.EMPTY;

        IoFuture<WebSocketChannel> ioFuture = WebSocketClient.connectionBuilder(
                Xnio.getInstance().createWorker(optionMap),
                new ByteBufferSlicePool(1024, 1024),
                uri).connect();

        WebSocketChannel webSocketChannel = ioFuture.get();
        webSocketChannel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                String messageData = message.getData();
                logger.debug("Received WS message {}.", messageData);
                onMessage.accept(messageData);
            }

            @Override
            protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
                logger.debug("Received binary WS message {}.", message.getData());
            }
        });
        webSocketChannel.resumeReceives();
        logger.info("Websocket listener started.");
    }
}
