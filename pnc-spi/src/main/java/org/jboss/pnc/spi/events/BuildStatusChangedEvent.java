package org.jboss.pnc.spi.events;

import org.jboss.pnc.spi.BuildStatus;

public interface BuildStatusChangedEvent {

    BuildStatus getOldStatus();
    BuildStatus getNewStatus();
    Integer getBuildConfigurationId();

}
