/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.executor.exceptions;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class ExecutorException extends Exception {
    public ExecutorException(String message) {
        super(message);
    }

    public ExecutorException(Throwable e) {
        super(e);
    }

    public ExecutorException(String message, Exception e) {
        super(message, e);
    }
}
