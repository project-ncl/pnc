/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

/**
 * Thrown by Brew-related endpoints.
 */
public class BrewNotSupportedException extends OperationNotAllowedException {

    private static final String MESSAGE = "Brew is not available in the current system.";

    public BrewNotSupportedException() {
        super(MESSAGE);
    }

    public BrewNotSupportedException(String detail) {
        super(MESSAGE + " " + detail);
    }
}
