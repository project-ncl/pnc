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
package org.jboss.pnc.spi.coordinator.events;

import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import java.util.Date;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultBuildSetStatusChangedEvent implements BuildSetStatusChangedEvent {

    private final BuildSetStatus oldStatus;
    private final BuildSetStatus newStatus;
    private final GroupBuild groupBuild;
    private final String description;

    public DefaultBuildSetStatusChangedEvent(
            BuildSetStatus oldStatus,
            BuildSetStatus newStatus,
            GroupBuild groupBuild,
            String description) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.groupBuild = groupBuild;
        this.description = description;
    }

    @Override
    public GroupBuild getGroupBuild() {
        return groupBuild;
    }

    @Override
    public BuildSetStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public BuildSetStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public String getBuildSetTaskId() {
        return groupBuild.getId();
    }

    @Override
    public String getUserId() {
        return groupBuild.getUser() == null ? null : groupBuild.getUser().getId();
    }

    @Override
    public String getBuildSetConfigurationId() {
        return groupBuild.getGroupConfig().getId();
    }

    @Override
    public String getBuildSetConfigurationName() {
        return groupBuild.getGroupConfig().getName();
    }

    @Override
    public Date getBuildSetStartTime() {
        return Date.from(groupBuild.getStartTime());
    }

    @Override
    public Date getBuildSetEndTime() {
        return Date.from(groupBuild.getEndTime());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "DefaultBuildSetStatusChangedEvent{" + "oldStatus=" + oldStatus + ", newStatus=" + newStatus
                + ", groupBuild=" + groupBuild + '}';
    }
}
