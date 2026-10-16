/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.exception;

/**
 * Thrown when triggering a build of a given kind is not allowed at this PNC environment.
 */
public class BuildTriggeringException extends Exception {

    private static final long serialVersionUID = 1L;

    public BuildTriggeringException(String message) {
        super(message);
    }
}
