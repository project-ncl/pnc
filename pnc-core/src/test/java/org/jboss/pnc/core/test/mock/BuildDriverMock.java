package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public class BuildDriverMock implements BuildDriver {
    private Consumer<ProjectBuildResult> onBuildComplete;

    @Override
    public String getDriverId() {
        return null;
    }

    @Override
    public void startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration,
            Consumer<ProjectBuildResult> onBuildComplete) {
        this.onBuildComplete = onBuildComplete;
        new Thread(new FakeBuilder()).start();
        return;
    }

    @Override
    public void setRepository(RepositoryConfiguration deployRepository) {

    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return true;
    }

    class FakeBuilder implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ProjectBuildResult buildResult = new ProjectBuildResult();
            buildResult.setStatus(BuildStatus.SUCCESS);
            onBuildComplete.accept(buildResult);
        }
    }
}
