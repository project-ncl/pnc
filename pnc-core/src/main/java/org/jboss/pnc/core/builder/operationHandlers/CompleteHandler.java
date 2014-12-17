package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.builder.DatastoreAdapter;
import org.jboss.pnc.model.TaskStatus;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class CompleteHandler extends OperationHandlerBase implements OperationHandler {

    BuildDriverFactory buildDriverFactory;
    DatastoreAdapter datastoreAdapter;

    private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(CompleteHandler.class);


    @Inject
    public CompleteHandler(BuildDriverFactory buildDriverFactory, DatastoreAdapter datastoreAdapter) {
        this.buildDriverFactory = buildDriverFactory;
        this.datastoreAdapter = datastoreAdapter;
    }

    @Override
    protected TaskStatus.Operation executeAfter() {
        return TaskStatus.Operation.COLLECT_RESULTS;
    }

    @Override
    protected void doHandle(BuildTask buildTask) {
        buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.COMPLETING_BUILD, TaskStatus.State.STARTED));
        try {

            //TODO clean up env

            datastoreAdapter.storeResult(buildTask.getBuildJobDetails(), buildTask.getBuildJobConfiguration());

            buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.COMPLETING_BUILD, TaskStatus.State.COMPLETED));
            buildTask.onComplete();
        } catch (Exception e) {
            log.error("Error while storing to datastore.", e);
            buildTask.onError(e);
        }
    }

}
