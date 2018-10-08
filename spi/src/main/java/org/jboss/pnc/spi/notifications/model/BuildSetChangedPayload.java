/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.spi.notifications.model;

import org.jboss.pnc.spi.BuildSetStatus;

import java.util.Date;

public class BuildSetChangedPayload implements NotificationPayload {

    private final Integer id;
    private final BuildSetStatus buildStatus;
    private final Integer userId;
    private final Integer buildSetConfigurationId;
    private final String buildSetConfigurationName;
    private final Date buildSetStartTime;
    private final Date buildSetEndTime;

    public BuildSetChangedPayload(Integer id, BuildSetStatus newStatus, Integer buildSetConfigurationId,
            String buildSetConfigurationName, Date buildSetStartTime, Date buildSetEndTime, Integer userId) {
        this.id = id;
        this.buildStatus = newStatus;
        this.userId = userId;
        this.buildSetConfigurationId = buildSetConfigurationId;
        this.buildSetConfigurationName = buildSetConfigurationName;
        this.buildSetStartTime = buildSetStartTime;
        this.buildSetEndTime = buildSetEndTime;
    }

    public BuildSetStatus getBuildStatus() {
        return buildStatus;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }

    public Integer getBuildSetConfigurationId() {
        return buildSetConfigurationId;
    }

    public String getBuildSetConfigurationName() {
        return buildSetConfigurationName;
    }

    public Date getBuildSetStartTime() {
        return buildSetStartTime;
    }

    public Date getBuildSetEndTime() {
        return buildSetEndTime;
    }

}
