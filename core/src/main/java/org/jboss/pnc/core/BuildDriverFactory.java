package org.jboss.pnc.core;

import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.spi.builddriver.BuildDriver;
import org.jboss.pnc.model.BuildType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class BuildDriverFactory {

    @Inject
    Instance<BuildDriver> availableDrivers;

    public BuildDriver getBuildDriver(BuildType buildType) throws CoreException {

        for (BuildDriver driver : availableDrivers) {
            if (driver.canBuild(buildType)) {
                return driver;
            }
        }

        throw new CoreException("No build driver available for " + buildType + " build type.");
    }

}
