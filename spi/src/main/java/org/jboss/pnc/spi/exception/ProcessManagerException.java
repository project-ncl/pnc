/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.exception;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ProcessManagerException extends Exception {

    public ProcessManagerException(String message) {
        super(message);
    }

    public ProcessManagerException(String message, Exception e) {
        super(message, e);
    }
}
