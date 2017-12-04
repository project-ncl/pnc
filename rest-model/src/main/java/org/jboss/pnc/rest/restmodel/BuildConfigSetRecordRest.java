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

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildRecord")
public class BuildConfigSetRecordRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private Integer buildConfigurationSetId;

    private String buildConfigurationSetName;

    private Date startTime;

    private Date endTime;

    private BuildStatus status;

    private Integer userId;

    private String username;

    private Integer productVersionId;

    private Set<Integer> buildRecordIds;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private Boolean temporaryBuild;

    public BuildConfigSetRecordRest() {
    }

    public BuildConfigSetRecordRest(BuildConfigSetRecord buildConfigSetRecord) {
        requireNonNull(buildConfigSetRecord);
        this.id = buildConfigSetRecord.getId();
        this.startTime = buildConfigSetRecord.getStartTime();
        this.endTime = buildConfigSetRecord.getEndTime();
        performIfNotNull(buildConfigSetRecord.getBuildConfigurationSet(),
                () -> buildConfigurationSetId = buildConfigSetRecord.getBuildConfigurationSet().getId());
        performIfNotNull(buildConfigSetRecord.getBuildConfigurationSet(),
                () -> buildConfigurationSetName = buildConfigSetRecord.getBuildConfigurationSet().getName());
        performIfNotNull(buildConfigSetRecord.getUser(), () -> userId = buildConfigSetRecord.getUser().getId());
        performIfNotNull(buildConfigSetRecord.getUser(), () -> username = buildConfigSetRecord.getUser().getUsername());
        performIfNotNull(buildConfigSetRecord.getProductVersion(),
                () -> productVersionId = buildConfigSetRecord.getProductVersion().getId());
        this.status = buildConfigSetRecord.getStatus();
        requireNonNull(buildConfigSetRecord.getBuildRecords());
        this.buildRecordIds = buildConfigSetRecord.getBuildRecords().stream().map(BuildRecord::getId)
                .collect(Collectors.toSet());
        this.temporaryBuild = buildConfigSetRecord.isTemporaryBuild();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
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

    public Integer getBuildConfigurationSetId() {
        return buildConfigurationSetId;
    }

    public void setBuildConfigurationSetId(Integer buildConfigurationSetId) {
        this.buildConfigurationSetId = buildConfigurationSetId;
    }

    public String getBuildConfigurationSetName() {
        return buildConfigurationSetName;
    }

    public void setBuildConfigurationSetName(String buildConfigurationSetName) {
        this.buildConfigurationSetName = buildConfigurationSetName;
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

    public Integer getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(Integer productVersionId) {
        this.productVersionId = productVersionId;
    }

    public Set<Integer> getBuildRecordIds() {
        return buildRecordIds;
    }
}
