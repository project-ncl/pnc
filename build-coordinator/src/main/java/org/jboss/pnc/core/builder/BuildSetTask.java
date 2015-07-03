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

import org.jboss.pnc.core.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildExecutionType;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.event.Event;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-03-26.
 */
public class BuildSetTask {

    private BuildConfigurationSet buildConfigurationSet;

    private final BuildExecutionType buildTaskType;
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    private BuildSetStatus status = BuildSetStatus.NEW;

    private String statusDescription;
    private Set<BuildTask> buildTasks = new HashSet<>();
    private int buildSetTaskId;

    private User user;

    public BuildSetTask(BuildCoordinator buildCoordinator, BuildConfigurationSet buildConfigurationSet,
            BuildExecutionType buildTaskType, User user, int buildSetTaskId) {
        this.buildConfigurationSet = buildConfigurationSet;
        this.buildTaskType = buildTaskType;
        this.user = user;
        this.buildSetStatusChangedEventNotifier = buildCoordinator.getBuildSetStatusChangedEventNotifier();
        this.buildSetTaskId = buildSetTaskId;
    }

    public BuildConfigurationSet getBuildConfigurationSet() {
        return buildConfigurationSet;
    }

    void setStatus(BuildSetStatus status) {
        BuildSetStatus oldStatus = this.status;
        this.status = status;
        Integer userId = Optional.ofNullable(user).map(user -> user.getId()).orElse(null);
        BuildSetStatusChangedEvent buildSetStatusChangedEvent = new DefaultBuildSetStatusChangedEvent(oldStatus, status, getId(),
                userId);
        buildSetStatusChangedEventNotifier.fire(buildSetStatusChangedEvent);
    }

    void taskStatusUpdated(BuildStatusChangedEvent buildStatusChangedEvent) {
        Long completedTasksCount = buildTasks.stream().filter(bt -> bt.getStatus().isCompleted()).count();
        //check if all tasks are completed
        if (completedTasksCount.intValue() == buildTasks.size()) {
            setStatus(BuildSetStatus.DONE);
        }
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

    public Set<BuildTask> getBuildTasks() {
        return buildTasks;
    }

    public void addBuildTask(BuildTask buildTask) {
        buildTasks.add(buildTask);
    }

    public Integer getId() {
        return buildSetTaskId;
    }

    public BuildExecutionType getBuildTaskType() {
        return buildTaskType;
    }
}
