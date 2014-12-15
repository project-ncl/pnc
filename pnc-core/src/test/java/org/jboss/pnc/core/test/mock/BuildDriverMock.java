package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;
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
                                  Consumer<BuildJobDetails> onComplete, Consumer<Exception> onError) {
        Runnable projectBuild = () -> {
            try {
                log.fine("Building " + projectBuildConfiguration);
                Thread.sleep(500);
                onComplete.accept(new BuildJobDetails(projectBuildConfiguration.getIdentifier(), 1));
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
    public void waitBuildToComplete(BuildJobDetails buildJobDetails, Consumer<String> onComplete, Consumer<Exception> onError) {
        Runnable projectBuild = () -> {
            try {
                log.fine("Waiting " + buildJobDetails.getJobName());
                Thread.sleep(500);
                onComplete.accept("");
            } catch (InterruptedException e) {
                onError.accept(e);
            }
        };
        new Thread(projectBuild).start();

    }

    @Override
    public void retrieveBuildResults(BuildJobDetails buildJobDetails, Consumer<BuildDriverResult> onComplete, Consumer<Exception> onError) {
        Runnable projectBuild = () -> {
            try {
                Thread.sleep(500);
                BuildDriverResult buildDriverResult = new BuildDriverResult();
                buildDriverResult.setBuildStatus(BuildStatus.SUCCESS);
                buildDriverResult.setConsoleOutput("Building in workspace ... Finished: SUCCESS");
                onComplete.accept(buildDriverResult);
            } catch (InterruptedException e) {
                onError.accept(e);
            }
        };
        new Thread(projectBuild).start();

    }


}
