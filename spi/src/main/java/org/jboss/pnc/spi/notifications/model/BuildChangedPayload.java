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
package org.jboss.pnc.spi.notifications.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.jboss.pnc.spi.BuildCoordinationStatus;

import java.util.Date;

@JsonDeserialize(builder = BuildChangedPayload.BuildChangedPayloadBuilder.class)
@AllArgsConstructor
@Builder
public class BuildChangedPayload implements NotificationPayload {

    private final Integer id;
    private final BuildCoordinationStatus buildCoordinationStatus;
    private final Integer userId;
    private final Integer buildConfigurationId;
    private final String buildConfigurationName;
    private final Date buildStartTime;
    private final Date buildEndTime;

    public BuildChangedPayload(Integer id, BuildCoordinationStatus eventType, Integer buildConfigurationId,
            String buildConfigurationName, Date buildStartTime, Date buildEndTime, Integer userId) {
        this.id = id;
        this.buildCoordinationStatus = eventType;
        this.userId = userId;
        this.buildConfigurationId = buildConfigurationId;
        this.buildConfigurationName = buildConfigurationName;
        this.buildStartTime = buildStartTime;
        this.buildEndTime = buildEndTime;
    }

    public BuildCoordinationStatus getBuildCoordinationStatus() {
        return buildCoordinationStatus;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }

    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    public String getBuildConfigurationName() {
        return buildConfigurationName;
    }

    public Date getBuildStartTime() {
        return buildStartTime;
    }

    public Date getBuildEndTime() {
        return buildEndTime;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class BuildChangedPayloadBuilder {
    }

}
