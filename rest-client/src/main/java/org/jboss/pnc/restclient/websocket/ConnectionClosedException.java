/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.restclient.websocket;

/**
 * Thrown when there is attempt to access WebSocket Connection that is closed.
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
public class ConnectionClosedException extends Exception {
    public ConnectionClosedException() {
    }

    public ConnectionClosedException(String message) {
        super(message);
    }

    public ConnectionClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionClosedException(Throwable cause) {
        super(cause);
    }

    public ConnectionClosedException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
