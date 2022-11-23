/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.BuildOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
public class BuildTask {

    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.build-task");

    @Getter
    private final String id;

    @Getter
    private final BuildConfigurationAudited buildConfigurationAudited;

    @Getter
    private final BuildOptions buildOptions;

    @Getter
    private final User user;

    @Getter
    private final Date submitTime;

    @Getter
    private final String contentId;

    @Getter
    @Setter
    private Date startTime;

    @Getter
    @Setter
    private Date endTime;

    @Getter
    @Setter
    private BuildCoordinationStatus status = BuildCoordinationStatus.NEW;

    /**
     * Description of current status. Eg. WAITING: there is no available executor; FAILED: exceptionMessage
     */
    @Getter
    @Setter
    private String statusDescription;

    /**
     * A list of builds waiting for this build to complete.
     */
    @Getter
    private final Set<BuildTask> dependants = new HashSet<>();

    /**
     * The builds which must be completed before this build can start
     */
    @Getter
    private Set<BuildTask> dependencies = new HashSet<>();

    @Getter
    private final BuildSetTask buildSetTask;

    @Getter
    private final ProductMilestone productMilestone;

    // called when all dependencies are built
    private Integer buildConfigSetRecordId;

    /**
     * This BR is set when Build Task is not required to be built.
     */
    @Getter
    @Setter
    private BuildRecord noRebuildCause;

    /**
     * Request that started the builds
     */
    private final String requestContext;

    private BuildTask(
            BuildConfigurationAudited buildConfigurationAudited,
            BuildOptions buildOptions,
            User user,
            Date submitTime,
            BuildSetTask buildSetTask,
            String id,
            Integer buildConfigSetRecordId,
            ProductMilestone productMilestone,
            String contentId,
            String requestContext) {

        this.id = id;
        this.buildConfigurationAudited = buildConfigurationAudited;
        this.buildOptions = buildOptions;
        this.user = user;
        this.submitTime = submitTime;

        this.buildSetTask = buildSetTask;
        this.buildConfigSetRecordId = buildConfigSetRecordId;
        this.productMilestone = productMilestone;
        this.contentId = contentId;

        this.requestContext = requestContext;
    }

    public void addDependency(BuildTask buildTask) {
        if (!dependencies.contains(buildTask)) {
            dependencies.add(buildTask);
            buildTask.addDependant(this);
        }
    }

    public void addDependant(BuildTask buildTask) {
        if (!dependants.contains(buildTask)) {
            dependants.add(buildTask);
            buildTask.addDependency(this);
        }
    }

    /**
     * Check if this build task has a build configuration dependency on the given build task. The search include
     * transitive dependencies.
     *
     * @param buildTask The buildTask with the config to check
     * @return true if this task's build config has a dependency (including transitive) on the build config of the given
     *         task, otherwise false
     */
    public boolean hasConfigDependencyOn(BuildTask buildTask) {
        if (buildTask == null || this.equals(buildTask)) {
            return false;
        }

        BuildConfiguration buildConfiguration = buildConfigurationAudited.getBuildConfiguration();
        if (buildConfiguration == null || buildConfiguration.getAllDependencies() == null) {
            return false;
        }

        return buildConfiguration.dependsOn(buildTask.getBuildConfigurationAudited().getBuildConfiguration());
    }

    /**
     * Check if this build task has a direct build configuration dependency on the given build task
     *
     * @param buildTask The buildTask with the config to check
     * @return true if this task's build config has a direct dependency on the build config of the given task, otherwise
     *         false
     */
    public boolean hasDirectConfigDependencyOn(BuildTask buildTask) {
        if (buildTask == null || this.equals(buildTask)) {
            return false;
        }

        BuildConfiguration buildConfiguration = buildConfigurationAudited.getBuildConfiguration();
        if (buildConfiguration == null || buildConfiguration.getDependencies() == null) {
            return false;
        }

        return buildConfiguration.getDependencies()
                .contains(buildTask.getBuildConfigurationAudited().getBuildConfiguration());
    }

    public Optional<String> getRequestContext() {
        return Optional.ofNullable(requestContext);
    }

    public Integer getBuildConfigSetRecordId() {
        if (buildConfigSetRecordId == null && buildSetTask != null
                && buildSetTask.getBuildConfigSetRecord().isPresent()) {
            buildConfigSetRecordId = buildSetTask.getBuildConfigSetRecord().get().getId();
        }
        return buildConfigSetRecordId;
    }

    public boolean hasFailed() {
        return status.hasFailed();
    }

    /**
     * A build task is equal to another build task if they are using the same build configuration ID and version.
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

        return getBuildConfigurationAudited().equals(buildTask.getBuildConfigurationAudited());
    }

    @Override
    public int hashCode() {
        return getBuildConfigurationAudited().hashCode();
    }

    @Override
    public String toString() {
        return "Build Task id:" + id + ", name: " + getBuildConfigurationAudited().getName() + ", project name: "
                + getBuildConfigurationAudited().getProject().getName() + ", status: " + status;
    }

    public static BuildTask build(
            BuildConfigurationAudited buildConfigurationAudited,
            BuildOptions buildOptions,
            User user,
            String buildTaskId,
            BuildSetTask buildSetTask,
            Date submitTime,
            ProductMilestone productMilestone,
            String contentId,
            String requestContext) {

        Integer buildConfigSetRecordId = null;
        if (buildSetTask != null) {
            buildConfigSetRecordId = buildSetTask.getBuildConfigSetRecord()
                    .map(BuildConfigSetRecord::getId)
                    .orElse(null);
        }

        ProductMilestone milestone = productMilestone;
        if (milestone != null && milestone.getEndDate() != null) {
            userLog.warn(
                    "Not using current milestone {} for build task {}, because the milestone is closed.",
                    productMilestone,
                    buildTaskId);
            milestone = null;
        }

        return new BuildTask(
                buildConfigurationAudited,
                buildOptions,
                user,
                submitTime,
                buildSetTask,
                buildTaskId,
                buildConfigSetRecordId,
                milestone,
                contentId,
                requestContext);
    }
}
