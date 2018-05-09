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

import lombok.Getter;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
*/
public class BuildTask {

    private static final Logger log = LoggerFactory.getLogger(BuildTask.class);

    private final Integer id;
    private final BuildConfiguration buildConfiguration; //TODO decouple DB entity
    private final BuildConfigurationAudited buildConfigurationAudited; //TODO decouple DB entity

    @Getter
    private final BuildOptions buildOptions;

    private final User user;
    private final Date submitTime;

    private final String contentId;

    private Date startTime;
    private Date endTime;

    private BuildCoordinationStatus status = BuildCoordinationStatus.NEW;
    private String statusDescription;

    /**
     * A list of builds waiting for this build to complete.
     */
    private final Set<BuildTask> dependants = new HashSet<>();

    /**
     * The builds which must be completed before this build can start
     */
    private Set<BuildTask> dependencies = new HashSet<>();

    private final BuildSetTask buildSetTask;

    private ProductMilestone productMilestone;

    private boolean hasFailed = false;

    //called when all dependencies are built
    private final Integer buildConfigSetRecordId;

    private BuildTask(BuildConfiguration buildConfiguration,
                      BuildConfigurationAudited buildConfigurationAudited,
                      BuildOptions buildOptions,
                      User user,
                      Date submitTime,
                      BuildSetTask buildSetTask,
                      int id,
                      Integer buildConfigSetRecordId,
                      ProductMilestone productMilestone,
                      String contentId) {

        this.id = id;
        this.buildConfiguration = buildConfiguration;
        this.buildConfigurationAudited = buildConfigurationAudited;
        this.buildOptions = buildOptions;
        this.user = user;
        this.submitTime = submitTime;

        this.buildSetTask = buildSetTask;
        this.buildConfigSetRecordId = buildConfigSetRecordId;
        this.productMilestone = productMilestone;
        this.contentId = contentId;

    }

    public void setStatus(BuildCoordinationStatus status) {
        this.status = status;
        setHasFailed(status.hasFailed());
    }

    public ProductMilestone getProductMilestone() {
        return productMilestone;
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

    /**
     * @return current status
     */
    public BuildCoordinationStatus getStatus() {
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

    /**
     * Check if this build task has a build configuration dependency on the given build task
     *
     * @param buildTask The buildTask with the config to check
     * @return true if this task's build config has a dependency on the build config of the given task, otherwise false
     */
    public boolean hasConfigDependencyOn(BuildTask buildTask) {
        if (buildTask == null || this.equals(buildTask)) {
            return false;
        }
        if (buildConfiguration == null || buildConfiguration.getAllDependencies() == null) {
            return false;
        }
        return buildConfiguration.dependsOn(buildTask.getBuildConfiguration());
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

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public boolean hasFailed(){
        return this.hasFailed;
    }

    void setHasFailed(boolean hasFailed){
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

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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
     * @return true if already built, false otherwise
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
        return "Build Task id:" + id + ", name: " + buildConfigurationAudited.getName() + ", project name: " + buildConfigurationAudited.getProject().getName() + ", status: " + status;
    }

    public static BuildTask build(BuildConfiguration buildConfiguration,
                                  BuildConfigurationAudited buildConfigAudited,
                                  BuildOptions buildOptions, User user,
                                  int buildTaskId,
                                  BuildSetTask buildSetTask,
                                  Date submitTime,
                                  ProductMilestone productMilestone,
                                  String contentId) {

        Integer buildConfigSetRecordId = null;
        if (buildSetTask != null) {
            buildConfigSetRecordId =
                    buildSetTask.getBuildConfigSetRecord().map(BuildConfigSetRecord::getId).orElse(null);
        }

        return new BuildTask(
                buildConfiguration,
                buildConfigAudited,
                buildOptions,
                user,
                submitTime,
                buildSetTask,
                buildTaskId,
                buildConfigSetRecordId,
                productMilestone,
                contentId);
    }


    public Integer getBuildConfigSetRecordId() {
        return buildConfigSetRecordId;
    }

    public String getContentId() {
        return contentId;
    }
}
