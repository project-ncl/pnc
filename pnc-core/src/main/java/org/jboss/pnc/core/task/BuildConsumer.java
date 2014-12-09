package org.jboss.pnc.core.task;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.BuildQueue;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
public class BuildConsumer implements Runnable {

    @Inject
    BuildDriverFactory buildDriverFactory;

    @Inject
    RepositoryManagerFactory repositoryManagerFactory;

    @Inject
    Datastore datastore;

    @Inject
    EnvironmentDriverProvider environmentDriverProvider;

    @Inject
    BuildQueue buildQueue;

    public BuildConsumer() throws CoreException {
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(projectBuildConfiguration.getEnvironment().getBuildType());

//        onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_REPOSITORY, 0));
//        RepositoryConfiguration repositoryConfiguration = repositoryManager.createRepository(projectBuildConfiguration, null);
//        onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_REPOSITORY, 100));

        onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_JENKINS_JOB, 0));
        JobConfig jobConfig = buildDriver.configureJob(projectBuildConfiguration, repositoryConfiguration);
        onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_JENKINS_JOB, 100));

        onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.RUN_JENKINS_JOB, 0));
        ProjectBuildResult buildResult = buildDriver.runJob(jobConfig);
        onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.RUN_JENKINS_JOB, 100));

        onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.STORE_BUILD_RESULTS, 0));
        datastore.storeCompletedBuild(buildResult);
        onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.STORE_BUILD_RESULTS, 100));

    }


    @Override
    public void run() {
        while (true) {
            try {
                BuildTask buildTask = buildQueue.take();
                runBuildTask(buildTask);
            } catch (InterruptedException e) {
                break;
            }

        }
    }

    private void runBuildTask(BuildTask buildTask) {
        if (buildTask.) {

        }
    }
}
