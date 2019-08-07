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
package org.jboss.pnc.spi.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.spi.BuildCoordinationStatus;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Deprecated
@Data
@Setter
@Getter
@AllArgsConstructor
@JsonDeserialize(builder = Build.Builder.class)
public class Build extends BuildRef {

    private ProjectRef project;

    private RepositoryConfiguration repository;

    private BuildEnvironment buildEnvironmentId;

    private Map<String, String> attributes;

    private User user;

    private BuildConfigurationRevisionRef buildConfigurationAudited;

    private List<Integer> dependentBuildIds;

    private List<Integer> dependencyBuildIds;

    @lombok.Builder(builderClassName = "Builder")
    public Build(ProjectRef project, RepositoryConfiguration repository, BuildEnvironment buildEnvironmentId, Map<String, String> attributes, User user, BuildConfigurationRevisionRef buildConfigurationAudited, List<Integer> dependentBuildIds, List<Integer> dependencyBuildIds, Integer id, BuildCoordinationStatus status, String buildContentId, Boolean temporaryBuild) {
        super(id, status, buildContentId, temporaryBuild);
        this.project = project;
        this.repository = repository;
        this.buildEnvironmentId = buildEnvironmentId;
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
