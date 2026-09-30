/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @deprecated use pnc-api
 */
@Deprecated
public enum ArtifactQuality { // TODO mark all deprecated
    /**
     * The artifact has not yet been verified or tested.
     */
    NEW,
    /**
     * The artifact has been verified by an automated process, but has not yet been tested against a complete product or
     * other large set of components.
     */
    VERIFIED,
    /**
     * The artifact has passed integration testing.
     */
    TESTED,
    /**
     * The artifact should no longer be used due to lack of support and/or a better alternative being available.
     */
    DEPRECATED,
    /**
     * The artifact contains a severe defect, possibly a functional or security issue.
     */
    BLACKLISTED,
    /**
     * Artifact with DELETED quality is used to show BuildRecord dependencies although the artifact itself was deleted
     * OR can identify artifacts, which are were removed from repository manager (e.g. due to conflicts), but the
     * metadata were kept for archival purposes.
     */
    DELETED,
    /**
     * The artifact is built as temporary and it is planned to remove it later. The artifact cannot be used for product
     * releases.
     */
    TEMPORARY,
    /**
     * The artifact was not built inhouse and was imported from outside world.
     */
    IMPORTED

}
