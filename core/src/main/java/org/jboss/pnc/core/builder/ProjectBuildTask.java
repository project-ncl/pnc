package org.jboss.pnc.core.builder;

import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Project;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
class ProjectBuildTask {
    private Project project;

    private BuildResult buildResult;

    private Status status;

    ProjectBuildTask(Project project) {
        this.project = project;
        status = Status.NEW;
    }

    Project getProject() {
        return project;
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

    boolean hasResolvedDependencies(Set<ProjectBuildTask> projectBuildTasks) {
        Predicate<ProjectBuildTask> filterSuccess = t -> t.isDone() && t.buildResult.getStatus().equals(BuildStatus.SUCCESS);
        long successfullyBuildDependencies = projectBuildTasks.stream().filter(filterSuccess).count();
        return successfullyBuildDependencies == projectBuildTasks.size();
    }

    enum Status {
        NEW, BUILDING, DONE;
    }
}
