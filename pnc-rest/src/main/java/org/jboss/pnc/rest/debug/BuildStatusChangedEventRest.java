package org.jboss.pnc.rest.debug;

import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BuildStatusChangedEventRest implements BuildStatusChangedEvent {

    private BuildStatus oldStatus;
    private BuildStatus newStatus;
    private int buildConfigurationId;
    private BuildExecution buildExecution;

    public void setOldStatus(BuildStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(BuildStatus newStatus) {
        this.newStatus = newStatus;
    }

    public void setBuildConfigurationId(int buildConfigurationId) {
        this.buildConfigurationId = buildConfigurationId;
    }

    public void setBuildExecution(BuildExecution buildExecution) {
        this.buildExecution = buildExecution;
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
}