package org.jboss.pnc.core.events;

import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

public class DefaultBuildStatusChangedEvent implements BuildStatusChangedEvent {

    private final BuildStatus oldStatus;
    private final BuildStatus newStatus;
    private final int buildConfigurationId;
    private final BuildExecution buildExecution;

    public DefaultBuildStatusChangedEvent(BuildStatus oldStatus, BuildStatus newStatus, int buildConfigurationId,
            BuildExecution execution) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.buildConfigurationId = buildConfigurationId;
        this.buildExecution = execution;
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
    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    @Override
    public BuildExecution getBuildExecution() {
        return buildExecution;
    }

    @Override public String toString() {
        return "DefaultBuildStatusChangedEvent{" +
                "oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", buildConfigurationId=" + buildConfigurationId +
                '}';
    }

}
