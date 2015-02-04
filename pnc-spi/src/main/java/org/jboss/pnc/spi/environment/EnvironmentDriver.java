package org.jboss.pnc.spi.environment;

import org.jboss.pnc.model.Environment;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public interface EnvironmentDriver {

    RunningEnvironment buildEnvironment(Environment environment);
    
    boolean canBuildEnviroment(Environment environment);

    void destroyEnvironment(RunningEnvironment runningEnvironment);
}
