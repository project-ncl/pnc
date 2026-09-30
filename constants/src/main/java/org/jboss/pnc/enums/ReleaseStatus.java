/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/26/16 Time: 2:39 PM
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum ReleaseStatus {
    SUCCESS(MilestoneCloseStatus.SUCCEEDED),
    FAILURE(MilestoneCloseStatus.FAILED),
    IMPORT_ERROR(MilestoneCloseStatus.FAILED),
    SET_UP_ERROR(MilestoneCloseStatus.SYSTEM_ERROR);

    private final MilestoneCloseStatus milestoneReleaseStatus;

    ReleaseStatus(MilestoneCloseStatus milestoneReleaseStatus) {
        this.milestoneReleaseStatus = milestoneReleaseStatus;
    }

    public MilestoneCloseStatus getMilestoneReleaseStatus() {
        return milestoneReleaseStatus;
    }
}
