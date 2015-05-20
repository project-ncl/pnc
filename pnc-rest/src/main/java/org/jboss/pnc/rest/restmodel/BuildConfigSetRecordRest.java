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
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildRecord")
public class BuildConfigSetRecordRest {

    private Integer id;

    private Integer buildConfigurationSetId;

    private Date startTime;

    private Date endTime;

    private BuildStatus status;

    private Integer userId;

    private Integer productVersionId;

    public BuildConfigSetRecordRest() {
    }

    public BuildConfigSetRecordRest(BuildConfigSetRecord buildConfigSetRecord) {
        this.id = buildConfigSetRecord.getId();
        this.startTime = buildConfigSetRecord.getStartTime();
        this.endTime = buildConfigSetRecord.getEndTime();
        performIfNotNull(buildConfigSetRecord.getBuildConfigurationSet() != null, () -> buildConfigurationSetId = buildConfigSetRecord
                .getBuildConfigurationSet().getId());
        performIfNotNull(buildConfigSetRecord.getUser() != null, () -> userId = buildConfigSetRecord.getUser().getId());
        performIfNotNull(buildConfigSetRecord.getProductVersion() != null, () -> productVersionId = buildConfigSetRecord.getProductVersion().getId());
        this.status = buildConfigSetRecord.getStatus();
    }

    public Integer getId() {
        return id;
    }

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductVersionId() {
        return productVersionId;
    }

    public void setSystemImageId(Integer productVersionId) {
        this.productVersionId = productVersionId;
    }

}
