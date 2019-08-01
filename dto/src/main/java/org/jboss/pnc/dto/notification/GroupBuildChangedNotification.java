/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.GroupBuild;

import lombok.Data;

import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.JobNotificationProgress;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.JobNotificationType.GROUP_BUILD;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class GroupBuildChangedNotification extends Notification {

    private static final String GROUP_BUILD_STATUS_CHANGED = "GROUP_BUILD_STATUS_CHANGED";

    private final GroupBuild groupBuild;

    @JsonCreator
    public GroupBuildChangedNotification(@JsonProperty("groupBuild") GroupBuild groupBuild) {
        super(GROUP_BUILD, GROUP_BUILD_STATUS_CHANGED, getProgress(groupBuild.getStatus()));
        this.groupBuild = groupBuild;
    }

    public static JobNotificationProgress getProgress(BuildStatus status) {
        if (status.isFinal()) {
            return FINISHED;
        } else {
            return IN_PROGRESS;
        }
    }
}
