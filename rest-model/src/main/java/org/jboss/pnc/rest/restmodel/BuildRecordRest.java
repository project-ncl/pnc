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
package org.jboss.pnc.rest.restmodel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildRecord")
@ToString
public class BuildRecordRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private Date submitTime;

    private Date startTime;

    private Date endTime;

    @ApiModelProperty(dataType = "string")
    private BuildCoordinationStatus status;

    private Integer buildConfigurationId;

    private String buildConfigurationName;

    private Integer buildConfigurationRev;

    private Integer projectId;

    private String projectName;

    private Integer userId;

    private String username;

    private String scmRepoURL;

    private String scmRevision;

    private Integer buildEnvironmentId;

    private Map<String, String> attributes = new HashMap<>();

    private String liveLogsUri;

    private Integer buildConfigSetRecordId;

    private String buildContentId;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private Boolean temporaryBuild;

    /**
     * The IDs of the build record sets which represent the builds performed for a milestone to which this build record belongs
     */
    private Integer productMilestoneId;

    /**
     * Required in order to use rsql on user
     */
    private UserRest user;

    /**
     * Required in order to use rsql on buildConfiguration
     */
    private BuildConfigurationAuditedRest buildConfigurationAudited;

    @Getter
    @Setter
    private String executionRootName;

    @Getter
    @Setter
    private String executionRootVersion;

    @Getter
    @Setter
    private Integer[] dependentBuildRecordIds;

    @Getter
    @Setter
    private Integer[] dependencyBuildRecordIds;

    public BuildRecordRest() {
    }

    public BuildRecordRest(BuildRecord buildRecord) {
        this.id = buildRecord.getId();
        this.submitTime = buildRecord.getSubmitTime();
        this.startTime = buildRecord.getStartTime();
        this.endTime = buildRecord.getEndTime();
        this.scmRepoURL = buildRecord.getScmRepoURL();
        this.scmRevision = buildRecord.getScmRevision();
        this.attributes = buildRecord.getAttributes();
        this.buildConfigurationId = buildRecord.getBuildConfigurationId();
        this.buildConfigurationRev = buildRecord.getBuildConfigurationRev();

        performIfNotNull(buildRecord.getBuildConfigurationAudited(),
                () -> buildConfigurationName = buildRecord.getBuildConfigurationAudited().getName());
        performIfNotNull(buildRecord.getBuildConfigurationAudited(),
                () -> projectId = buildRecord.getBuildConfigurationAudited().getProject().getId());
        performIfNotNull(buildRecord.getBuildConfigurationAudited(),
                () -> projectName = buildRecord.getBuildConfigurationAudited().getProject().getName());

        performIfNotNull(buildRecord.getUser(), () -> userId = buildRecord.getUser().getId());
        performIfNotNull(buildRecord.getUser(), () -> username = buildRecord.getUser().getUsername());
        performIfNotNull(buildRecord.getBuildEnvironment(), () -> buildEnvironmentId = buildRecord.getBuildEnvironment().getId());
        this.status = BuildCoordinationStatus.fromBuildStatus(buildRecord.getStatus());
        if (buildRecord.getBuildConfigSetRecord() != null)
            this.buildConfigSetRecordId = buildRecord.getBuildConfigSetRecord().getId();

        this.buildContentId = buildRecord.getBuildContentId();
        this.temporaryBuild = buildRecord.isTemporaryBuild();
        
        performIfNotNull(buildRecord.getProductMilestone(), () -> productMilestoneId = buildRecord.getProductMilestone().getId());
        performIfNotNull(buildRecord.getUser(), () -> user = new UserRest(buildRecord.getUser()));
        performIfNotNull(buildRecord.getBuildConfigurationAudited(),
                () -> buildConfigurationAudited = new BuildConfigurationAuditedRest(
                        buildRecord.getBuildConfigurationAudited()));

        executionRootName = buildRecord.getExecutionRootName();
        executionRootVersion = buildRecord.getExecutionRootVersion();

        dependencyBuildRecordIds = buildRecord.getDependencyBuildRecordIds();
        dependentBuildRecordIds = buildRecord.getDependentBuildRecordIds();
    }

    public BuildRecordRest(
            BuildExecutionSession buildExecutionSession,
            Date submitTime, UserRest user,
            BuildConfigurationAuditedRest buildConfigurationAudited) {
        this.id = buildExecutionSession.getId();
        this.submitTime = submitTime;
        this.startTime = buildExecutionSession.getStartTime();
        this.endTime = buildExecutionSession.getEndTime();
        BuildExecutionConfiguration buildExecutionConfig = buildExecutionSession.getBuildExecutionConfiguration();

        //TODO Why masking i.e. BUILD_WAITING status with BUILDING ?
        this.status = BuildCoordinationStatus.fromBuildExecutionStatus(buildExecutionSession.getStatus());
        buildExecutionSession.getLiveLogsUri().ifPresent(logsUri -> setLiveLogsUri(logsUri.toString()));

        this.userId = user.getId();
        this.username = user.getUsername();

        this.user = user;
        this.buildConfigurationAudited = buildConfigurationAudited;

        this.buildContentId = buildExecutionConfig.getBuildContentId();

        this.buildConfigurationName = buildExecutionConfig.getName();
        this.scmRepoURL = buildExecutionConfig.getScmRepoURL();
        this.scmRevision = buildExecutionConfig.getScmRevision();

        this.buildConfigurationId = buildConfigurationAudited.getId();
        this.projectId = buildConfigurationAudited.getProjectId();
        performIfNotNull(buildConfigurationAudited.getProject(),
                () -> this.projectName = buildConfigurationAudited.getProject().getName());

        this.temporaryBuild = buildExecutionConfig.isTempBuild();
    }

    public BuildRecordRest(
            Integer id,
            BuildCoordinationStatus buildCoordinationStatus,
            Date submitTime,
            Date startTime,
            Date endTime,
            UserRest user,
            BuildConfigurationAuditedRest buildConfigurationAudited,
            boolean temporaryBuild) {
        this.id = id;
        this.submitTime = submitTime;
        this.startTime = startTime;
        this.endTime = endTime;

        this.status = buildCoordinationStatus;

        this.userId = user.getId();
        this.username = user.getUsername();

        this.user = user;
        this.buildConfigurationAudited = buildConfigurationAudited;
        this.buildConfigurationId = buildConfigurationAudited.getId();
        this.buildConfigurationName = buildConfigurationAudited.getName();
        this.projectId = buildConfigurationAudited.getProjectId();
        this.temporaryBuild = temporaryBuild;

        performIfNotNull(buildConfigurationAudited.getProject(),
                () -> this.projectName = buildConfigurationAudited.getProject().getName());

    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
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

    public BuildCoordinationStatus getStatus() {
        return status;
    }

    public void setStatus(BuildCoordinationStatus status) {
        this.status = status;
    }

    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    public void setBuildConfigurationId(Integer buildConfigurationId) {
        this.buildConfigurationId = buildConfigurationId;
    }

    public Integer getBuildConfigurationRev() {
        return buildConfigurationRev;
    }

    public void setBuildConfigurationRev(Integer buildConfigurationRev) {
        this.buildConfigurationRev = buildConfigurationRev;
    }

    public String getBuildConfigurationName() {
        return buildConfigurationName;
    }

    public void setBuildConfigurationName(String buildConfigurationName) {
        this.buildConfigurationName = buildConfigurationName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getScmRepoURL() {
        return scmRepoURL;
    }

    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    public String getScmRevision() {
        return scmRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    public Integer getBuildEnvironmentId() {
        return buildEnvironmentId;
    }

    public void setBuildEnvironmentId(Integer buildEnvironmentId) {
        this.buildEnvironmentId = buildEnvironmentId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void putAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    public String getLiveLogsUri() {
        return liveLogsUri;
    }

    public void setLiveLogsUri(String liveLogsUri) {
        this.liveLogsUri = liveLogsUri;
    }

    public Integer getBuildConfigSetRecordId() {
        return buildConfigSetRecordId;
    }

    public String getBuildContentId() {
        return buildContentId;
    }

    public void setBuildContentId(String buildContentId) {
        this.buildContentId = buildContentId;
    }

    public Integer getProductMilestoneId() {
        return productMilestoneId;
    }

    public void setProductMilestoneId(Integer productMilestoneId) {
        this.productMilestoneId = productMilestoneId;
    }

    public UserRest getUser() {
        return user;
    }

    public BuildConfigurationAuditedRest getBuildConfigurationAudited() {
        return buildConfigurationAudited;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }
}
