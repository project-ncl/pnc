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
import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.JobNotificationType.BREW_PUSH;

/**
 * Notification about Brew Push.
 * 
 * <pre>
 * Job: {@link JobNotificationType#BREW_PUSH} Notification type: {@code BREW_PUSH_RESULT}
 * Progress:{@link JobNotificationProgress#FINISHED} Message: no
 * 
 * <pre>
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Deprecated(forRemoval = true, since = "3.2")
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
