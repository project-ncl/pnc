/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.core.events;

import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

public class DefaultBuildStatusChangedEvent implements BuildStatusChangedEvent {

    private final BuildStatus oldStatus;
    private final BuildStatus newStatus;
    private final Integer buildTaskId;
    private final Integer userId;
    private final Integer buildConfigurationId;

    public DefaultBuildStatusChangedEvent(BuildStatus oldStatus, BuildStatus newStatus, Integer buildTaskId,
            Integer buildConfigurationId, Integer userId) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.buildTaskId = buildTaskId;
        this.buildConfigurationId = buildConfigurationId;
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
    public BuildStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public BuildStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public String toString() {
        return "DefaultBuildStatusChangedEvent{" +
                "oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", buildTaskId=" + buildTaskId +
                ", userId=" + userId +
                ", buildConfigurationId=" + buildConfigurationId +
                '}';
    }
}
