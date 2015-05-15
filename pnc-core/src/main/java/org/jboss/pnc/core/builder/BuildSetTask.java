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

import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.BuildExecutionType;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-03-26.
 */
public class BuildSetTask {

    private BuildConfigurationSet buildConfigurationSet;

    private final BuildExecutionType buildTaskType;

    private BuildStatus status;

    private String statusDescription;
    private Set<BuildTask> buildTasks = new HashSet<>();

    public BuildSetTask(BuildConfigurationSet buildConfigurationSet, BuildExecutionType buildTaskType) {
        this.buildConfigurationSet = buildConfigurationSet;
        this.buildTaskType = buildTaskType;
    }

    public BuildConfigurationSet getBuildConfigurationSet() {
        return buildConfigurationSet;
    }

    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    public BuildStatus getStatus() {
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
        return buildConfigurationSet.getId();
    }

    public BuildExecutionType getBuildTaskType() {
        return buildTaskType;
    }
}
