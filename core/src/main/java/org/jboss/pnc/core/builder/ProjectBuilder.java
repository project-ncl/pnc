package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.spi.builddriver.BuildDriver;
import org.jboss.pnc.core.spi.repositorymanager.Repository;
import org.jboss.pnc.core.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.datastore.Datastore;
import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.Project;

import javax.inject.Inject;
import java.util.Set;
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

        TaskSet taskSet = new TaskSet(projects);

        while (true) {
            final Task<Project> task = taskSet.getNext();
            if (task == null) break;

            task.setBuilding();
            buildProject(task.getTask(), onBuildComplete(task));
        }
    }

    private void buildProject(Project project, Consumer<BuildResult> onBuildComplete) throws CoreException {
        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(project.getBuildType());
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(project.getBuildType());

        Repository deployRepository = repositoryManager.createEmptyRepository();
        Repository repositoryProxy = repositoryManager.createProxyRepository();

        buildDriver.setDeployRepository(deployRepository);
        buildDriver.setSourceRepository(repositoryProxy);

        //TODO who should decide which image to use
        //buildDriver.setImage

        buildDriver.buildProject(project, onBuildComplete);

    }

    Consumer<BuildResult> onBuildComplete(Task<Project> task) {
        return buildResult -> {
            storeResult(buildResult);
            
            task.buildComplete(buildResult);
        };
    }

    private void storeResult(BuildResult buildResult) {
        datastore.storeCompletedBuild(buildResult);
    }

}
