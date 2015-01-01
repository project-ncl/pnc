package org.jboss.pnc.core.builder;

import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.util.collection.WeakSet;

import java.util.Set;
import java.util.function.Consumer;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
*/
public class SubmittedBuild {
    public ProjectBuildConfiguration projectBuildConfiguration;
    BuildStatus status = BuildStatus.NEW;
    private String statusDescription;

    private Set<Consumer<BuildStatus>> statusUpdateListeners;
    private Set<Consumer<String>> logConsumers;

    SubmittedBuild(ProjectBuildConfiguration projectBuildConfiguration) {
        statusUpdateListeners = new WeakSet();
        logConsumers = new WeakSet();
        this.projectBuildConfiguration = projectBuildConfiguration;
    }

    SubmittedBuild(ProjectBuildConfiguration projectBuildConfiguration, Set<Consumer<BuildStatus>> statusUpdateListeners, Set<Consumer<String>> logConsumers) {
        this(projectBuildConfiguration);
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
        statusUpdateListeners.forEach(consumer -> consumer.accept(status));
        this.status = status;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubmittedBuild submittedBuild = (SubmittedBuild) o;

        return projectBuildConfiguration.equals(submittedBuild.getProjectBuildConfiguration());

    }

    @Override
    public int hashCode() {
        return projectBuildConfiguration.hashCode();
    }

    void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getIdentifier() {
        return projectBuildConfiguration.getIdentifier();
    }

    public String getBuildLog() {
        return null;//TODO reference to progressive log
    }
}
