/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;

import java.util.Date;

public class DefaultBuildStatusChangedEvent implements BuildCoordinationStatusChangedEvent {

    private final BuildCoordinationStatus oldStatus;
    private final BuildCoordinationStatus newStatus;
    private final Integer buildTaskId;
    private final Integer userId;
    private final Integer buildConfigurationId;
    private final Integer buildConfigurationRevision;
    private final String buildConfigurationName;
    private final Date buildStartTime;
    private final Date buildEndTime;

    public DefaultBuildStatusChangedEvent(
            BuildCoordinationStatus oldStatus,
            BuildCoordinationStatus newStatus,
            Integer buildTaskId,
            Integer buildConfigurationId,
            Integer buildConfigurationRevision,
            String buildConfigurationName,
            Date buildStartTime,
            Date buildEndTime,
            Integer userId) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.buildTaskId = buildTaskId;
        this.buildConfigurationId = buildConfigurationId;
        this.buildConfigurationRevision = buildConfigurationRevision;
        this.buildConfigurationName = buildConfigurationName;
        this.buildStartTime = buildStartTime;
        this.buildEndTime = buildEndTime;
        this.userId = userId;
    }

    @Override
    public Integer getBuildTaskId() {
        return buildTaskId;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }

    @Override
    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    @Override
    public Integer getBuildConfigurationRevision() {
        return buildConfigurationRevision;
    }

    @Override
    public String getBuildConfigurationName() {
        return buildConfigurationName;
    }

    @Override
    public BuildCoordinationStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public BuildCoordinationStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public Date getBuildStartTime() {
        return buildStartTime;
    }

    @Override
    public Date getBuildEndTime() {
        return buildEndTime;
    }

    @Override
    public String toString() {

        return "DefaultBuildStatusChangedEvent{" +
                "oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", buildTaskId=" + buildTaskId +
                ", userId=" + userId +
                ", buildConfigurationId=" + buildConfigurationId +
                ", buildConfigurationName=" + buildConfigurationName +
                ", buildStartTime=" + buildStartTime +
                ", buildEndTime=" + buildEndTime +
                '}';
    }
}
