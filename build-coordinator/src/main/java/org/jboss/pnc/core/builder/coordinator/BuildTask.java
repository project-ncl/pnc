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
package org.jboss.pnc.core.builder.coordinator;

import org.jboss.pnc.core.builder.datastore.BuildConfigurationUtils;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.User;
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
import java.util.function.Consumer;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
*/
public class BuildTask {

    public static final Logger log = LoggerFactory.getLogger(BuildTask.class);

    private Integer id;
    private BuildConfiguration buildConfiguration;
    private BuildConfigurationAudited buildConfigurationAudited;
    private User user;
    private Date submitTime;
    private Date startTime;
    private Date endTime;

    private BuildStatus status = BuildStatus.NEW;
    private String statusDescription;

    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    /**
     * A list of builds waiting for this build to complete.
     */
    private Set<BuildTask> dependants = new HashSet<>();

    /**
     * The builds which must be completed before this build can start
     */
    private Set<BuildTask> dependencies = new HashSet<>();

    private BuildSetTask buildSetTask;

    private Set<Integer> buildRecordSetIds = new HashSet<>();

    private final AtomicReference<URI> logsWebSocketLink = new AtomicReference<>();
    private boolean hasFailed = false;

    //called when all dependencies are built
    private Consumer<BuildTask> onAllDependenciesCompleted;
    private Integer buildConfigSetRecordId;
    private final boolean rebuildAll;

    private BuildTask(BuildConfiguration buildConfiguration,
                      BuildConfigurationAudited buildConfigurationAudited,
                      String topContentId,
                      String buildContentId,
                      User user,
                      Date submitTime,
                      BuildSetTask buildSetTask,
                      int id,
                      Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier,
                      Consumer<BuildTask> onAllDependenciesCompleted,
                      Integer buildConfigSetRecordId,
                      boolean rebuildAll) {

        this.id = id;
        this.buildConfiguration = buildConfiguration;
        this.buildConfigurationAudited = buildConfigurationAudited;
        this.user = user;
        this.submitTime = submitTime;

        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetTask = buildSetTask;
        this.onAllDependenciesCompleted = onAllDependenciesCompleted;
        this.buildConfigSetRecordId = buildConfigSetRecordId;
        this.rebuildAll = rebuildAll;

        if (buildSetTask != null && buildSetTask.getProductMilestone() != null) {
            buildRecordSetIds.add(buildSetTask.getProductMilestone().getPerformedBuildRecordSet().getId());
        }
        if (buildConfiguration.getProductVersions() != null) {
            for (ProductVersion productVersion : buildConfiguration.getProductVersions()) {
                if (productVersion.getCurrentProductMilestone() != null) {
                    buildRecordSetIds.add(productVersion.getCurrentProductMilestone().getPerformedBuildRecordSet().getId());
                }
            }
        }

    }

    public void setStatus(BuildStatus status) {
        BuildStatus oldStatus = this.status;
        this.status = status;
        if (status.hasFailed()) {
            setHasFailed(true);
        }
        Integer userId = Optional.ofNullable(getUser()).map(user -> user.getId()).orElse(null);
        BuildStatusChangedEvent buildStatusChanged = new DefaultBuildStatusChangedEvent(oldStatus, status, getId(),
                buildConfigurationAudited.getId().getId(), userId);
        log.debug("Updating build task {} status to {}", this.getId(), buildStatusChanged);
        if (buildSetTask != null) {
            buildSetTask.taskStatusUpdated(buildStatusChanged);
        }
        buildStatusChangedEventNotifier.fire(buildStatusChanged);
        if (status.isCompleted()) {
            dependants.forEach((dep) -> dep.requiredBuildCompleted(this));
        }
    }

    public Set<Integer> getBuildRecordSetIds() {
        return buildRecordSetIds;
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
            onAllDependenciesCompleted.accept(this);
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

    public BuildConfigurationAudited getBuildConfigurationAudited() {
        return buildConfigurationAudited;
    }

    public Set<BuildConfiguration> getBuildConfigurationDependencies() {
        return buildConfiguration.getDependencies();
    }

    public void addDependant(BuildTask buildTask) {
        if (!dependants.contains(buildTask)) {
            dependants.add(buildTask);
            buildTask.addDependency(this);
        }
    }

    /**
     * A build task is equal to another build task if they are using the same
     * build configuration ID and version.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BuildTask buildTask = (BuildTask) o;
        return buildConfigurationAudited.equals(buildTask.getBuildConfigurationAudited());
    }

    @Override
    public int hashCode() {
        return buildConfigurationAudited.hashCode();
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
        return id;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public User getUser() {
        return user;
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
    public String toString() {
        return "Build Task id:" + id + ", name: " + buildConfigurationAudited.getName() + ", status: " + status;
    }

    public static BuildTask build(BuildConfiguration buildConfiguration,
            BuildConfigurationAudited buildConfigAudited,
            User user,
            Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier,
            Consumer<BuildTask> onAllDependenciesCompleted,
            int buildTaskId,
            BuildSetTask buildSetTask,
            Date submitTime,
            boolean rebuildAll) {
        String topContentId = ContentIdentityManager.getProductContentId(BuildConfigurationUtils.getFirstProductVersion(buildConfiguration));
        String buildContentId = ContentIdentityManager.getBuildContentId(buildConfiguration);

        Integer buildConfigSetRecordId = null;
        if (buildSetTask != null && buildSetTask.getBuildConfigSetRecord() != null) {
            buildConfigSetRecordId = buildSetTask.getBuildConfigSetRecord().getId();
        }

        return new BuildTask(
                buildConfiguration,
                buildConfigAudited,
                topContentId,
                buildContentId,
                user,
                submitTime,
                buildSetTask,
                buildTaskId,
                buildStatusChangedEventNotifier,
                onAllDependenciesCompleted,
                buildConfigSetRecordId,
                rebuildAll);
    }


    public Integer getBuildConfigSetRecordId() {
        return buildConfigSetRecordId;
    }

    public boolean getRebuildAll() {
        return rebuildAll;
    }
}
