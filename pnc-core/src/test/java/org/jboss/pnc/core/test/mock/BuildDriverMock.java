package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.repositorymanager.Repository;
import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Project;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public class BuildDriverMock implements BuildDriver {
    private Consumer<BuildResult> onBuildComplete;

    @Override
    public String getDriverId() {
        return null;
    }

    @Override
    public void buildProject(Project project, Consumer<BuildResult> onBuildComplete) {
        this.onBuildComplete = onBuildComplete;
        new Thread(new FakeBuilder()).start();
        return;
    }

    @Override
    public void setDeployRepository(Repository deployRepository) {

    }

    @Override
    public void setSourceRepository(Repository repositoryProxy) {

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
            BuildResult buildResult = new BuildResult();
            buildResult.setStatus(BuildStatus.SUCCESS);
            onBuildComplete.accept(buildResult);
        }
    }
}
