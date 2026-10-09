/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore;

/**
 * Thrown when Datastore is unable to process a request.
 */
public class DatastoreException extends Exception {
    public DatastoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
