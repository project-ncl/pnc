package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.task.Task;
import org.jboss.pnc.core.task.TaskQueue;
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
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.inject.Inject;

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
    TaskQueue taskQueue;

    @Inject
    private Logger log;

    public boolean buildProject(ProjectBuildConfiguration projectBuildConfiguration, BuildCollection buildCollection)
            throws CoreException, BuildDriverException, RepositoryManagerException {

        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(projectBuildConfiguration.getEnvironment().getBuildType());
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);

        RepositoryConfiguration repositoryConfiguration = repositoryManager.createRepository(projectBuildConfiguration, buildCollection);

        Task buildTask = new Task(projectBuildConfiguration);
        Consumer<TaskStatus> updateStatus = (ts) -> {
            
        };
        boolean buildPassed = buildDriver.startProjectBuild(projectBuildConfiguration, repositoryConfiguration, updateStatus);
        taskQueue.addTask(buildTask);

    }

}
