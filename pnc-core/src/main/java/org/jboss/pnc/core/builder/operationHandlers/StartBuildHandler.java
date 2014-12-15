package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;
import org.jboss.pnc.spi.builddriver.BuildDriver;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class StartBuildHandler extends OperationHandlerBase implements OperationHandler {

    private final BuildDriverFactory buildDriverFactory;

    @Inject
    public StartBuildHandler(BuildDriverFactory buildDriverFactory) {
        this.buildDriverFactory = buildDriverFactory;
    }

    @Override
    protected TaskStatus.Operation executeAfter() {
        return TaskStatus.Operation.CREATE_REPOSITORY;
    }

    @Override
    protected void doHandle(BuildTask buildTask) {
        buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.BUILD_SCHEDULED, TaskStatus.State.STARTED));
        try {
            Consumer<BuildJobDetails> onComplete = (buildDetails) -> {
                buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.BUILD_SCHEDULED, TaskStatus.State.COMPLETED));
                buildTask.setBuildJobDetails(buildDetails);
            };

            Consumer<Exception> onError = (e) -> {
                buildTask.onError(e);
            };

            ProjectBuildConfiguration projectBuildConfiguration = buildTask.getBuildJobConfiguration().getProjectBuildConfiguration();

            //TODO better validation
            assert (projectBuildConfiguration != null);
            assert (buildTask.getRepositoryConfiguration() != null);

            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(projectBuildConfiguration.getEnvironment().getBuildType());
            buildDriver.startProjectBuild(projectBuildConfiguration, buildTask.getRepositoryConfiguration(), onComplete, onError);

        } catch (CoreException e) {
            buildTask.onError(e);
        }
    }
}
