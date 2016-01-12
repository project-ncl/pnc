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

import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultBuildSetStatusChangedEvent implements BuildSetStatusChangedEvent {

    private final BuildSetStatus oldStatus;
    private final BuildSetStatus newStatus;
    private final Integer buildSetTaskId;
    private final Integer userId;
    private final Integer buildSetConfigurationId;
    private final String buildSetConfigurationName;

    public DefaultBuildSetStatusChangedEvent(BuildSetStatus oldStatus, BuildSetStatus newStatus, Integer buildSetTaskId,
            Integer buildSetConfigurationId, String buildSetConfigurationName, Integer userId) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.buildSetTaskId = buildSetTaskId;
        this.buildSetConfigurationId = buildSetConfigurationId;
        this.buildSetConfigurationName = buildSetConfigurationName;
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
    public String toString() {
        return "DefaultBuildSetStatusChangedEvent{" + "oldStatus=" + oldStatus + ", newStatus=" + newStatus
                + ", buildSetTaskId=" + buildSetTaskId + ", userId=" + userId + ", buildSetConfigurationId="
                + buildSetConfigurationId + ", buildSetConfigurationName=" + buildSetConfigurationName + '}';
    }
}
