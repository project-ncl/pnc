/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.notifications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint
public class NotificationCollector {

    private Logger logger = LoggerFactory.getLogger(NotificationCollector.class);

    private List<String> messages = new ArrayList<>();

    @OnMessage
    public void onMessage(String message) {
        logger.debug("Received notification {}.", message);
        messages.add(message);
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void clear() {
        messages.clear();
    }

}
