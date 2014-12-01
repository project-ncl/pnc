package org.jboss.pnc.spi.builddriver;

import java.util.function.Consumer;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.spi.repositorymanager.Repository;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface BuildDriver {

    String getDriverId();

    /**
     * Method returns as soon as build was triggered.
     *
     * @param project
     * @param onBuildComplete
     */
    // Future<BuildResult> startProjectBuild(Project project);
    void startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration, Consumer<ProjectBuildResult> onBuildComplete);

    /**
     *
     * @param deployRepository
     */
    void setDeployRepository(Repository deployRepository);

    void setSourceRepository(Repository repositoryProxy);

    boolean canBuild(BuildType buildType);

}
