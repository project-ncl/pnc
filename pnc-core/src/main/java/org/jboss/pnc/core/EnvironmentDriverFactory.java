package org.jboss.pnc.core;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class EnvironmentDriverFactory {

    @Inject
    Instance<EnvironmentDriver> availableDrivers;
    
    public EnvironmentDriver getDriver(Environment environment) throws CoreException {
        for (EnvironmentDriver driver : availableDrivers) {
            if (driver.canBuildEnviroment(environment))
                return driver;
        }

        throw new CoreException("No environment driver available for " + environment + " environment type.");
    }
}
