package org.jboss.pnc.spi.environment;

import org.jboss.pnc.model.Environment;

public interface EnvironmentDriver {
    void buildEnvironment(Environment environment);
}
