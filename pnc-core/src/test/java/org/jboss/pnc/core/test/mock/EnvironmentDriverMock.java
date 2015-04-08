package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import javax.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class EnvironmentDriverMock implements EnvironmentDriver {

    @Override
    public StartedEnvironment buildEnvironment(Environment buildEnvironment,
            final RepositorySession repositoryConfiguration) throws EnvironmentDriverException {
        return new StartedEnvironment() {

            @Override
            public void destroyEnvironment() throws EnvironmentDriverException {

            }

            @Override
            public void monitorInitialization(Consumer<RunningEnvironment> onComplete,
                    Consumer<Exception> onError) {
                onComplete.accept(
                        new RunningEnvironment() {

                            @Override
                            public void transferDataToEnvironment(String pathOnHost, String data)
                                    throws EnvironmentDriverException {
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
                        });
            }
        };

    }

    @Override
    public boolean canBuildEnvironment(Environment environment) {
        return true;
    }

}
