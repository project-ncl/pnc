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
package org.jboss.pnc.integration.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import java.util.function.Consumer;

@ClientEndpoint
public class UpdatesMessageHandler {

    private Logger logger = LoggerFactory.getLogger(UpdatesMessageHandler.class);

    private Consumer<String> onMessage;

    public UpdatesMessageHandler(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    @OnMessage
    public void onMessage(String message) {
        logger.debug("Received notification {}.", message);
        onMessage.accept(message);
    }

}
