package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public class BuildDriverMock implements BuildDriver {

    @Override
    public String getDriverId() {
        return null;
    }

    @Override
    public boolean startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration, Consumer<TaskStatus> onUpdate) {

        Runnable projectBuild = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    onUpdate.accept(new TaskStatus(TaskStatus.Operation.BUILD_SCHEDULED, 0));
                } catch (InterruptedException e) {
                    onUpdate.accept(new TaskStatus(TaskStatus.Operation.BUILD_SCHEDULED, -1));
                }
            }
        };
        //TODO use thread pool, return false if there are no available executors
        new Thread(projectBuild).start();

        onUpdate.accept(new TaskStatus(TaskStatus.Operation.BUILD_SCHEDULED, 0));
        return true;
    }


    @Override
    public void setRepository(RepositoryConfiguration deployRepository) {

    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return true;
    }

}
