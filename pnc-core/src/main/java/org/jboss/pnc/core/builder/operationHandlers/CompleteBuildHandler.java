package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildQueue;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class CompleteBuildHandler implements OperationHandler {
    private OperationHandler next = null;

    @Inject
    BuildQueue buildQueue;

    @Inject
    BuildDriverFactory buildDriverFactory;

    @Override
    public void handle(BuildTask task) {
        if (task.getStatus().isOperationCompleted(TaskStatus.Operation.BUILD_SCHEDULED)) {
            completeBuild(task);
        } else {
            if (next != null) {
                next.handle(task);
            }
        }
    }

    @Override
    public void next(OperationHandler handler) {
        next = handler;
    }

    private void completeBuild(BuildTask buildTask) {
        buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.COMPLETING_BUILD, 0));
        try {
            Consumer<String> onComplete = (jobId) -> {
                buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.COMPLETING_BUILD, 100));
            };

            Consumer<Exception> onError = (e) -> {
                buildTask.onError(e);
            };

            //TODO better validation
            assert (buildTask.getProjectBuildConfiguration() != null);
            assert (buildTask.getRepositoryConfiguration() != null);

            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildTask.getProjectBuildConfiguration().getEnvironment().getBuildType());
            //TODO get build log
            //TODO get stored artifacts
            //TODO clean up env
            onComplete.accept("id");

        } catch (CoreException e) {
            buildTask.onError(e);
        }
    }
}
