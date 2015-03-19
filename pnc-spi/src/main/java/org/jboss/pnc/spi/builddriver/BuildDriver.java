package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface BuildDriver {

    String getDriverId();

    boolean canBuild(BuildType buildType);

    public RunningBuild startProjectBuild(BuildConfiguration buildConfiguration, RunningEnvironment runningEnvironment) throws BuildDriverException;

}
