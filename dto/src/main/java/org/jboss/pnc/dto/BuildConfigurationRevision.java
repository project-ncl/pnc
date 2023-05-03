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
import org.jboss.pnc.enums.BuildType;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class is used to maintain an audit trail of modifications made to a Build Config. Each instance represents a
 * specific revision of a build config.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = BuildConfigurationRevision.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfigurationRevision extends BuildConfigurationRevisionRef {

    /**
     * SCM repository where the build's sources are stored.
     */
    private final SCMRepository scmRepository;

    /**
     * The project which the build config is part of.
     */
    private final ProjectRef project;

    /**
     * Build environment that the build will be run in.
     */
    private final Environment environment;

    /**
     * Map of build parameters. These parameters can influence various parts of the build like alignment phase or
     * builder pod memory available.
     */
    private final Map<String, String> parameters;

    /**
     * User who created the build config.
     */
    private final User creationUser;

    /**
     * User who last modified the build config.
     */
    private final User modificationUser;

    private final Set<AlignmentStrategy> alignmentStrategies;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private BuildConfigurationRevision(
            SCMRepository scmRepository,
            ProjectRef project,
            Environment environment,
            Map<String, String> parameters,
            String id,
            Integer rev,
            String name,
            String buildScript,
            String scmRevision,
            Instant creationTime,
            Instant modificationTime,
            BuildType buildType,
            User creationUser,
            User modificationUser,
            String defaultAlignmentParams,
            boolean brewPullActive,
            Set<AlignmentStrategy> alignmentStrategies) {
        super(
                id,
                rev,
                name,
                buildScript,
                scmRevision,
                creationTime,
                modificationTime,
                buildType,
                defaultAlignmentParams,
                brewPullActive);
        this.scmRepository = scmRepository;
        this.project = project;
        this.environment = environment;
        this.parameters = parameters;
        this.creationUser = creationUser;
        this.modificationUser = modificationUser;
        this.alignmentStrategies = alignmentStrategies;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
