/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

    private final String id;
    private final BuildSetStatus buildStatus;
    private final String userId;
    private final String buildSetConfigurationId;
    private final String buildSetConfigurationName;
    private final Date buildSetStartTime;
    private final Date buildSetEndTime;
    private final String description;

    public BuildSetChangedPayload(String id,
            BuildSetStatus newStatus,
            String buildSetConfigurationId,
            String buildSetConfigurationName,
            Date buildSetStartTime,
            Date buildSetEndTime,
            String userId,
            String description) {
        this.id = id;
        this.buildStatus = newStatus;
        this.userId = userId;
        this.buildSetConfigurationId = buildSetConfigurationId;
        this.buildSetConfigurationName = buildSetConfigurationName;
        this.buildSetStartTime = buildSetStartTime;
        this.buildSetEndTime = buildSetEndTime;
        this.description = description;
    }

    public BuildSetStatus getBuildStatus() {
        return buildStatus;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public String getBuildSetConfigurationId() {
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

    public String getDescription() {
        return description;
    }
}
