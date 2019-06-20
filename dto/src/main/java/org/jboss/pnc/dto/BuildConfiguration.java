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
import java.util.Set;

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
public class BuildConfiguration extends BuildConfigurationRef {

    @PatchSupport({REPLACE})
    @RefHasId(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private final SCMRepository repository;

    @PatchSupport({REPLACE})
    @RefHasId(groups = WhenCreatingNew.class)
    private final ProjectRef project;

    @PatchSupport({REPLACE})
    @RefHasId(groups = {WhenCreatingNew.class, WhenUpdating.class})
    protected final Environment environment;

    @PatchSupport({ADD, REPLACE})
    private final Set<BuildConfigurationRef> dependencies;

    @PatchSupport({REPLACE})
    @RefHasId(groups = {WhenCreatingNew.class, WhenUpdating.class}, optional = true)
    private final ProductVersionRef productVersion;

    private final Set<GroupConfigurationRef> groupConfigs;

    @PatchSupport({ADD, REMOVE, REPLACE})
    private final Map<String, String> genericParameters;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private BuildConfiguration(SCMRepository repository, ProjectRef project, Environment environment, Set<BuildConfigurationRef> dependencies, ProductVersionRef productVersion, Set<GroupConfigurationRef> groupConfigs, Map<String, String> genericParameters, Integer id, String name, String description, String buildScript, String scmRevision, Instant creationTime, Instant modificationTime, boolean archived, BuildType buildType) {
        super(id, name, description, buildScript, scmRevision, creationTime, modificationTime, archived, buildType);
        this.repository = repository;
        this.project = project;
        this.environment = environment;
        this.dependencies = dependencies;
        this.productVersion = productVersion;
        this.groupConfigs = groupConfigs;
        this.genericParameters = genericParameters;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
