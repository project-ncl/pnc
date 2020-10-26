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
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * This class is used to maintain an audit trail of modifications made to a Build Config. Each instance represents a
 * specific revision of a build config.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = BuildConfigurationRevisionRef.Builder.class)
public class BuildConfigurationRevisionRef implements DTOEntity {

    /**
     * ID of the build config.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final String id;

    /**
     * Revision ID of the build config.
     */
    protected final Integer rev;

    /**
     * Build config name.
     */
    protected final String name;

    /**
     * Shell script to be executed.
     */
    protected final String buildScript;

    /**
     * SCM revision to build.
     */
    protected final String scmRevision;

    /**
     * The time when the build config was created.
     */
    protected final Instant creationTime;

    /**
     * The time when the build config was last modified.
     */
    protected final Instant modificationTime;

    /**
     * Build type of the build config. It defines pre-build operations and sets the proper repository.
     */
    protected final BuildType buildType;

    /**
     * The default alignment parameters for this build config type.
     */
    protected final String defaultAlignmentParams;

    /**
     * Whether brew pull active is on or off
     */
    protected final boolean brewPullActive;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
