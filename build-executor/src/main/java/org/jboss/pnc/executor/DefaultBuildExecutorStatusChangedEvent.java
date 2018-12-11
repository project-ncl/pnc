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

package org.jboss.pnc.executor;

import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;

import java.util.Optional;

class DefaultBuildExecutorStatusChangedEvent implements BuildExecutionStatusChangedEvent {

    private final BuildExecutionStatus oldStatus;
    private final BuildExecutionStatus newStatus;
    private final Integer buildTaskId;
    private final Integer buildConfigurationId;
    private final Optional<BuildResult> buildResult;

    private boolean isFinal;

    public DefaultBuildExecutorStatusChangedEvent(
            BuildExecutionStatus oldStatus,
            BuildExecutionStatus newStatus,
            Integer buildTaskId,
            Integer buildConfigurationId,
            Optional<BuildResult> buildResult,
            boolean isFinal) {

        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.buildTaskId = buildTaskId;
        this.buildConfigurationId = buildConfigurationId;
        this.buildResult = buildResult;
        this.isFinal = isFinal;
    }

    @Override
    public Integer getBuildTaskId() {
        return buildTaskId;
    }

    @Override
    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    @Override
    public Optional<BuildResult> getBuildResult() {
        return buildResult;
    }

    @Override
    public BuildExecutionStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public BuildExecutionStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public String toString() {
        return "DefaultBuildExecutionStatusChangedEvent{" +
                "oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", buildTaskId=" + buildTaskId +
                ", buildConfigurationId=" + buildConfigurationId +
                ", buildResult=" + buildResult +
                ", isFinal=" + isFinal +
                '}';
    }
}
