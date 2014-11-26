package org.jboss.pnc.core.builder;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
class TaskSet<T> {

    private Logger log = Logger.getLogger(TaskSet.class.getName());

    private Set<Task<T>> tasks = new HashSet<Task<T>>();

    private Task<T> wrap(T task) {
        Task wrappedTask = getWrappedTask(task);
        if (wrappedTask == null) {
            wrappedTask = new Task(task);
            tasks.add(wrappedTask);
        }
        return wrappedTask;
    }

    private Task getWrappedTask(T task) {
        for (Task<T> wrappedTask : tasks) {
            if (wrappedTask.getTask().equals(task)) {
                return wrappedTask;
            }
        }
        return null;
    }

    void add(T task, Set<T> dependencies) {
        Set<Task<T>> wrappedDependencies = new HashSet<>();
        for (T dependency : dependencies) {
            wrappedDependencies.add(wrap(dependency));
        }
        Task wrappedTask = wrap(task);
        wrappedTask.setDependencies(wrappedDependencies);
    }

    /**
     * Method blocks if there is any running task and no new tasks available to run.
     *
     * @return Task with resolved dependencies or null if there are no tasks left
     */
    Task<T> getNext() throws InterruptedException {
        for (Task<T> task : tasks) {
            if (task.isNew() && task.hasResolvedDependencies()) {
                return task;
            }
        }
        if (isAnyTaskStillBuilding()) {
            synchronized (this) {
                try {
                    this.wait(); //TODO max timeout
                } catch (InterruptedException e1){}
            }
            return getNext();
        }
        return null;
    }

    private boolean isAnyTaskStillBuilding() {
        for (Task<T> task : tasks) {
            if (task.isBuilding()) {
                log.finest("Task is still running " + task.getTask());
                return true;
            }
        }
        return false;
    }

    public boolean isAnyNewTask() {
        for (Task<T> task : tasks) {
            if (task.isNew()) {
                return true;
            }
        }
        return false;
    }
}
