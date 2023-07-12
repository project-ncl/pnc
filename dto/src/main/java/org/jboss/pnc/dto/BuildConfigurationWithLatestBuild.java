/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.ToString;
import org.jboss.pnc.common.validator.NoHtml;
import org.jboss.pnc.enums.BuildType;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Build configuration with information about last executed build with it
 */
@Data
@ToString(callSuper = true)
@JsonDeserialize(builder = BuildConfigurationWithLatestBuild.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfigurationWithLatestBuild extends BuildConfiguration {

    /**
     * The latest build started with this build config.
     */
    private final BuildRef latestBuild;

    /**
     * Username of the user who ran the last build.
     */
    @NoHtml
    private final String latestBuildUsername;

    @lombok.Builder(builderClassName = "Builder", builderMethodName = "builderWithLatestBuild")
    private BuildConfigurationWithLatestBuild(
            SCMRepository scmRepository,
            ProjectRef project,
            Environment environment,
            Map<String, BuildConfigurationRef> dependencies,
            ProductVersionRef productVersion,
            Map<String, GroupConfigurationRef> groupConfigs,
            Map<String, String> parameters,
            String id,
            String name,
            String description,
            String buildScript,
            String scmRevision,
            Instant creationTime,
            Instant modificationTime,
            BuildType buildType,
            User creationUser,
            User modificationUser,
            String defaultAlignmentParams,
            Boolean brewPullActive,
            Set<AlignmentStrategy> alignmentStrategies,
            BuildRef latestBuild,
            String latestBuildUsername) {
        super(
                scmRepository,
                project,
                environment,
                dependencies,
                productVersion,
                groupConfigs,
                parameters,
                id,
                name,
                description,
                buildScript,
                scmRevision,
                creationTime,
                modificationTime,
                buildType,
                creationUser,
                modificationUser,
                defaultAlignmentParams,
                brewPullActive,
                alignmentStrategies);
        this.latestBuild = latestBuild;
        this.latestBuildUsername = latestBuildUsername;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {

        public BuildConfigurationWithLatestBuild.Builder buildConfig(BuildConfiguration buildConfiguration) {
            this.id = buildConfiguration.id;
            this.name = buildConfiguration.name;
            this.description = buildConfiguration.description;
            this.buildScript = buildConfiguration.buildScript;
            this.scmRevision = buildConfiguration.scmRevision;
            this.creationTime = buildConfiguration.creationTime;
            this.modificationTime = buildConfiguration.modificationTime;
            this.buildType = buildConfiguration.buildType;
            this.defaultAlignmentParams = buildConfiguration.defaultAlignmentParams;
            this.brewPullActive = buildConfiguration.brewPullActive;
            this.scmRepository = buildConfiguration.scmRepository;
            this.project = buildConfiguration.project;
            this.environment = buildConfiguration.environment;
            this.dependencies = buildConfiguration.dependencies;
            this.productVersion = buildConfiguration.productVersion;
            this.groupConfigs = buildConfiguration.groupConfigs;
            this.parameters = buildConfiguration.parameters;
            this.creationUser = buildConfiguration.creationUser;
            this.modificationUser = buildConfiguration.modificationUser;
            this.alignmentStrategies = buildConfiguration.alignmentStrategies;
            return this;
        }
    }
}
