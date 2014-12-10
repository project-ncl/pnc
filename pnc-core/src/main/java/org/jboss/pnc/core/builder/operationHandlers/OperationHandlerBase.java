package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.builder.BuildQueue;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.model.TaskStatus;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public abstract class OperationHandlerBase {
    protected OperationHandler next;
    protected BuildQueue buildQueue;

    public OperationHandlerBase(BuildQueue buildQueue) {
        this.buildQueue = buildQueue;
    }

    public void handle(BuildTask task) {
        if (task.getStatus().isOperationCompleted(executeAfter())) {
            doHandle(task);
        } else {
            if (next != null) {
                next.handle(task);
            }
        }
    }

    public void next(OperationHandler handler) {
        next = handler;
    }

    protected abstract void doHandle(BuildTask buildTask);

    /**
     *
     * @return Operation that needs to be completted in order to call doHandle method.
     */
    protected abstract TaskStatus.Operation executeAfter();

}
