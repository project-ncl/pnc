package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.logging.Logger;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.builder.DatastoreAdapter;
import org.jboss.pnc.model.TaskStatus;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class ErrorStateHandler extends OperationHandlerBase implements OperationHandler {

    DatastoreAdapter datastoreAdapter;
    private static final Logger log = Logger.getLogger(ErrorStateHandler.class);

    @Inject
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
            if (next != null) {
                next.handle(task);
            }
        }
    }

    @Override
    protected void doHandle(BuildTask buildTask) {
        log.warn("Handling exception", buildTask.getException());
        try {
            //TODO clean up env

            datastoreAdapter.storeResult(buildTask.getBuildJobDetails(), buildTask.getBuildJobConfiguration());

            buildTask.onComplete();
        } catch (Exception e) {
            log.error("Cannot handle error", e);
            buildTask.onComplete();
        }
    }
}
