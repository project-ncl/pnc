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
package org.jboss.pnc.rest.debug;

import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
public class BuildStatusChangedEventRest implements BuildCoordinationStatusChangedEvent {

    private BuildCoordinationStatus oldStatus;
    private BuildCoordinationStatus newStatus;
    private Integer buildTaskId;
    private Integer userId;
    private Integer buildConfigurationId;
    private Integer buildConfigurationRevision;
    private String buildConfigurationName;
    private Date buildStartTime;
    private Date buildEndTime;

    public void setOldStatus(BuildCoordinationStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(BuildCoordinationStatus newStatus) {
        this.newStatus = newStatus;
    }

    public void setBuildTaskId(Integer buildTaskId) {
        this.buildTaskId = buildTaskId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setBuildConfigurationId(Integer buildConfigurationId) {
        this.buildConfigurationId = buildConfigurationId;
    }

    public void setBuildConfigurationRevision(Integer buildConfigurationRevision) {
        this.buildConfigurationRevision = buildConfigurationRevision;
    }

    public void setBuildConfigurationName(String buildConfigurationName) {
        this.buildConfigurationName = buildConfigurationName;
    }

    public void setBuildStartTime(Date buildStartTime) {
        this.buildStartTime = buildStartTime;
    }

    public void setBuildEndTime(Date buildEndTime) {
        this.buildEndTime = buildEndTime;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }

    @Override
    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    @Override
    public Integer getBuildConfigurationRevision() {
        return buildConfigurationRevision;
    }

    @Override
    public BuildCoordinationStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public BuildCoordinationStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public Integer getBuildTaskId() {
        return buildTaskId;
    }

    @Override
    public String getBuildConfigurationName() {
        return buildConfigurationName;
    }

    @Override
    public Date getBuildStartTime() {
        return buildStartTime;
    }

    @Override
    public Date getBuildEndTime() {
        return buildEndTime;
    }
}