package org.jboss.pnc.model;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class TaskStatus {
    State state;
    Operation operation;

    public TaskStatus(Operation operation, State state) {
        this.operation = operation;
        this.state = state;
    }

    public enum State {
        STARTED,
        COMPLETED;
    }

    public enum Operation { //TODO clean up
        NEW,
        BUILD_SCHEDULED,
        WAITING_BUILD_TO_COMPLETE,
        CREATE_REPOSITORY,
        COMPLETING_BUILD,
        COLLECT_RESULTS;
    }

    public Operation getOperation() {
        return operation;
    }

    public boolean isCompleted() {
        return State.COMPLETED.equals(state);
    }

    public boolean isOperationCompleted(Operation operation) {
        return operation.equals(this.operation) && isCompleted();
    }
}
