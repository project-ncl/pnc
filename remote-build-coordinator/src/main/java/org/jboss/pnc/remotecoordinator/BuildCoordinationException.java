/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildCoordinationException extends Exception {
    public BuildCoordinationException(String message) {
        super(message);
    }

    public BuildCoordinationException(String message, Throwable cause) {
        super(message, cause);
    }
}
