/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

/**
 * Unchecked exception (unlike {@link org.jboss.pnc.spi.exception.BuildTriggeringException}) thrown when there is a
 * problem with triggering a build.
 */
public class BuildTriggeringNotAllowedException extends RuntimeException {

    public BuildTriggeringNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
