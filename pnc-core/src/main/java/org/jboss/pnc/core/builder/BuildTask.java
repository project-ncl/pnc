package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.BuildJobConfiguration;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
public class BuildTask {

    private static final AtomicInteger jobCounter = new AtomicInteger();
    private final int id;

    private Set<BuildTask> runningBuilds;
    private BuildTaskQueue buildTaskQueue;
    private ProjectBuildConfiguration projectBuildConfiguration;
    private Consumer<TaskStatus> onStatusUpdate;
    private Consumer<BuildJobDetails> onComplete;
    private TaskStatus status;
    private long lastStatusUpdate;

    private BuildJobDetails buildJobDetails;
    private BuildJobConfiguration buildJobConfiguration;
    private RepositoryConfiguration repositoryConfiguration;

    private Exception exception = null;

    Logger log = Logger.getLogger(BuildTask.class);

    public BuildTask(Set<BuildTask> runningBuilds, BuildTaskQueue buildTaskQueue, ProjectBuildConfiguration projectBuildConfiguration, Consumer<TaskStatus> onStatusUpdate, Consumer<BuildJobDetails> onComplete) {
        this.runningBuilds = runningBuilds;
        this.buildTaskQueue = buildTaskQueue;
        this.projectBuildConfiguration = projectBuildConfiguration;
        this.onStatusUpdate = onStatusUpdate;
        this.onComplete = onComplete;
        status = new TaskStatus(TaskStatus.Operation.NEW, TaskStatus.State.COMPLETED);

        buildJobConfiguration = new BuildJobConfiguration(projectBuildConfiguration);

        this.id = jobCounter.getAndIncrement();
    }

    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Sets new status to task, if status state is State.COMPLETED task is added back to work queue.
     * Note to set all required fields for further operations before calling this method.
     * onStatusUpdate is called
     *
     * @param newStatus
     */
    public void onStatusUpdate(TaskStatus newStatus) {
        log.tracef("Updating task %s status to %s", toString(), newStatus.toString());
        lastStatusUpdate = System.currentTimeMillis();
        status = newStatus;
        onStatusUpdate.accept(newStatus);
        if (newStatus.isCompleted()) {
            buildTaskQueue.add(this);
        }
    }

    public void onComplete() {
        onComplete.accept(buildJobDetails);
        runningBuilds.remove(this);
    }

    public void onError(Exception e) {
        exception = e;
        buildTaskQueue.add(this); //task will be taken from queue by error handler
    }

    public void setBuildJobDetails(BuildJobDetails buildJobDetails) {
        this.buildJobDetails = buildJobDetails;
    }

    public BuildJobDetails getBuildJobDetails() {
        return buildJobDetails;
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

    public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
        this.repositoryConfiguration = repositoryConfiguration;
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    public BuildJobConfiguration getBuildJobConfiguration() {
        return buildJobConfiguration;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildTask buildTask = (BuildTask) o;

        if (projectBuildConfiguration != null ? !projectBuildConfiguration.equals(buildTask.projectBuildConfiguration) : buildTask.projectBuildConfiguration != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return projectBuildConfiguration != null ? projectBuildConfiguration.hashCode() : 0;
    }
}
