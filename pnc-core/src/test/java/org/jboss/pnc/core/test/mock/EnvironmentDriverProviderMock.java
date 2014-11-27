package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EnvironmentDriverProviderMock implements EnvironmentDriverProvider {

    @Override
    public EnvironmentDriver getDriver(OperationalSystem operationalSystem) {
        return new EnvironmentDriverMock();
    }
}
