package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.datastore.Datastore;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class CompleteHandler extends OperationHandlerBase implements OperationHandler {

    BuildDriverFactory buildDriverFactory;
    Datastore datastore;

    @Inject
    public CompleteHandler(BuildDriverFactory buildDriverFactory, Datastore datastore) {
        this.buildDriverFactory = buildDriverFactory;
        this.datastore = datastore;
    }

    @Override
    protected TaskStatus.Operation executeAfter() {
        return TaskStatus.Operation.COLLECT_RESULTS;
    }

    @Override
    public void handle(BuildTask task) {
        if (task.getException() != null) {
            doHandle(task);
        } else {
            super.handle(task);
        }
    }

    @Override
    protected void doHandle(BuildTask buildTask) {
        buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.COMPLETING_BUILD, TaskStatus.State.STARTED));
        try {

            //TODO clean up env
            //TODO store results to db

//            BuildDetails buildDetails = buildTask.getBuildDetails();
//            ProjectBuildResult buildResult = new ProjectBuildResult();
//            buildResult.setBuildLog(buildDetails.getBuildLog());
//            buildResult.setStatus(buildResult.getStatus());
//            buildResult.setBuildCollections();
//            buildResult.setBuildCollections();
//            datastore.storeCompletedBuild(buildResult);

            buildTask.onComplete();
        } catch (Exception e) {
            buildTask.onError(e);
        }
    }
}
