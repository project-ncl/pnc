/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient.exception;

/**
 * Rex returned 409 Conflict.
 */
public class ConflictResponseException extends RuntimeException {

    public ConflictResponseException() {
        super();
    }

    public ConflictResponseException(String message) {
        super(message);
    }

    public ConflictResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflictResponseException(Throwable cause) {
        super(cause);
    }
}
