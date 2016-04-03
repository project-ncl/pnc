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
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildRecord")
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

    private Integer userId;

    private String username;

    private String scmRepoURL;

    private String scmRevision;

    private Integer systemImageId;

    private Integer externalArchiveId;

    private String liveLogsUri;

    private Integer buildConfigSetRecordId;

    private String buildContentId;

    /**
     * The IDs of all the build record sets to which this build record belongs
     */
    private Set<Integer> buildRecordSetIds;

    /**
     * The IDs of the build record sets which represent the builds performed for a milestone to which this build record belongs
     */
    private Set<Integer> performedMilestoneBuildRecordSetIds;

    /**
     * Required in order to use rsql on user
     */
    private UserRest user;

    /**
     * Required in order to use rsql on buildConfiguration
     */
    private BuildConfigurationAuditedRest buildConfigurationAudited;

    public BuildRecordRest() {
    }

    public BuildRecordRest(BuildRecord buildRecord) {
        this.id = buildRecord.getId();
        this.submitTime = buildRecord.getSubmitTime();
        this.startTime = buildRecord.getStartTime();
        this.endTime = buildRecord.getEndTime();
        this.scmRepoURL = buildRecord.getScmRepoURL();
        this.scmRevision = buildRecord.getScmRevision();
        this.externalArchiveId = buildRecord.getExternalArchiveId();
        performIfNotNull(buildRecord.getBuildConfigurationAudited(),
                () -> buildConfigurationId = buildRecord.getBuildConfigurationAudited().getId().getId());
        performIfNotNull(buildRecord.getBuildConfigurationAudited(),
                () -> buildConfigurationName = buildRecord.getBuildConfigurationAudited().getName());
        performIfNotNull(buildRecord.getBuildConfigurationAudited(),
                () -> buildConfigurationRev = buildRecord.getBuildConfigurationAudited().getRev());
        performIfNotNull(buildRecord.getUser(), () -> userId = buildRecord.getUser().getId());
        performIfNotNull(buildRecord.getUser(), () -> username = buildRecord.getUser().getUsername());
        performIfNotNull(buildRecord.getSystemImage(), () -> systemImageId = buildRecord.getSystemImage().getId());
        this.status = BuildCoordinationStatus.fromBuildStatus(buildRecord.getStatus());
        if (buildRecord.getBuildConfigSetRecord() != null)
            this.buildConfigSetRecordId = buildRecord.getBuildConfigSetRecord().getId();

        this.buildContentId = buildRecord.getBuildContentId();
        this.buildRecordSetIds = nullableStreamOf(buildRecord.getBuildRecordSets())
                .map(buildRecordSet -> buildRecordSet.getId()).collect(Collectors.toSet());
        this.performedMilestoneBuildRecordSetIds = nullableStreamOf(buildRecord.getBuildRecordSets())
                .filter(buildRecordSet -> buildRecordSet.getPerformedInProductMilestone() != null)
                .map(buildRecordSet -> buildRecordSet.getId()).collect(Collectors.toSet());

        performIfNotNull(buildRecord.getUser(), () -> user = new UserRest(buildRecord.getUser()));
        performIfNotNull(buildRecord.getBuildConfigurationAudited(),
                () -> buildConfigurationAudited = new BuildConfigurationAuditedRest(
                        buildRecord.getBuildConfigurationAudited()));
    }

    public BuildRecordRest(BuildExecutionSession buildExecutionSession, Date submitTime, UserRest user,
            BuildConfigurationAuditedRest buildConfigurationAudited) {
        this.id = buildExecutionSession.getId();
        this.submitTime = submitTime;
        this.startTime = buildExecutionSession.getStartTime();
        this.endTime = buildExecutionSession.getEndTime();
        BuildExecutionConfiguration buildExecutionConfig = buildExecutionSession.getBuildExecutionConfiguration();

        // FIXME Why masking i.e. BUILD_WAITING status with BUILDING ?
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
    }

    public BuildRecordRest(Integer id, BuildCoordinationStatus buildCoordinationStatus, Date submitTime, Date startTime,
            Date endTime, UserRest user, BuildConfigurationAuditedRest buildConfigurationAudited) {
        this.id = id;
        this.submitTime = submitTime;
        this.startTime = startTime;
        this.endTime = endTime;

        this.status = buildCoordinationStatus;

        this.userId = user.getId();
        this.username = user.getUsername();

        this.user = user;
        this.buildConfigurationAudited = buildConfigurationAudited;
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

    public Integer getSystemImageId() {
        return systemImageId;
    }

    public void setSystemImageId(Integer systemImageId) {
        this.systemImageId = systemImageId;
    }

    public Integer getExternalArchiveId() {
        return externalArchiveId;
    }

    public void setExternalArchiveId(Integer externalArchiveId) {
        this.externalArchiveId = externalArchiveId;
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

    public Set<Integer> getBuildRecordSetIds() {
        return buildRecordSetIds;
    }

    public Set<Integer> getPerformedMilestoneBuildRecordSetIds() {
        return performedMilestoneBuildRecordSetIds;
    }

    public UserRest getUser() {
        return user;
    }

    public BuildConfigurationAuditedRest getBuildConfigurationAudited() {
        return buildConfigurationAudited;
    }
}
