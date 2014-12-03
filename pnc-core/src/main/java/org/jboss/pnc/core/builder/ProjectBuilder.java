package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class ProjectBuilder {

    @Inject
    BuildDriverFactory buildDriverFactory;

    @Inject
    RepositoryManagerFactory repositoryManagerFactory;

    @Inject
    Datastore datastore;

    @Inject
    EnvironmentDriverProvider environmentDriverProvider;

    @Inject
    private Logger log;

    public boolean buildProject(ProjectBuildConfiguration projectBuildConfiguration, BuildCollection buildCollection)
            throws CoreException, BuildDriverException {

        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(projectBuildConfiguration.getEnvironment().getBuildType());
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);

        RepositoryConfiguration repository = repositoryManager.createRepository( projectBuildConfiguration, buildCollection );

        buildDriver.setRepository( repository );


        Consumer<TaskStatus> updateStatus = (ts) -> {
            //TODO update build task status to building
        };
        return buildDriver.startProjectBuild(projectBuildConfiguration, updateStatus);

    }

}
