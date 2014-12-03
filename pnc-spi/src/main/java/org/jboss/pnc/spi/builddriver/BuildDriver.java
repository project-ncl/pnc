package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface BuildDriver {

    String getDriverId();

    /**
     * Method returns as soon as build was triggered.
     *
     * @param projectBuildConfiguration
     * @param repositoryConfiguration
     * @param onUpdate call to update the task status
     * @return return false if driver is not ready for accepting new requests
     */
    boolean startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration, RepositoryConfiguration repositoryConfiguration, Consumer<TaskStatus> onUpdate)
            throws BuildDriverException;

    boolean canBuild(BuildType buildType);

}
