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
package org.jboss.pnc.dto.model;

import org.jboss.pnc.dto.validation.constraints.RefHasId;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Value
public class BuildConfiguration extends BuildConfigurationRef {

    @RefHasId(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private final RepositoryConfigurationRef repositoryConfiguration;

    @RefHasId(groups = WhenCreatingNew.class)
    private final ProjectRef project;

    @RefHasId(groups = {WhenCreatingNew.class, WhenUpdating.class})
    protected final BuildEnvironmentRef environment;

    private final Set<Integer> dependencyIds;

    @RefHasId(groups = {WhenCreatingNew.class, WhenUpdating.class}, optional = true)
    private final ProductVersionRef productVersion;

    private final Set<Integer> groupBuildIds;

    private final Map<String, String> genericParameters;

    @lombok.Builder(builderClassName = "Builder")
    public BuildConfiguration(RepositoryConfigurationRef repositoryConfiguration, ProjectRef project, BuildEnvironmentRef environment, Set<Integer> dependencyIds, ProductVersionRef productVersion, Set<Integer> groupBuildIds, Map<String, String> genericParameters, Integer id, String name, String description, String buildScript, String scmRevision, Instant creationTime, Instant lastModificationTime, boolean archived, BuildType buildType) {
        super(id, name, description, buildScript, scmRevision, creationTime, lastModificationTime, archived, buildType);
        this.repositoryConfiguration = repositoryConfiguration;
        this.project = project;
        this.environment = environment;
        this.dependencyIds = dependencyIds;
        this.productVersion = productVersion;
        this.groupBuildIds = groupBuildIds;
        this.genericParameters = genericParameters;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
