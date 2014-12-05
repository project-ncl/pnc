package org.jboss.pnc.model.exchange;

import org.jboss.pnc.model.TaskStatus;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
*/
public class Task<T> {
    TaskStatus status;
    private T taskConfiguration;

    public Task(T taskConfiguration) {
        this.taskConfiguration = taskConfiguration;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public T getTaskConfiguration() {
        return taskConfiguration;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
