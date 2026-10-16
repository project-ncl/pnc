/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.notification;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.JobNotificationProgress.PENDING;
import static org.jboss.pnc.enums.JobNotificationType.GROUP_BUILD;

import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Notification about change in Group Build.
 * 
 * <pre>
 * Job: {@link JobNotificationType#GROUP_BUILD} Notification type: {@code GROUP_BUILD_STATUS_CHANGED} Progress:
 * {@link JobNotificationProgress#IN_PROGRESS} - build is not in a final state {@link JobNotificationProgress#FINISHED}
 * - build is in final state Message: no
 * 
 * <pre>
 * For other failure notifications see {@link RepositoryCreationFailure}.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class GroupBuildChangedNotification extends Notification {

    private static final String GROUP_BUILD_STATUS_CHANGED = "GROUP_BUILD_STATUS_CHANGED";

    /**
     * Group Build entity in the new state.
     */
    private final GroupBuild groupBuild;

    @JsonCreator
    public GroupBuildChangedNotification(@JsonProperty("groupBuild") GroupBuild groupBuild) {
        super(
                GROUP_BUILD,
                GROUP_BUILD_STATUS_CHANGED,
                getProgress(groupBuild.getStatus()),
                getPreviousProgress(groupBuild.getStatus()));
        this.groupBuild = groupBuild;
    }

    public GroupBuildChangedNotification(GroupBuild groupBuild, BuildStatus oldStatus) {
        super(
                GROUP_BUILD,
                GROUP_BUILD_STATUS_CHANGED,
                getProgress(groupBuild.getStatus()),
                oldStatus == null ? PENDING : getProgress(oldStatus));
        this.groupBuild = groupBuild;
    }

    // mstodo could be problem
    public static JobNotificationProgress getProgress(BuildStatus status) {
        if (BuildStatus.NEW.equals(status)) {
            return PENDING;
        } else if (status.isFinal()) {
            return FINISHED;
        } else {
            return IN_PROGRESS;
        }
    }

    public static JobNotificationProgress getPreviousProgress(BuildStatus status) {
        if (BuildStatus.NEW.equals(status)) {
            return null;
        } else if (BuildStatus.NO_REBUILD_REQUIRED.equals(status)) {
            return PENDING;
        } else if (status.isFinal()) {
            return IN_PROGRESS;
        } else {
            return PENDING;
        }
    }

}
