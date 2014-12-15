package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.builder.DatastoreAdapter;
import org.jboss.pnc.model.TaskStatus;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class ErrorStateHandler extends OperationHandlerBase implements OperationHandler {

    DatastoreAdapter datastoreAdapter;

    public ErrorStateHandler(DatastoreAdapter datastoreAdapter) {
        this.datastoreAdapter = datastoreAdapter;
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

            datastoreAdapter.storeResult(buildTask.getBuildJobDetails(), buildTask.getBuildJobConfiguration());


            buildTask.onComplete();
        } catch (Exception e) {
            //TODO log errors
        }
    }
}
