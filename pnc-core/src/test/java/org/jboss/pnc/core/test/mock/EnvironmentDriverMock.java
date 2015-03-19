package org.jboss.pnc.core.test.mock;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class EnvironmentDriverMock implements EnvironmentDriver {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    @Override
    public RunningEnvironment buildEnvironment(Environment buildEnvironment,
            final RepositorySession repositoryConfiguration) throws EnvironmentDriverException {
        return new RunningEnvironment() {
            
            @Override
            public void transferDataToEnvironment(String pathOnHost, String data) throws EnvironmentDriverException {
            }
            
            @Override
            public void transferDataToEnvironment(String pathOnHost, InputStream stream)
                    throws EnvironmentDriverException {
            }
            
            @Override
            public RepositorySession getRepositorySession() {
                return repositoryConfiguration;
            }
            
            @Override
            public String getJenkinsUrl() {
                return "http://10.10.10.10:8080";
            }
            
            @Override
            public int getJenkinsPort() {
                return 0;
            }
            
            @Override
            public String getId() {
                return null;
            }
            
            @Override
            public void destroyEnvironment() throws EnvironmentDriverException {
            }
        };
    }

    @Override
    public boolean canBuildEnvironment(Environment environment) {
        return true;
    }


}
