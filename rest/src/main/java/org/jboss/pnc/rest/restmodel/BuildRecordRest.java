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

import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildRecord")
public class BuildRecordRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private Date submitTime;

    private Date startTime;

    private Date endTime;

    private BuildStatus status;

    private Integer buildConfigurationId;

    private Integer buildConfigurationRev;

    private Integer userId;

    private String buildDriverId;

    private Integer systemImageId;

    private Integer externalArchiveId;

    private String liveLogsUri;

    private Integer buildConfigSetRecordId;

    private String buildContentId;

    public BuildRecordRest() {
    }

    public BuildRecordRest(BuildRecord buildRecord) {
        this.id = buildRecord.getId();
        this.submitTime = buildRecord.getSubmitTime();
        this.startTime = buildRecord.getStartTime();
        this.endTime = buildRecord.getEndTime();
        this.externalArchiveId = buildRecord.getExternalArchiveId();
        performIfNotNull(buildRecord.getBuildConfigurationAudited(), () -> buildConfigurationId = buildRecord
                .getBuildConfigurationAudited().getId().getId());
        performIfNotNull(buildRecord.getBuildConfigurationAudited(), () -> buildConfigurationRev = buildRecord
                .getBuildConfigurationAudited().getRev());
        performIfNotNull(buildRecord.getUser(), () -> userId = buildRecord.getUser().getId());
        performIfNotNull(buildRecord.getSystemImage(), () -> systemImageId = buildRecord.getSystemImage().getId());
        this.status = buildRecord.getStatus();
        this.buildDriverId = buildRecord.getBuildDriverId();
        if(buildRecord.getBuildConfigSetRecord() != null)
            this.buildConfigSetRecordId = buildRecord.getBuildConfigSetRecord().getId();

        this.buildContentId = buildRecord.getBuildContentId();
    }

    public BuildRecordRest(BuildTask buildTask) {
        this.id = buildTask.getId();
        this.submitTime = buildTask.getSubmitTime();
        this.startTime = buildTask.getStartTime();
        this.endTime = buildTask.getEndTime();
        if (buildTask.getBuildConfigurationAudited() != null) {
            this.buildConfigurationId = buildTask.getBuildConfigurationAudited().getId().getId();
            this.buildConfigurationRev = buildTask.getBuildConfigurationAudited().getRev();
        }
        this.status = BuildStatus.BUILDING;
        buildTask.getLogsWebSocketLink().ifPresent(logsUri -> this.liveLogsUri = logsUri.toString());
        performIfNotNull(buildTask.getBuildSetTask(), () -> this.buildConfigSetRecordId = buildTask.getBuildSetTask().getId());
        if(buildTask.getUser() != null)
            this.userId = buildTask.getUser().getId();

        this.buildContentId = buildTask.getBuildContentId();
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

    public BuildStatus getStatus() {
        return status;
    }

    public void setStatus(BuildStatus status) {
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getBuildDriverId() {
        return buildDriverId;
    }

    public void setBuildDriverId(String buildDriverId) {
        this.buildDriverId = buildDriverId;
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
}
