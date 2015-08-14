/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.BuildExecutionType;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
*/
public class BuildTask implements BuildExecution {

    public static final Logger log = LoggerFactory.getLogger(BuildTask.class);

    private BuildRecord buildRecord;

    private BuildExecutionType buildTaskType;
    private BuildStatus status = BuildStatus.NEW;
    private String statusDescription;

    private Event<BuildStatusChangedEvent> buildStatusChangedEvent;

    /**
     * A list of builds waiting for this build to complete.
     */
    private Set<BuildTask> dependants;

    /**
     * The builds which must be completed before this build can start
     */
    private Set<BuildTask> dependencies;

    private BuildCoordinator buildCoordinator;

    private String topContentId;

    private String buildSetContentId;

    private String buildContentId;

    private BuildSetTask buildSetTask;

    private final AtomicReference<URI> logsWebSocketLink = new AtomicReference<>();
    private boolean hasFailed = false;

    BuildTask(BuildCoordinator buildCoordinator, 
            BuildConfiguration buildConfiguration, 
            BuildConfigurationAudited buildConfigurationAudited,
            String topContentId,
              String buildSetContentId,
              String buildContentId, 
              BuildExecutionType buildTaskType, 
              User user, 
              BuildSetTask buildSetTask,
              int buildTaskId) {

        this.buildCoordinator = buildCoordinator;
        this.buildTaskType = buildTaskType;
        this.buildStatusChangedEvent = buildCoordinator.getBuildStatusChangedEventNotifier();
        this.topContentId = topContentId;
        this.buildSetContentId = buildSetContentId;
        this.buildContentId = buildContentId;
        this.buildSetTask = buildSetTask;

        this.buildRecord = BuildRecord.Builder.newBuilder().id(buildTaskId)
                .latestBuildConfiguration(buildConfiguration)
                .buildConfigurationAudited(buildConfigurationAudited)
                .user(user)
                .startTime(new Date())
                .build();

        // The the buildconfigsetrecord has a non-null ID, then this is a build set and not a single build
        if (buildSetTask.getBuildConfigSetRecord().getId() != null) {
            buildRecord.setBuildConfigSetRecord(buildSetTask.getBuildConfigSetRecord());
        }
        if (buildSetTask.getProductMilestone() != null) {
            this.buildRecord.addBuildRecordSet(buildSetTask.getProductMilestone().getPerformedBuildRecordSet());
        }
        if (buildConfiguration.getProductVersions() != null) {
            for (ProductVersion productVersion : buildConfiguration.getProductVersions()) {
                if (productVersion.getCurrentProductMilestone() != null) {
                    this.buildRecord
                            .addBuildRecordSet(productVersion.getCurrentProductMilestone().getPerformedBuildRecordSet());
                }
            }
        }

        dependants = new HashSet<>();
        dependencies = new HashSet<>();
    }

    public BuildRecord getBuildRecord() {
        return this.buildRecord;
    }

    public void setStatus(BuildStatus status) {
        BuildStatus oldStatus = this.status;
        this.status = status;
        if (status.hasFailed()) {
            setHasFailed(true);
        }
        Integer userId = Optional.ofNullable(getUser()).map(user -> user.getId()).orElse(null);
        BuildStatusChangedEvent buildStatusChanged = new DefaultBuildStatusChangedEvent(oldStatus, status, getId(),
                buildRecord.getBuildConfigurationAudited().getId().getId(), userId);
        log.debug("Updating build task {} status to {}", this.getId(), buildStatusChanged);
        buildSetTask.taskStatusUpdated(buildStatusChanged);
        buildStatusChangedEvent.fire(buildStatusChanged);
        if (status.isCompleted()) {
            dependants.forEach((dep) -> dep.requiredBuildCompleted(this));
        }
    }

    public Set<BuildTask> getDependencies() {
        return dependencies;
    }

    public void addDependency(BuildTask buildTask) {
        if (!dependencies.contains(buildTask)) {
            dependencies.add(buildTask);
            buildTask.addDependant(this);
        }
    }

    void setRequiredBuilds(List<BuildTask> dependencies) {
        this.dependencies.addAll(dependencies);
    }

    void setRequiredBuilds(Set<BuildTask> dependencies) {
        this.dependencies = dependencies;
    }

    private void requiredBuildCompleted(BuildTask completed) {
        if (dependencies.contains(completed) && completed.hasFailed()) {
            this.setStatus(BuildStatus.REJECTED);
        } else if (dependencies.stream().allMatch(dep -> dep.getStatus().isCompleted())) {
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

    public BuildConfigurationAudited getBuildConfigurationAudited() {
        return buildRecord.getBuildConfigurationAudited();
    }

    public Set<BuildConfiguration> getBuildConfigurationDependencies() {
        return buildRecord.getLatestBuildConfiguration().getDependencies();
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

    public void addDependant(BuildTask buildTask) {
        if (!dependants.contains(buildTask)) {
            dependants.add(buildTask);
            buildTask.addDependency(this);
        }
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

        return buildRecord.getBuildConfigurationAudited().equals(buildTask.buildRecord.getBuildConfigurationAudited());

    }

    @Override
    public int hashCode() {
        return buildRecord.hashCode();
    }

    void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public boolean hasFailed(){
        return this.hasFailed;
    }

    public void setHasFailed(boolean hasFailed){
       this.hasFailed = hasFailed;
    }

    public int getId() {
        return buildRecord.getId();
    }

    @Override
    public String getProjectName() {
        return buildRecord.getBuildConfigurationAudited().getProject().getName();
    }

    public BuildExecutionType getBuildExecutionType() {
        return buildTaskType;
    }

    public Date getStartTime() {
        return buildRecord.getStartTime();
    }

    public Date getEndTime() {
        return buildRecord.getEndTime();
    }

    public void setEndTime(Date endTime) {
        buildRecord.setEndTime(endTime);
    }

    public User getUser() {
        return buildRecord.getUser();
    }

    public BuildSetTask getBuildSetTask() {
        return buildSetTask;
    }

    /**
     * Check if this build is ready to build, for example if all dependency builds
     * are complete.
     * 
     * @return
     */
    public boolean readyToBuild() {
        for (BuildTask buildTask : dependencies) {
            if(!buildTask.getStatus().isCompleted()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setLogsWebSocketLink(URI link) {
        this.logsWebSocketLink.set(link);
    }

    @Override
    public void clearLogsWebSocketLink() {
        this.logsWebSocketLink.set(null);
    }

    @Override
    public Optional<URI> getLogsWebSocketLink() {
        return Optional.ofNullable(logsWebSocketLink.get());
    }

    @Override
    public String toString() {
        return "id :" + buildRecord.getBuildConfigurationAudited() + " " + status;
    }
}
