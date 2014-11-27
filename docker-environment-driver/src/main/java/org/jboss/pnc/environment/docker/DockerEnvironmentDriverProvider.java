package org.jboss.pnc.environment.docker;

import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;

import java.util.ArrayList;
import java.util.List;

public class DockerEnvironmentDriverProvider implements EnvironmentDriverProvider {

    List<EnvironmentDriver> availableDrivers = new ArrayList<>();

    public DockerEnvironmentDriverProvider(List<EnvironmentDriver> availableDrivers) {
        this.availableDrivers = availableDrivers;
    }

    @Override
    public EnvironmentDriver getDriver(OperationalSystem operationalSystem) {
        return new DockerEnvironmentDriver();
    }

    public List<EnvironmentDriver> getAvailableDrivers() {
        return availableDrivers;
    }
}
