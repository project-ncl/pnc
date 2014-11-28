package org.jboss.pnc.spi.environment;

import org.jboss.pnc.model.OperationalSystem;

public interface EnvironmentDriverProvider {
    EnvironmentDriver getDriver(OperationalSystem operationalSystem);
}
