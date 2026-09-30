/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * Enum describing build progress.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @deprecated use pnc-api
 */
@Deprecated
public enum BuildProgress {
    /**
     * The build is waiting.
     */
    PENDING,
    /**
     * The build is running.
     */
    IN_PROGRESS,
    /**
     * The build has finished. For example it failed.
     */
    FINISHED
}
