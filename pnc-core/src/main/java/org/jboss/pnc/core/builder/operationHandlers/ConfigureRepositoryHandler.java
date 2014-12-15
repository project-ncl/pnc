package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.BuildTaskQueue;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
public class ConfigureRepositoryHandler extends OperationHandlerBase implements OperationHandler {

    RepositoryManagerFactory repositoryManagerFactory;

    @Inject
    public ConfigureRepositoryHandler(BuildTaskQueue buildTaskQueue, RepositoryManagerFactory repositoryManagerFactory) {
        this.repositoryManagerFactory = repositoryManagerFactory;
    }

    @Override
    protected TaskStatus.Operation executeAfter() {
        return TaskStatus.Operation.NEW;
    }

    @Override
    protected void doHandle(BuildTask buildTask) {
        buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.CREATE_REPOSITORY, TaskStatus.State.STARTED));
        try {
            Consumer<RepositoryConfiguration> onComplete = (repositoryConfiguration) -> {
                buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.CREATE_REPOSITORY, TaskStatus.State.COMPLETED));
                buildTask.setRepositoryConfiguration(repositoryConfiguration);
            };

            Consumer<Exception> onError = (e) -> {
                buildTask.onError(e);
            };

            RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
            ProjectBuildConfiguration buildConfiguration = buildTask.getBuildJobConfiguration().getProjectBuildConfiguration();
            BuildCollection buildCollection = buildTask.getBuildJobConfiguration().getBuildCollection();

            //TODO better validation
            assert (buildConfiguration != null);
            assert (buildCollection != null);

            repositoryManager.createRepository(buildConfiguration, buildCollection, onComplete, onError);

        } catch (CoreException e) {
            buildTask.onError(e);
        }
    }

}
