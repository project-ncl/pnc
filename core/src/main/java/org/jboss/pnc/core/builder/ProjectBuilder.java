package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.spi.builddriver.BuildDriver;
import org.jboss.pnc.core.spi.repositorymanager.Repository;
import org.jboss.pnc.core.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.datastore.Datastore;
import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Project;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

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

    public void buildProjects(Set<Project> projects) throws CoreException, InterruptedException {

        final TaskSet taskSet = new TaskSet(projects);

        Semaphore maxConcurrentTasks = new Semaphore(3); //TODO configurable

        while (true) {
            final Task<Project> task = taskSet.getNext();
            maxConcurrentTasks.acquire();
            if (task == null) break;

            Consumer<BuildResult> notifyTaskComplete = buildResult -> {
                if (buildResult.getStatus().equals(BuildStatus.SUCCESS)) {
                    task.completedSuccessfully();
                } else {
                    task.completedWithError();
                }
                maxConcurrentTasks.release();
                taskSet.notify();
            };

            task.setBuilding();
            buildProject(task.getTask(), notifyTaskComplete);
        }
    }

    private void buildProject(Project project, Consumer<BuildResult> notifyTaskComplete) throws CoreException {
        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(project.getBuildType());
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(project.getBuildType());

        Repository deployRepository = repositoryManager.createEmptyRepository();
        Repository repositoryProxy = repositoryManager.createProxyRepository();

        buildDriver.setDeployRepository(deployRepository);
        buildDriver.setSourceRepository(repositoryProxy);

        //TODO who should decide which image to use
        //buildDriver.setImage

        buildDriver.buildProject(project, onBuildComplete(deployRepository, repositoryProxy));

    }

    Consumer<BuildResult> onBuildComplete(Repository deployRepository, Repository repositoryProxy) {
        return buildResult -> {
            storeResult(buildResult);
            //TODO if scratch etc
            deployRepository.persist();
            repositoryProxy.persist();
        };
    }

    private void storeResult(BuildResult buildResult) {
        datastore.storeCompletedBuild(buildResult);
    }

}
