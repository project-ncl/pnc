package org.jboss.pnc.core.builder;

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.model.builder.BuildDetails;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
public class BuildTask extends ProjectBuildConfiguration {
    private Set<BuildTask> runningBuilds;
    private BuildTaskQueue buildTaskQueue;
    private ProjectBuildConfiguration projectBuildConfiguration;
    private BuildCollection buildCollection;
    private Consumer<TaskStatus> onStatusUpdate;
    private Consumer<BuildDetails> onComplete;
    private TaskStatus status;
    private long lastStatusUpdate;
    private RepositoryConfiguration repositoryConfiguration;
    private BuildDetails buildDetails; //TODO move all build related fields under BuildDetails
    private Exception exception = null;

    public BuildTask(Set<BuildTask> runningBuilds, BuildTaskQueue buildTaskQueue, ProjectBuildConfiguration projectBuildConfiguration, BuildCollection buildCollection, Consumer<TaskStatus> onStatusUpdate, Consumer<BuildDetails> onComplete) {
        this.runningBuilds = runningBuilds;
        this.buildTaskQueue = buildTaskQueue;
        this.projectBuildConfiguration = projectBuildConfiguration;
        this.buildCollection = buildCollection;
        this.onStatusUpdate = onStatusUpdate;
        this.onComplete = onComplete;
        status = new TaskStatus(TaskStatus.Operation.NEW, TaskStatus.State.COMPLETED);

        this.buildTaskQueue.add(this); //TODO move out of constructor, create builder ?
        this.runningBuilds.add(this); //TODO move out of constructor, create builder ?
    }

    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Sets new status to task, if status state is State.COMPLETED task is added back to work queue.
     * onStatusUpdate is called
     *
     * @param newStatus
     */
    public void onStatusUpdate(TaskStatus newStatus) {
        lastStatusUpdate = System.currentTimeMillis();
        status = newStatus;
        onStatusUpdate.accept(newStatus);
        if (newStatus.isCompleted()) {
            buildTaskQueue.add(this);
        }
    }

    public void onComplete() {
        onComplete.accept(buildDetails);
        runningBuilds.remove(this);
    }

    public void onError(Exception e) {
        exception = e;
        buildTaskQueue.add(this); //task will be taken from queue by error handler
    }

    public ProjectBuildConfiguration getProjectBuildConfiguration() {
        return projectBuildConfiguration;
    }

    public BuildCollection getBuildCollection() {
        return buildCollection;
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
        this.repositoryConfiguration = repositoryConfiguration;
    }

    public void setBuildDetails(BuildDetails buildDetails) {
        this.buildDetails = buildDetails;
    }

    public BuildDetails getBuildDetails() {
        return buildDetails;
    }

    public long getLastStatusUpdate() {
        return lastStatusUpdate;
    }

    public long getLastStatusUpdateDiff() {
        return System.currentTimeMillis() - lastStatusUpdate;
    }

    public Exception getException() {
        return exception;
    }
}
