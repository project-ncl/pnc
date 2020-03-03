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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jboss.pnc.enums.BuildType;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = BuildConfigurationRevision.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfigurationRevision extends BuildConfigurationRevisionRef {

    private final SCMRepository scmRepository;

    private final ProjectRef project;

    private final Environment environment;

    private final Map<String, String> parameters;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private BuildConfigurationRevision(
            SCMRepository scmRepository,
            ProjectRef project,
            Environment environment,
            Map<String, String> parameters,
            String id,
            Integer rev,
            String name,
            String description,
            String buildScript,
            String scmRevision,
            Instant creationTime,
            Instant modificationTime,
            BuildType buildType) {
        super(id, rev, name, description, buildScript, scmRevision, creationTime, modificationTime, buildType);
        this.scmRepository = scmRepository;
        this.project = project;
        this.environment = environment;
        this.parameters = parameters;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
