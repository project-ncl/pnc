package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildQueue;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.model.builder.BuildDetails;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class RetrieveBuildResultsHandler extends OperationHandlerBase implements OperationHandler {

    private final BuildDriverFactory buildDriverFactory;

    @Inject
    public RetrieveBuildResultsHandler(BuildQueue buildQueue, BuildDriverFactory buildDriverFactory) {
        super(buildQueue);
        this.buildDriverFactory = buildDriverFactory;
    }

    @Override
    protected TaskStatus.Operation executeAfter() {
        return TaskStatus.Operation.BUILD_COMPLETED;
    }

    @Override
    protected void doHandle(BuildTask buildTask) {
        buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.COLLECT_RESULTS, 0));
        try {
            Consumer<BuildDriverResult> onBuildResultComplete = (buildDriverResult) -> {
                buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.COLLECT_RESULTS, 100));
                BuildDetails buildDetails = buildTask.getBuildDetails();
                buildDetails.setBuildStatus(buildDriverResult.getBuildStatus());
                buildDetails.setBuildLog(buildDriverResult.getConsoleOutput());
                buildQueue.add(buildTask);
            };

            Consumer<Exception> onBuildResultError = (e) -> {
                buildTask.onError(e);
            };

            //TODO better validation
            assert (buildTask.getBuildDetails() != null);

            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildTask.getProjectBuildConfiguration().getEnvironment().getBuildType());
            buildDriver.retrieveBuildResults(buildTask.getBuildDetails(), onBuildResultComplete, onBuildResultError);

        } catch (CoreException e) {
            buildTask.onError(e);
        }
    }
}
