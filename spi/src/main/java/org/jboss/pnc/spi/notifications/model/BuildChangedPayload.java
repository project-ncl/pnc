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

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.dto.Build;

import java.util.Date;

//@JsonDeserialize(builder = BuildChangedPayload.BuildChangedPayloadBuilder.class)
@AllArgsConstructor
@Builder
//TODO 2.0 unify with BuildStatusChanged
public class BuildChangedPayload implements NotificationPayload {

    @Deprecated
    private final Integer id;
    @Deprecated
    private final BuildCoordinationStatus buildCoordinationStatus;

    @Deprecated
    private final Integer userId;
    @Deprecated
    private final Integer buildConfigurationId;
    @Deprecated
    private final String buildConfigurationName;
    @Deprecated
    private final Date buildStartTime;
    @Deprecated
    private final Date buildEndTime;

    private final Build build;

    @Deprecated
    public BuildChangedPayload(
            Integer id,
            BuildCoordinationStatus eventType,
            Integer buildConfigurationId,
            String buildConfigurationName,
            Date buildStartTime,
            Date buildEndTime,
            Integer userId) {
        this.id = id;
        this.buildCoordinationStatus = eventType;
        this.userId = userId;
        this.buildConfigurationId = buildConfigurationId;
        this.buildConfigurationName = buildConfigurationName;
        this.buildStartTime = buildStartTime;
        this.buildEndTime = buildEndTime;
        this.build = null;
    }

    public BuildChangedPayload(
            Build build,
            @Deprecated Date buildStartTime, //TODO remove in 2.0 it should be part of the build
            @Deprecated Date buildEndTime //TODO remove in 2.0 it should be part of the build
    ) {
        this.id = build.getId();
        this.buildCoordinationStatus = build.getStatus();
        this.userId = build.getUser().getId();
        this.buildConfigurationId = build.getBuildConfigurationAudited().getId();
        this.buildConfigurationName = build.getBuildConfigurationAudited().getName();
        this.buildStartTime = buildStartTime;
        this.buildEndTime = buildEndTime;
        this.build = build;
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

    public Build getBuild() {
        return build;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class BuildChangedPayloadBuilder {
    }

}
