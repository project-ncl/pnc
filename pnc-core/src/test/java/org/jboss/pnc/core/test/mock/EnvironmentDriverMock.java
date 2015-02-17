package org.jboss.pnc.core.test.mock;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;

@ApplicationScoped
public class EnvironmentDriverMock implements EnvironmentDriver {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());


    @Override
    public RunningEnvironment buildEnvironment(Environment buildEnvironment, String dependencyUrl,
            String deployUrl) throws EnvironmentDriverException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void destroyEnvironment(RunningEnvironment env) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean canBuildEnvironment(Environment environment) {
        // TODO Auto-generated method stub
        return false;
    }

}
