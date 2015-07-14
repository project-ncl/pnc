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

import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BuildStatusChangedEventRest implements BuildStatusChangedEvent {

    private BuildStatus oldStatus;
    private BuildStatus newStatus;
    private Integer buildTaskId;
    private Integer userId;
    private Integer buildConfigurationId;

    public void setOldStatus(BuildStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(BuildStatus newStatus) {
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

    @Override
    public Integer getUserId() {
        return userId;
    }

    @Override
    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    @Override
    public BuildStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public BuildStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public Integer getBuildTaskId() {
        return buildTaskId;
    }
}