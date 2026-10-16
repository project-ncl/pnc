/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.notification;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.JobNotificationType.BREW_PUSH;

import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Notification about Brew Push.
 * 
 * <pre>
 * Job: {@link JobNotificationType#BREW_PUSH} Notification type: {@code BREW_PUSH_RESULT}
 * Progress:{@link JobNotificationProgress#FINISHED} Message: no
 * 
 * <pre>
 * 
 * @deprecated since = "3.2", forRemoval = true;
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Deprecated
public class BuildPushResultNotification extends Notification {

    private static final String BREW_PUSH_RESULT = "BREW_PUSH_RESULT";

    /**
     * The result of the Brew Push.
     */
    private final BuildPushResult buildPushResult;

    @JsonCreator
    public BuildPushResultNotification(@JsonProperty("buildPushResult") BuildPushReport buildPushReport) {
        super(BREW_PUSH, BREW_PUSH_RESULT, FINISHED, IN_PROGRESS);

        BuildPushStatus status;
        if (buildPushReport.getResult() == null) {
            status = BuildPushStatus.ACCEPTED;
        } else {
            switch (buildPushReport.getResult()) {
                case SUCCESSFUL:
                    status = BuildPushStatus.SUCCESS;
                    break;
                case FAILED:
                    status = BuildPushStatus.FAILED;
                    break;
                case REJECTED:
                    status = BuildPushStatus.REJECTED;
                    break;
                case CANCELLED:
                    status = BuildPushStatus.CANCELED;
                    break;
                case TIMEOUT:
                case SYSTEM_ERROR:
                default:
                    status = BuildPushStatus.SYSTEM_ERROR;
            }
        }

        this.buildPushResult = BuildPushResult.builder()
                .status(status)
                .id(buildPushReport.getId())
                .brewBuildId(buildPushReport.getBrewBuildId())
                .brewBuildUrl(buildPushReport.getBrewBuildUrl())
                .buildId(buildPushReport.getBuild().getId())
                .userInitiator(buildPushReport.getUser().getUsername())
                .logContext(buildPushReport.getId())
                .build();
    }

}
