/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.dto.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildProgress;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.JobNotificationProgress.PENDING;
import static org.jboss.pnc.enums.JobNotificationType.BUILD;

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
