/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification.dist;

/**
 * Not distributed at all. It just delegates the event directly to this node's notifier.
 */
public class LocalEventHandler extends AbstractDistributedEventHandler {

    @Override
    public void sendEvent(Object event) {
        sendMessage(toMessage(event));
    }

    @Override
    public void start() {
    }

    @Override
    public void close() {
    }
}
