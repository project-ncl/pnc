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
package org.jboss.pnc.rest.model;

import org.jboss.pnc.rest.model.enums.BuildType;
import org.jboss.pnc.rest.model.response.Page;
import org.jboss.pnc.rest.model.response.Singleton;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuildConfigurationRest implements RestEntity {

    public static abstract class BuildConfigurationPage extends Page<BuildConfigurationRest> {
    }

    public static class BuildConfigurationSingleton extends Singleton<BuildConfigurationRest> {
    }

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private final Integer id;

    @NotNull(groups = WhenCreatingNew.class)
    @Pattern(regexp = "^[a-zA-Z0-9_.][a-zA-Z0-9_.-]*(?<!\\.git)$", groups = {WhenCreatingNew.class, WhenUpdating.class})
    private final String name;

    private final String description;

    private final String buildScript;

    @NotNull
    private final RepositoryConfigurationRef repositoryConfiguration;

    private final String scmRevision;

    private final Instant creationTime;

    private final Instant lastModificationTime;

    private final boolean archived;

    @NotNull(groups = WhenCreatingNew.class)
    private final ProjectRef project;

    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private final BuildType buildType;

    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private final BuildEnvironmentRef environment;

    private final Set<Integer> dependencyIds;

    private final Integer productVersionId;

    private final Set<Integer> buildConfigurationSetIds;

    private final Map<String, String> genericParameters;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class BuildConfigurationRestBuilder {
    }
}
