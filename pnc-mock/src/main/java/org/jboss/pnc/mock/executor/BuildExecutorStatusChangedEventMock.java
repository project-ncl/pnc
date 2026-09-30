/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.executor;

import java.util.Optional;

import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;

class BuildExecutorStatusChangedEventMock implements BuildExecutionStatusChangedEvent {

    private final BuildExecutionStatus oldStatus;
    private final BuildExecutionStatus newStatus;
    private final String buildTaskId;
    private final Integer buildConfigurationId;
    private final Optional<BuildResult> buildResult;

    private boolean isFinal;

    public BuildExecutorStatusChangedEventMock(
            BuildExecutionStatus oldStatus,
            BuildExecutionStatus newStatus,
            String buildTaskId,
            Integer buildConfigurationId,
            Optional<BuildResult> buildResult,
            boolean isFinal) {

        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.buildTaskId = buildTaskId;
        this.buildConfigurationId = buildConfigurationId;
        this.buildResult = buildResult;
    }

    @Override
    public String getBuildTaskId() {
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
    public boolean isFinal() {
        return isFinal;
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
    public String toString() {
        return "DefaultBuildExecutionStatusChangedEvent{" + "oldStatus=" + oldStatus + ", newStatus=" + newStatus
                + ", buildTaskId=" + buildTaskId + ", buildConfigurationId=" + buildConfigurationId + ", buildResult="
                + buildResult + '}';
    }
}
