package org.jboss.pnc.core.events;

import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

public class DefaultBuildStatusChangedEvent implements BuildStatusChangedEvent {

    private final BuildStatus oldStatus;
    private final BuildStatus newStatus;
    private final int buildConfigurationId;

    public DefaultBuildStatusChangedEvent(BuildStatus oldStatus, BuildStatus newStatus, int buildConfigurationId) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.buildConfigurationId = buildConfigurationId;
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

    @Override public String toString() {
        return "DefaultBuildStatusChangedEvent{" +
                "oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", buildConfigurationId=" + buildConfigurationId +
                '}';
    }
}
