package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.datastore.Datastore;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class ErrorStateHandler extends OperationHandlerBase implements OperationHandler {

    private Datastore datastore;

    public ErrorStateHandler(Datastore datastore) {
        this.datastore = datastore;
    }

    @Override
    protected TaskStatus.Operation executeAfter() {
        return null; //execution handled on its own by overriding handle() method
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
        try {
            //TODO clean up env
            //TODO store results/error to db

//            BuildDetails buildDetails = buildTask.getBuildDetails();
//            ProjectBuildResult buildResult = new ProjectBuildResult();
//            buildResult.setBuildLog(buildDetails.getBuildLog());
//            buildResult.setStatus(buildResult.getStatus());
//            datastore.storeCompletedBuild(buildResult);

            buildTask.onComplete();
        } catch (Exception e) {
            //TODO log errors
        }
    }
}
