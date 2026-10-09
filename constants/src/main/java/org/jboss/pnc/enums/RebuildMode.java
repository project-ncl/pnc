/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @deprecated use pnc-api
 */
@Deprecated
public enum RebuildMode {

    /**
     * Check automatically captured dependencies on {@link org.jboss.pnc.model.BuildRecord}.
     */
    IMPLICIT_DEPENDENCY_CHECK,

    /**
     * Check the user defined dependencies on {@link org.jboss.pnc.model.BuildConfiguration}.
     */
    EXPLICIT_DEPENDENCY_CHECK,

    /**
     * Don't check anything and run the build anyway.
     */
    FORCE
}
