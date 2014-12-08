package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.*;
import org.jboss.pnc.model.exchange.BuildTaskConfiguration;
import org.jboss.pnc.model.exchange.Task;
import org.jboss.pnc.core.task.TaskQueue;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Function;
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
    TaskQueue taskQueue;

    @Inject
    private Logger log;

    public boolean buildProject(ProjectBuildConfiguration projectBuildConfiguration)
            throws CoreException, BuildDriverException {

        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(projectBuildConfiguration.getEnvironment().getBuildType());
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);

        BuildTaskConfiguration buildTaskConfiguration = new BuildTaskConfiguration(projectBuildConfiguration, repositoryConfiguration);
        Task<BuildTaskConfiguration> buildTask = new Task(buildTaskConfiguration);

        RepositoryConfiguration repositoryConfiguration = repositoryManager.createRepository(projectBuildConfiguration, buildCollection);

        boolean buildQueued = buildDriver.startProjectBuild(buildTask);

        Consumer<TaskStatus> onStatusUpdate = ;

        buildProject2(null, (task) -> {
                                    task.getOperation().
                                });
    }

    public void buildProject2(ProjectBuildConfiguration projectBuildConfiguration, Consumer<TaskStatus> onStatusUpdate, Consumer<Exception> onError) {
        try {
            RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(projectBuildConfiguration.getEnvironment().getBuildType());

            onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_REPOSITORY, 0));
            RepositoryConfiguration repositoryConfiguration = repositoryManager.createRepository(projectBuildConfiguration, null);
            onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_REPOSITORY, 100));

            onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_JENKINS_JOB, 0));
            JobConfig jobConfig = buildDriver.configureJob(projectBuildConfiguration, repositoryConfiguration);
            onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_JENKINS_JOB, 100));

            onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.RUN_JENKINS_JOB, 0));
            ProjectBuildResult buildResult = buildDriver.runJob(jobConfig);
            onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.RUN_JENKINS_JOB, 100));

            onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.STORE_BUILD_RESULTS, 0));
            datastore.storeCompletedBuild(buildResult);
            onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.STORE_BUILD_RESULTS, 100));


        } catch (Exception e) {
            onError.accept(e);
        }
    }

    public void buildProject(ProjectBuildConfiguration buildConfiguration, Consumer<TaskStatus> onStatusUpdate, Consumer<Exception> onError) {
        try {
            RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);


            Function<ProjectBuildConfiguration, RepositoryConfiguration> createRepositoryConfiguration = (projectBuildConfiguration) -> {
                onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_REPOSITORY, 0));
                RepositoryConfiguration repositoryConfiguration = repositoryManager.createRepository(projectBuildConfiguration, null);
                onStatusUpdate.accept(new TaskStatus(TaskStatus.Operation.CREATE_REPOSITORY, 100));
                return repositoryConfiguration;
            };

            Function<RepositoryConfiguration, String> createJenkinsJob = (stringParam) -> {
                return stringParam + "!";
            };

            Function<String, String> runJenkinsJob = (stringParam) -> {
                return stringParam + "!";
            };

            createRepositoryConfiguration.andThen(createJenkinsJob);
            createJenkinsJob.andThen(runJenkinsJob);

            Task<ProjectBuildConfiguration> buildTask = new Task<>(runJenkinsJob, onStatusUpdate, onError);

//            Task<ProjectBuildConfiguration> buildTask = new Task<>(buildConfiguration, onStatusUpdate, onError);
//            buildTask.andThen(createRepositoryConfiguration);
//            buildTask.andThen(createJenkinsJob);
//            buildTask.andThen(runJenkisnJob);
//            buildTask.andThen(storeJobResults);
//            buildTask.andThen(storeJobMetaData);
            taskQueue.addTask(buildTask);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

}
