/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.notification;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.JobNotificationProgress.PENDING;
import static org.jboss.pnc.enums.JobNotificationType.BUILD;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildProgress;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Notification about change in Build.
 * 
 * <pre>
 * Job: {@link JobNotificationType#BUILD} Notification type: {@code BUILD_STATUS_CHANGED} Progress:
 * {@link JobNotificationProgress#PENDING} - build is new or waiting for dependencies
 * {@link JobNotificationProgress#IN_PROGRESS} - build is not in a final state {@link JobNotificationProgress#FINISHED}
 * - build is in final state Message: no
 * 
 * <pre>
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class BuildChangedNotification extends Notification {

    private static final String BUILD_STATUS_CHANGED = "BUILD_STATUS_CHANGED";

    /**
     * Previous status of the build.
     */
    private final BuildStatus oldStatus;

    /**
     * Build entity in the new state.
     */
    private final Build build;

    @JsonCreator
    public BuildChangedNotification(
            @JsonProperty("oldStatus") BuildStatus oldStatus,
            @JsonProperty("build") Build build) {
        super(
                BUILD,
                BUILD_STATUS_CHANGED,
                getProgress(build.getStatus().progress()),
                getProgress(oldStatus.progress()));
        this.oldStatus = oldStatus;
        this.build = build;
    }

    public static JobNotificationProgress getProgress(BuildProgress status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case PENDING:
                return PENDING;
            case FINISHED:
                return FINISHED;
            case IN_PROGRESS:
                return IN_PROGRESS;
            default:
                throw new UnsupportedOperationException("Unknown status " + status);
        }
    }
}
