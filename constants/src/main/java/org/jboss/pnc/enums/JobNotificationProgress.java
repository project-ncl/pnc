/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * Enum describing job progress in notifications.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @deprecated use pnc-api
 */
@Deprecated
public enum JobNotificationProgress {
    /**
     * The job is waiting. For example build waiting for dependencies.
     */
    PENDING,
    /**
     * The job is running. For example build is building.
     */
    IN_PROGRESS,
    /**
     * The job has finished. For example build failed.
     */
    FINISHED
}
