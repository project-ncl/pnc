/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * Status of the process of milestone closing.
 *
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/30/16 Time: 1:16 PM
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum MilestoneCloseStatus {

    /**
     * Milestone close is in progress.
     */
    IN_PROGRESS,
    /**
     * Milestone close failed because of user-side issue.
     */
    FAILED,
    /**
     * Milestone close finished successfully and builds were pushed to Koji.
     */
    SUCCEEDED,
    /**
     * Milestone close was canceled.
     */
    CANCELED,
    /**
     * Milestone close failed because of server-side issue.
     */
    SYSTEM_ERROR
}
