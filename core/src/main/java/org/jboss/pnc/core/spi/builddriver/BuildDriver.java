package org.jboss.pnc.core.spi.builddriver;

import org.jboss.pnc.core.spi.repositorymanager.Repository;
import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Project;

import java.util.function.Consumer;

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
    //Future<BuildResult> buildProject(Project project);
    void buildProject(Project project, Consumer<BuildResult> onBuildComplete);

    void setDeployRepository(Repository deployRepository);

    void setSourceRepository(Repository repositoryProxy);

    BuildType getBuildType();

}
