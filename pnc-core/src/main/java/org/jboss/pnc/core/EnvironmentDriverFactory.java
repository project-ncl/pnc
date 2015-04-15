package org.jboss.pnc.core;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;

/**
 * Creates instances of environment drivers
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@ApplicationScoped
public class EnvironmentDriverFactory {

    @Inject
    Instance<EnvironmentDriver> availableDrivers;
    
    /**
     * Gets environment driver, which can manage requested environment
     * @param environment Requested environment specification
     * @return Available driver for given environment
     * @throws CoreException Throw if no suitable driver for selected environment was found
     */
    public EnvironmentDriver getDriver(Environment environment) throws CoreException {
        for (EnvironmentDriver driver : availableDrivers) {
            if (driver.canBuildEnvironment(environment))
                return driver;
        }

        throw new CoreException("No environment driver available for " + environment + " environment type.");
    }
}
