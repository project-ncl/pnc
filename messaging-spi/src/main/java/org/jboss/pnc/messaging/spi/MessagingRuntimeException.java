/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.messaging.spi;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MessagingRuntimeException extends RuntimeException {

    public MessagingRuntimeException(Exception e) {
        super(e);
    }

    public MessagingRuntimeException(String message) {
        super(message);
    }

    public MessagingRuntimeException(String message, Exception e) {
        super(message, e);
    }
}
