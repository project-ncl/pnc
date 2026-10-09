/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * Status of a push of a build to Koji.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @deprecated use pnc-api
 */
@Deprecated
public enum BuildPushStatus {
    /**
     * Push was accepted and is in progress.
     */
    ACCEPTED,
    /**
     * Build was successfuly pushed to Koji.
     */
    SUCCESS,
    /**
     * Push was rejected for some reason.
     */
    REJECTED,
    /**
     * Push failed because of user-side issue.
     */
    FAILED,
    /**
     * Push failed because of server-side issue.
     */
    SYSTEM_ERROR,
    /**
     * Push was canceled.
     */
    CANCELED
}
