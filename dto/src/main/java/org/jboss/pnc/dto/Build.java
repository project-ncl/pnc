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
package org.jboss.pnc.dto;

import org.jboss.pnc.enums.BuildCoordinationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class Build extends BuildRef {

    private final ProjectRef project;

    private final RepositoryConfiguration repository;

    private final BuildEnvironment environment;

    private final Map<String, String> attributes;

    private final User user;

    private final BuildConfigurationRevisionRef buildConfigurationAudited;

    private final List<Integer> dependentBuildIds;

    private final List<Integer> dependencyBuildIds;

    @lombok.Builder(builderClassName = "Builder")
    public Build(ProjectRef project, RepositoryConfiguration repository, BuildEnvironment environment, Map<String, String> attributes, User user, BuildConfigurationRevisionRef buildConfigurationAudited, List<Integer> dependentBuildIds, List<Integer> dependencyBuildIds, Integer id, Instant submitTime, Instant startTime, Instant endTime, BuildCoordinationStatus status, String buildContentId, Boolean temporaryBuild) {
        super(id, submitTime, startTime, endTime, status, buildContentId, temporaryBuild);
        this.project = project;
        this.repository = repository;
        this.environment = environment;
        this.attributes = attributes;
        this.user = user;
        this.buildConfigurationAudited = buildConfigurationAudited;
        this.dependentBuildIds = dependentBuildIds;
        this.dependencyBuildIds = dependencyBuildIds;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
