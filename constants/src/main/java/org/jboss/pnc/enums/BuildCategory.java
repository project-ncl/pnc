/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * Enum identifying category of a build.
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 * @deprecated use pnc-api
 */
@Deprecated
public enum BuildCategory {
    /**
     * The build is built to be used in On-Premise IBM products.
     */
    STANDARD,
    /**
     * The build is built to be used in On-Premise Red Hat products.
     */
    LEGACY_REDHAT,
    /**
     * The build is built to be used in Managed services only.
     */
    SERVICE,
    /**
     * The build is built to be used for Project Lightwell only.
     */
    LIGHTWELL,
    /**
     * The build is built to be used for Project Lightwell upstream (no suffix) only
     */
    LIGHTWELL_UPSTREAM,
    /**
     * The build is built to be used for Project Lightwell Novel builds only.
     */
    LIGHTWELL_NOVEL,
}
