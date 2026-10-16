/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.notifications;

import java.util.function.Consumer;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
