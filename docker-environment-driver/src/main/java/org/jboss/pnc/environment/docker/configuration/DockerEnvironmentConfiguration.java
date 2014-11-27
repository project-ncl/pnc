package org.jboss.pnc.environment.docker.configuration;

import org.jboss.pnc.environment.docker.DockerEnvironmentDriver;
import org.jboss.pnc.environment.docker.DockerEnvironmentDriverProvider;
import org.jboss.pnc.spi.environment.EnvironmentDriver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import java.util.ArrayList;
import java.util.List;

/**
 * Semi automatic dependency injection example. Scales a lot better then fully automatic.
 */
@ApplicationScoped
public class DockerEnvironmentConfiguration {

    @Produces
    public DockerEnvironmentDriver dockerEnvironmentDriver() {
        return new DockerEnvironmentDriver();
    }

    @Produces
    public DockerEnvironmentDriverProvider dockerEnvironmentDriverProvider(Instance<EnvironmentDriver> drivers) {
        List<EnvironmentDriver> allDrivers = new ArrayList<>();
        drivers.forEach((driver) -> allDrivers.add(driver));
        return new DockerEnvironmentDriverProvider(allDrivers);
    }
}
