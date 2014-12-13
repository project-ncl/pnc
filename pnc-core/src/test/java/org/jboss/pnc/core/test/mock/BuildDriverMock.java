package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.BuildDetails;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public class BuildDriverMock implements BuildDriver {

    @Inject
    Logger log;

    @Override
    public String getDriverId() {
        return null;
    }

    @Override
    public void startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration,
                                  RepositoryConfiguration repositoryConfiguration,
                                  Consumer<BuildDetails> onComplete, Consumer<Exception> onError) {
        Runnable projectBuild = () -> {
            try {
                log.fine("Building " + projectBuildConfiguration);
                Thread.sleep(500);
                onComplete.accept(new BuildDetails(projectBuildConfiguration.getIdentifier(), 1));
            } catch (InterruptedException e) {
                onError.accept(e);
            }
        };
        new Thread(projectBuild).start();
    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return true;
    }

    @Override
    public void waitBuildToComplete(BuildDetails buildDetails, Consumer<String> onComplete, Consumer<Exception> onError) {
        Runnable projectBuild = () -> {
            try {
                log.fine("Waiting " + buildDetails.getJobName());
                Thread.sleep(500);
                onComplete.accept("");
            } catch (InterruptedException e) {
                onError.accept(e);
            }
        };
        new Thread(projectBuild).start();

    }


}
