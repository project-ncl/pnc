package org.jboss.pnc.environment.docker;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

public class DockerEnvironmentDriver implements EnvironmentDriver {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    @Override
    public void buildEnvironment(Environment buildEnvironment) {

    }
}
