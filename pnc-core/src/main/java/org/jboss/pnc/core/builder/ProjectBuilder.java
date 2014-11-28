package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryManagerType;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.pnc.spi.repositorymanager.Repository;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.Semaphore;
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

    public void buildProjects(Set<Project> projects) throws CoreException, InterruptedException {

        final TaskSet<Project> taskSet = new TaskSet();
        for (Project project : projects) {
            taskSet.add(project, project.getDependencies());
        }

        Semaphore maxConcurrentTasks = new Semaphore(3); //TODO configurable

        while (true) {
            final Task<Project> task = taskSet.getNext();
            if (task == null) break;
            log.info("Building task " + task);
            synchronized (taskSet) {
                maxConcurrentTasks.acquire();
            }

            Consumer<BuildResult> notifyTaskComplete = buildResult -> {
                if (buildResult.getStatus().equals(BuildStatus.SUCCESS)) {
                    task.completedSuccessfully();
                } else {
                    task.completedWithError();
                }
                log.finest("Notifying build completed " + task.getTask());
                synchronized (taskSet) {
                    maxConcurrentTasks.release();
                    taskSet.notify();
                }
            };

            task.setBuilding();
            buildProject(task.getTask(), notifyTaskComplete);
        }
    }

    private void buildProject(Project project, Consumer<BuildResult> notifyTaskComplete) throws CoreException {
        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(project.getEnvironment().getBuildType());
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryManagerType.MAVEN); //TODO configure per project

        Repository deployRepository = repositoryManager.createEmptyRepository();
        Repository repositoryProxy = repositoryManager.createProxyRepository();

        buildDriver.setDeployRepository(deployRepository);
        buildDriver.setSourceRepository(repositoryProxy);

        EnvironmentDriver environmentDriver = environmentDriverProvider.getDriver(project.getEnvironment().getOperationalSystem());
        environmentDriver.buildEnvironment(project.getEnvironment());

        buildDriver.startProjectBuild(project, onBuildComplete(notifyTaskComplete, deployRepository, repositoryProxy));

    }

    Consumer<BuildResult> onBuildComplete(Consumer<BuildResult> notifyTaskComplete, Repository deployRepository, Repository repositoryProxy) {
        return buildResult -> {
            storeResult(buildResult);
            //TODO if scratch etc
            deployRepository.persist();
            repositoryProxy.persist();
            notifyTaskComplete.accept(buildResult);
        };
    }

    private void storeResult(BuildResult buildResult) {
        datastore.storeCompletedBuild(buildResult);
    }

}
