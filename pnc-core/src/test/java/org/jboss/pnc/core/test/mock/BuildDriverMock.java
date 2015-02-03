package org.jboss.pnc.core.test.mock;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public class BuildDriverMock implements BuildDriver {

    public static final Logger log = Logger.getLogger(BuildDriverMock.class);

    @Override
    public String getDriverId() {
        return null;
    }


    @Override
    public RunningBuild startProjectBuild(BuildConfiguration buildConfiguration, RepositoryConfiguration repositoryConfiguration) throws BuildDriverException {
        try {
            log.debug("Building " + buildConfiguration);
            Thread.sleep(RandomUtils.randInt(100, 300));
            return new RunningBuild() {

                RepositoryConfiguration repositoryConfiguration = new RepositoryConfigurationMock();

                @Override
                public void monitor(Consumer<CompletedBuild> onComplete, Consumer<Exception> onError) {
                    onComplete.accept(new CompletedBuild() {
                        @Override
                        public BuildDriverStatus getCompleteStatus() {
                            return BuildDriverStatus.SUCCESS;
                        }
                        @Override
                        public BuildDriverResult getBuildResult() throws BuildDriverException {
                            return getBuildResultMock(repositoryConfiguration);
                        }

                        @Override
                        public RepositoryConfiguration getRepositoryConfiguration() {
                            return repositoryConfiguration;
                        }
                    });
                }

                @Override
                public RepositoryConfiguration getRepositoryConfiguration() {
                    return repositoryConfiguration;
                }
            };
        } catch (InterruptedException e) {
            log.error(e);
            return null;
        }
    }

    private BuildDriverResult getBuildResultMock(RepositoryConfiguration repositoryConfiguration) {
        return new BuildDriverResult() {
            @Override
            public String getBuildLog() throws BuildDriverException {
                return "Building in workspace ... Finished: SUCCESS";
            }

            @Override
            public BuildDriverStatus getBuildDriverStatus() throws BuildDriverException {
                return BuildDriverStatus.SUCCESS;
            }

            @Override
            public RepositoryConfiguration getRepositoryConfiguration() {
                return repositoryConfiguration;
            }
        };
    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return true;
    }

}
