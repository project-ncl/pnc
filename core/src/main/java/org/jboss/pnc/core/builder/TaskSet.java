package org.jboss.pnc.core.builder;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
class TaskSet<T> {

    private Set<Task<T>> tasks = new HashSet<Task<T>>();

    TaskSet(Set<T> tasks) {
        tasks.forEach(project -> wrap(project));
        tasks.stream().peek(project -> this.tasks.add(wrap(project)));
    }

    private Task<T> wrap(T project) {
        return new Task(project);
    }

    /**
     * Method blocks until there are no new tasks to build or all are build.
     *
     * @return Task with resolved dependencies or null if there are no tasks left
     */
    Task<T> getNext() throws InterruptedException {
        for (Task<T> task : tasks) {
            if (task.isNew() && task.hasResolvedDependencies(tasks)) {
                return task;
            }
        }
        if (isAnyTaskStillBuilding()) {
            wait(); //TODO max timeout
            return getNext();
        }
        return null;
    }

    private boolean isAnyTaskStillBuilding() {
        for (Task<T> task : tasks) {
            if (task.isBuilding()) {
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
