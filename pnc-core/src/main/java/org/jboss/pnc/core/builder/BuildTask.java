package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.util.collection.WeakSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
*/
public class BuildTask {

    public static final Logger log = Logger.getLogger(BuildTask.class);

    public ProjectBuildConfiguration projectBuildConfiguration;
    BuildStatus status = BuildStatus.NEW;
    private String statusDescription;

    private Set<Consumer<BuildStatus>> statusUpdateListeners;
    private Set<Consumer<String>> logConsumers;

    /**
     * A list of builds waiting for this build to complete.
     */
    private Set<BuildTask> waiting;
    private List<BuildTask> requiredBuilds;
    private BuildCoordinator buildCoordinator;

    BuildTask(BuildCoordinator buildCoordinator, ProjectBuildConfiguration projectBuildConfiguration) {
        this.buildCoordinator = buildCoordinator;
        this.projectBuildConfiguration = projectBuildConfiguration;
        statusUpdateListeners = new WeakSet();
        logConsumers = new WeakSet();
        waiting = new HashSet<>();
    }

    BuildTask(BuildCoordinator buildCoordinator, ProjectBuildConfiguration projectBuildConfiguration, Set<Consumer<BuildStatus>> statusUpdateListeners, Set<Consumer<String>> logConsumers) {
        this(buildCoordinator, projectBuildConfiguration);
        this.statusUpdateListeners.addAll(statusUpdateListeners);
        this.logConsumers.addAll(logConsumers);
    }

    public void registerStatusUpdateListener(Consumer<BuildStatus> statusUpdateListener) {
        this.statusUpdateListeners.add(statusUpdateListener);
    }

    public void registerLogConsumer(Consumer<String> logConsumer) {
        this.logConsumers.add(logConsumer);
    }

    public void setStatus(BuildStatus status) {
        log.debugf("Updating build task #%s status to %s", this.getId(), status);
        statusUpdateListeners.forEach(consumer -> consumer.accept(status));
        if (status.equals(BuildStatus.DONE)) {
            waiting.forEach((submittedBuild) -> submittedBuild.requiredBuildCompleted(this));
        }
        this.status = status;
    }

    void setRequiredBuilds(List<BuildTask> requiredBuilds) {
        this.requiredBuilds = requiredBuilds;
    }

    private void requiredBuildCompleted(BuildTask completed) {
        requiredBuilds.remove(completed);
        if (requiredBuilds.size() == 0) {
            try {
                buildCoordinator.startBuilding(this);
            } catch (CoreException e) {
                setStatus(BuildStatus.SYSTEM_ERROR);
                setStatusDescription(e.getMessage());
            }
        }
    }

    /**
     * @return current status
     */
    public BuildStatus getStatus() {
        return status;
    }

    /**
     * @return Description of current status. Eg. WAITING: there is no available executor; FAILED: exceptionMessage
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    public ProjectBuildConfiguration getProjectBuildConfiguration() {
        return projectBuildConfiguration;
    }

    void addWaiting(BuildTask buildTask) {
        waiting.add(buildTask);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildTask buildTask = (BuildTask) o;

        return projectBuildConfiguration.equals(buildTask.getProjectBuildConfiguration());

    }

    @Override
    public int hashCode() {
        return projectBuildConfiguration.hashCode();
    }

    void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }


    public Integer getId() {
        return projectBuildConfiguration.getId();
    }

    public String getBuildLog() {
        return null;//TODO reference to progressive log
    }

}
