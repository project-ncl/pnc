/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.exception;

public class MissingDataException extends CoreException {
    public MissingDataException(String message) {
        super(message);
    }

    public MissingDataException(Exception e) {
        super(e);
    }

    public MissingDataException(String message, Exception e) {
        super(message, e);
    }
}
