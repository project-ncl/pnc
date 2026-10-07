/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * Enum describing asynchonous job types in notifications.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @deprecated use pnc-api
 */
@Deprecated
public enum JobNotificationType {
    /**
     * Job type representing single build.
     */
    BUILD,
    /**
     * Job type representing group build.
     */
    GROUP_BUILD,
    /**
     * Job type representing import of a build into Brew.
     */
    BREW_PUSH,
    /**
     * Job type representing asynchronous creation of SCM Repository.
     */
    SCM_REPOSITORY_CREATION,
    /**
     * Job type representing asynchronous creation of Build Config together with SCM Repository.
     */
    BUILD_CONFIG_CREATION,
    /**
     * Job type representing generic operation-type job.
     */
    OPERATION,
    /**
     * Job type representing a change in the generic setting.
     */
    GENERIC_SETTING,
    /**
     * Job type representing closing of milestone
     */
    PRODUCT_MILESTONE_CLOSE,
}
