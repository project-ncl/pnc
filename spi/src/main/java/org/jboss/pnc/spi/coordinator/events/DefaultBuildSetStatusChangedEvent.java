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

import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import java.util.Date;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultBuildSetStatusChangedEvent implements BuildSetStatusChangedEvent {

    private final BuildSetStatus oldStatus;
    private final BuildSetStatus newStatus;
    private final Integer buildSetTaskId;
    private final Integer buildSetConfigurationId;
    private final String buildSetConfigurationName;
    private final Date buildSetStartTime;
    private final Date buildSetEndTime;
    private final Integer userId;

    public DefaultBuildSetStatusChangedEvent(BuildSetStatus oldStatus, BuildSetStatus newStatus, Integer buildSetTaskId,
            Integer buildSetConfigurationId, String buildSetConfigurationName, Date buildSetStartTime,
            Date buildSetEndTime, Integer userId) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.buildSetTaskId = buildSetTaskId;
        this.buildSetConfigurationId = buildSetConfigurationId;
        this.buildSetConfigurationName = buildSetConfigurationName;
        this.buildSetStartTime = buildSetStartTime;
        this.buildSetEndTime = buildSetEndTime;
        this.userId = userId;
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
    public Integer getBuildSetTaskId() {
        return buildSetTaskId;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }

    @Override
    public Integer getBuildSetConfigurationId() {
        return buildSetConfigurationId;
    }

    @Override
    public String getBuildSetConfigurationName() {
        return buildSetConfigurationName;
    }

    @Override
    public Date getBuildSetStartTime() {
        return buildSetStartTime;
    }

    @Override
    public Date getBuildSetEndTime() {
        return buildSetEndTime;
    }

    @Override
    public String toString() {
        return "DefaultBuildSetStatusChangedEvent{" + "oldStatus=" + oldStatus + ", newStatus=" + newStatus
                + ", buildSetTaskId=" + buildSetTaskId + ", buildSetConfigurationId=" + buildSetConfigurationId
                + ", buildSetConfigurationName=" + buildSetConfigurationName + ", buildSetStartTime="
                + buildSetStartTime + ", buildSetEndTime=" + buildSetEndTime + ", userId=" + userId + '}';
    }
}
