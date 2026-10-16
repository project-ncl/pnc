/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.notifications;

/**
 * Notification mechanism for Web Sockets. All implementation details should be placed in AttachedClient.
 */
public interface Notifier {

    void attachClient(AttachedClient attachedClient);

    void detachClient(AttachedClient attachedClient);

    int getAttachedClientsCount();

    void sendMessage(Object message);

    MessageCallback getCallback();

}
