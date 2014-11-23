package org.jboss.pnc.core.builder;

import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.BuildStatus;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
class Task<T> {
    private T task;

    private BuildResult buildResult;

    private Status status;

    Task(T task) {
        this.task = task;
        status = Status.NEW;
    }

    T getTask() {
        return task;
    }

    void setBuilding() {
        status = Status.BUILDING;
    }

    void buildComplete(BuildResult buildResult) {
        this.buildResult = buildResult;
        status = Status.DONE;
        notify();
    }

    boolean isNew() {
        return Status.NEW.equals(status);
    }

    boolean isBuilding() {
        return Status.BUILDING.equals(status);
    }

    boolean isDone() {
        return Status.DONE.equals(status);
    }

    boolean hasResolvedDependencies(Set<Task<T>> tasks) {
        Predicate<Task> filterSuccess = t -> t.isDone() && t.buildResult.getStatus().equals(BuildStatus.SUCCESS);
        long successfullyBuildDependencies = tasks.stream().filter(filterSuccess).count();
        return successfullyBuildDependencies == tasks.size();
    }

    enum Status {
        NEW, BUILDING, DONE;
    }
}
