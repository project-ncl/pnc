package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.util.collection.WeakSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
*/
public class BuildTask implements BuildExecution {

    public static final Logger log = LoggerFactory.getLogger(BuildTask.class);

    public BuildConfiguration buildConfiguration;
    BuildStatus status = BuildStatus.NEW;
    private String statusDescription;

    private Set<Consumer<BuildStatusChangedEvent>> statusUpdateListeners;
    private Set<Consumer<String>> logConsumers;

    /**
     * A list of builds waiting for this build to complete.
     */
    private Set<BuildTask> waiting;
    private List<BuildTask> requiredBuilds;
    private BuildCoordinator buildCoordinator;

    private String topContentId;

    private String buildSetContentId;

    private String buildContentId;

    BuildTask(BuildCoordinator buildCoordinator, BuildConfiguration buildConfiguration, String topContentId,
            String buildSetContentId, String buildContentId) {
        this.buildCoordinator = buildCoordinator;
        this.buildConfiguration = buildConfiguration;
        this.topContentId = topContentId;
        this.buildSetContentId = buildSetContentId;
        this.buildContentId = buildContentId;
        statusUpdateListeners = new WeakSet();
        logConsumers = new WeakSet();
        waiting = new HashSet<>();
    }

    BuildTask(BuildCoordinator buildCoordinator, BuildConfiguration buildConfiguration, String topContentId,
            String buildSetContentId, String buildContentId, Set<Consumer<BuildStatusChangedEvent>> statusUpdateListeners,
            Set<Consumer<String>> logConsumers) {
        this(buildCoordinator, buildConfiguration, topContentId, buildSetContentId, buildContentId);
        this.statusUpdateListeners.addAll(statusUpdateListeners);
        this.logConsumers.addAll(logConsumers);
    }

    public void registerStatusUpdateListener(Consumer<BuildStatusChangedEvent> statusUpdateListener) {
        this.statusUpdateListeners.add(statusUpdateListener);
    }

    public void registerLogConsumer(Consumer<String> logConsumer) {
        this.logConsumers.add(logConsumer);
    }

    public void setStatus(BuildStatus status) {
        BuildStatusChangedEvent buildStatusChangedEvent = new DefaultBuildStatusChangedEvent(this.status, status,
                buildConfiguration.getId(), this);
        log.debug("Updating build task {} status to {}", this.getId(), buildStatusChangedEvent);

        statusUpdateListeners.forEach(consumer -> consumer.accept(buildStatusChangedEvent));
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

    public BuildConfiguration getBuildConfiguration() {
        return buildConfiguration;
    }

    @Override
    public String getTopContentId() {
        return topContentId;
    }

    @Override
    public String getBuildSetContentId() {
        return buildSetContentId;
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    void addWaiting(BuildTask buildTask) {
        waiting.add(buildTask);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BuildTask buildTask = (BuildTask) o;

        return buildConfiguration.equals(buildTask.getBuildConfiguration());

    }

    @Override
    public int hashCode() {
        return buildConfiguration.hashCode();
    }

    void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }


    public Integer getId() {
        return buildConfiguration.getId();
    }

    public String getBuildLog() {
        return null;//TODO reference to progressive log
    }

    @Override
    public String getProjectName() {
        return buildConfiguration.getProject().getName();
    }

}
