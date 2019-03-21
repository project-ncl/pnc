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
package org.jboss.pnc.dto;

import org.jboss.pnc.enums.BuildType;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = BuildConfigurationRevision.Builder.class)
public class BuildConfigurationRevision extends BuildConfigurationRevisionRef {

    private final SCMRepository repository;

    private final ProjectRef project;

    private final Environment environment;

    private final Map<String, String> genericParameters ;

    @lombok.Builder(builderClassName = "Builder")
    private BuildConfigurationRevision(SCMRepository repository, ProjectRef project, Environment environment, Map<String, String> genericParameters, Integer id, Integer rev, String name, String description, String buildScript, String scmRevision, Date creationTime, Date modificationTime, BuildType buildType) {
        super(id, rev, name, description, buildScript, scmRevision, creationTime, modificationTime, buildType);
        this.repository = repository;
        this.project = project;
        this.environment = environment;
        this.genericParameters = genericParameters;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
