/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jboss.pnc.dto.validation.constraints.RefHasId;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.processor.annotation.PatchSupport;

import java.time.Instant;
import java.util.Map;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REMOVE;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@PatchSupport
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = BuildConfiguration.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfiguration extends BuildConfigurationRef {

    @PatchSupport({ REPLACE })
    @RefHasId(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private final SCMRepository scmRepository;

    @PatchSupport({ REPLACE })
    @RefHasId(groups = WhenCreatingNew.class)
    private final ProjectRef project;

    @PatchSupport({ REPLACE })
    @RefHasId(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final Environment environment;

    @PatchSupport({ ADD, REPLACE })
    private final Map<String, BuildConfigurationRef> dependencies;

    @PatchSupport({ REPLACE })
    @RefHasId(groups = { WhenCreatingNew.class, WhenUpdating.class }, optional = true)
    private final ProductVersionRef productVersion;

    private final Map<String, GroupConfigurationRef> groupConfigs;

    @PatchSupport({ ADD, REMOVE, REPLACE })
    private final Map<String, String> parameters;

    private final User creationUser;

    private final User modificationUser;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private BuildConfiguration(
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
            User modificationUser) {
        super(id, name, description, buildScript, scmRevision, creationTime, modificationTime, buildType);
        this.scmRepository = scmRepository;
        this.project = project;
        this.environment = environment;
        this.dependencies = dependencies;
        this.productVersion = productVersion;
        this.groupConfigs = groupConfigs;
        this.parameters = parameters;
        this.creationUser = creationUser;
        this.modificationUser = modificationUser;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
