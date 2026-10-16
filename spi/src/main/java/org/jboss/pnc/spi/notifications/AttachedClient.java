/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.notifications;

/**
 * A generic WS client.
 */
public interface AttachedClient {

    /**
     * @return Returns <code>true</code> if enabled.
     */
    boolean isEnabled();

    String getSessionId();

    /**
     * Sends a message to the client
     *
     * @param messageBody Message body - depends on implementation how to deal with it.
     * @param callback the callback from the asynch method
     */
    void sendMessage(Object messageBody, MessageCallback callback);

}
