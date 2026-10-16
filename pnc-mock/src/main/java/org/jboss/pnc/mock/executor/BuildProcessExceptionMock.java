/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.executor;

import org.jboss.pnc.spi.environment.DestroyableEnvironment;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildProcessExceptionMock extends RuntimeException {

    private DestroyableEnvironment destroyableEnvironment;

    public BuildProcessExceptionMock(Throwable cause) {
        super(cause);
    }

    public BuildProcessExceptionMock(Throwable cause, DestroyableEnvironment destroyableEnvironment) {
        super(cause);
        this.destroyableEnvironment = destroyableEnvironment;
    }

    public DestroyableEnvironment getDestroyableEnvironment() {
        return destroyableEnvironment;
    }
}
