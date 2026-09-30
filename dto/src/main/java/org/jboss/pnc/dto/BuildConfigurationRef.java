/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

import java.time.Instant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * A build config cointains the information needed to execute a build of a project, i.e. link to the sources, the build
 * script, the build system image needed to run.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = BuildConfigurationRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfigurationRef implements DTOEntity {

    /**
     * ID of the build config.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * Build config name. It must be unique and can be made of alphanumeric characters with [_.-].
     */
    @PatchSupport({ REPLACE })
    @NotNull(groups = WhenCreatingNew.class)
    @Pattern(
            regexp = "^[a-zA-Z0-9_.][a-zA-Z0-9_.-]*(?<!\\.git)$",
            groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String name;

    /**
     * Build config description.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String description;

    /**
     * Shell script to be executed.
     */
    @PatchSupport({ REPLACE })
    protected final String buildScript;

    /**
     * SCM revision to build.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
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
    @PatchSupport({ REPLACE })
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final BuildType buildType;

    /**
     * The default alignment parameters for this build config type.
     */
    protected final String defaultAlignmentParams;

    /**
     * Indicates whether the Brew Bridge Pull feature is active or not. It defaults to false.
     */
    @PatchSupport({ REPLACE })
    protected final Boolean brewPullActive;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
