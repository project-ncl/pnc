package org.jboss.pnc.core.test.mock;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public class BuildDriverMock implements BuildDriver {

    Logger log = Logger.getLogger(BuildDriverMock.class);

    @Override
    public String getDriverId() {
        return null;
    }


    @Override
    public RunningBuild startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration, RepositoryConfiguration repositoryConfiguration) throws BuildDriverException {
        try {
            log.debug("Building " + projectBuildConfiguration);
            Thread.sleep(500);
            return new RunningBuild() {
                @Override
                public void monitor(Consumer<CompletedBuild> onComplete, Consumer<Exception> onError) {
                    onComplete.accept(new CompletedBuild() {
                        @Override
                        public BuildDriverStatus getCompleteStatus() {
                            return BuildDriverStatus.SUCCESS;
                        }

                        @Override
                        public BuildResult getBuildResult() throws BuildDriverException {
                            return getBuildResultMock();
                        }
                    });
                }
            };
        } catch (InterruptedException e) {
            log.error(e);
            return null;
        }
    }

    private BuildResult getBuildResultMock() {
        return new BuildResult() {
            @Override
            public String getBuildLog() throws BuildDriverException {
                return "Building in workspace ... Finished: SUCCESS";
            }

            @Override
            public BuildDriverStatus getBuildDriverStatus() throws BuildDriverException {
                return BuildDriverStatus.SUCCESS;
            }
        };
    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return true;
    }

}
