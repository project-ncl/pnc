/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.exception;

/**
 * Thrown when the user attempts to run an empty build or there are cycle dependencies in the build request.
 */
public class BuildRequestException extends Exception {

    private static final long serialVersionUID = 1L;

    public BuildRequestException(String message) {
        super(message);
    }

}
