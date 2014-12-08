package org.jboss.pnc.model.exchange;

import org.jboss.pnc.model.TaskStatus;

import java.util.function.Consumer;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
*/
public class Task<T> {
    private Consumer<TaskStatus> onStatusUpdate;
    private Consumer<Exception> onError;
    TaskStatus status;
    private T taskConfiguration;

    public Task(T taskConfiguration) {
        this.taskConfiguration = taskConfiguration;
    }

    public Task(T taskConfiguration, Consumer<TaskStatus> onStatusUpdate, Consumer<Exception> onError) {
        this.onStatusUpdate = onStatusUpdate;
        this.onError = onError;
        this.taskConfiguration = taskConfiguration;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public T getTaskConfiguration() {
        return taskConfiguration;
    }

}
