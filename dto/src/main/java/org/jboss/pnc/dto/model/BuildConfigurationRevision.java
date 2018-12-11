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

import org.jboss.pnc.enums.BuildType;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class BuildConfigurationRevision extends BuildConfigurationRevisionRef {

    private final RepositoryConfigurationRef repositoryConfiguration;

    private final ProjectRef project;

    private final BuildEnvironment environment;

    private final Map<String, String> genericParameters ;

    @lombok.Builder(builderClassName = "Builder")
    public BuildConfigurationRevision(RepositoryConfigurationRef repositoryConfiguration, ProjectRef project, BuildEnvironment environment, Map<String, String> genericParameters, Integer id, Integer rev, String name, String description, String buildScript, String scmRevision, Instant creationTime, Instant lastModificationTime, BuildType buildType) {
        super(id, rev, name, description, buildScript, scmRevision, creationTime, lastModificationTime, buildType);
        this.repositoryConfiguration = repositoryConfiguration;
        this.project = project;
        this.environment = environment;
        this.genericParameters = genericParameters;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
