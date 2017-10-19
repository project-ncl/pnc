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
package org.jboss.pnc.spi.coordinator;

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.BuildSetStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-03-26.
 */
public class BuildSetTask {

    private final Logger log = LoggerFactory.getLogger(BuildCoordinator.class);

    private final Optional<BuildConfigSetRecord> buildConfigSetRecord;

    private final boolean forceRebuildAll;
    private final boolean keepAfterFailure;

    private BuildSetStatus status;

    private String statusDescription;

    private Date startTime;

    private final Set<BuildTask> buildTasks = new HashSet<>();

    /**
     * Create build set task for running a single build or set of builds
     * 
     * @param buildConfigSetRecord The config set record which will be stored to the db
     * @param forceRebuildAll Rebuild all configs in the set regardless of whether they were built previously
     * @param keepAfterFailure Don't stop the pod after build failure
     */
    private BuildSetTask(
            BuildConfigSetRecord buildConfigSetRecord, //TODO decouple datastore entity
            boolean forceRebuildAll,
            boolean keepAfterFailure) {
        this.buildConfigSetRecord = Optional.ofNullable(buildConfigSetRecord);
        this.forceRebuildAll = forceRebuildAll;
        this.keepAfterFailure = keepAfterFailure;
    }

    public void setStatus(BuildSetStatus status) {
        this.status = status;
    }

    /**
     * Notify the set that the state of one of it's tasks has changed.
     *
     */
    public void taskStatusUpdatedToFinalState() {
        // If any of the build tasks have failed or all are complete, then the build set is done
        if(buildTasks.stream().anyMatch(bt -> bt.getStatus().hasFailed())) {
            log.debug("Marking build set as FAILED as one or more tasks failed.");
            if (log.isDebugEnabled()) {
                logTasksStatus(buildTasks);
            }
            buildConfigSetRecord.ifPresent(r -> r.setStatus(BuildStatus.FAILED));
            finishBuildSetTask();
        } else if (buildTasks.stream().allMatch(bt -> bt.getStatus().isCompleted())) {
            log.debug("Marking build set as SUCCESS.");
            buildConfigSetRecord.ifPresent(r -> r.setStatus(BuildStatus.SUCCESS));
            finishBuildSetTask();
        } else {
            if (log.isTraceEnabled()) {
                List<Integer> running = buildTasks.stream()
                        .filter(bt -> !bt.getStatus().isCompleted())
                        .filter(bt -> !bt.getStatus().hasFailed())
                        .map(BuildTask::getId)
                        .collect(Collectors.toList());
                log.trace("There are still running or waiting builds [{}].", running);
            }
        }
    }

    private void logTasksStatus(Set<BuildTask> buildTasks) {
        String taskStatuses = buildTasks.stream().map(bt -> "TaskId " + bt.getId() + ":" + bt.getStatus()).collect(Collectors.joining("; "));
        log.debug("Tasks statuses: {}", taskStatuses);
    }

    private void finishBuildSetTask() {
        buildConfigSetRecord.ifPresent(r -> r.setEndTime(new Date()));
    }

    public BuildSetStatus getStatus() {
        return status;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Set<BuildTask> getBuildTasks() {
        return buildTasks;
    }

    public void addBuildTask(BuildTask buildTask) {
        buildTasks.add(buildTask);
    }

    /**
     * Get the build task which contains the given audited build configuration
     * 
     * @param buildConfig
     * @return The build task with the matching configuration, or null if there is none
     */
    public BuildTask getBuildTask(BuildConfiguration buildConfig) {
        return buildTasks.stream().filter((bt) -> bt.getBuildConfiguration().equals(buildConfig)).findFirst().orElse(null);
    }

    public Integer getId() {
        return buildConfigSetRecord.map(BuildConfigSetRecord::getId).orElse(null);
    }

    public Optional<BuildConfigSetRecord> getBuildConfigSetRecord() {
        return buildConfigSetRecord;
    }

    public boolean getForceRebuildAll() {
        return forceRebuildAll;
    }

    public boolean isKeepAfterFailure() {
        return keepAfterFailure;
    }

    public static class Builder {
        private BuildConfigSetRecord buildConfigSetRecord; //TODO decouple datastore entity
        private boolean forceRebuildAll;
        private boolean keepAfterFailure;
        private Date startTime;

        private Builder() {}

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder buildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
            this.buildConfigSetRecord = buildConfigSetRecord;
            this.startTime(buildConfigSetRecord.getStartTime());
            return this;
        }
        public Builder forceRebuildAll(boolean forceRebuildAll) {
            this.forceRebuildAll = forceRebuildAll;
            return this;
        }

        public Builder keepAfterFailure(boolean keepAfterFailure) {
            this.keepAfterFailure = keepAfterFailure;
            return this;
        }

        public Builder startTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public BuildSetTask build() {
            BuildSetTask buildSetTask = new BuildSetTask(buildConfigSetRecord, forceRebuildAll, keepAfterFailure);
            buildSetTask.startTime = this.startTime;
            return buildSetTask;
        }
    }

    @Override
    public String toString() {
        return "BuildSetTask{" +
                "status=" + status +
                ", statusDescription='" + statusDescription + '\'' +
                ", submitTime=" + getStartTime() +
                ", buildTasks=" + buildTasks +
                '}';
    }
}
