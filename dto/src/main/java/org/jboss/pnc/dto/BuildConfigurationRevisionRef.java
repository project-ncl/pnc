/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * Revision ID of the build config.
     */
    protected final Integer rev;

    /**
     * Build config name.
     */
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String name;

    /**
     * Shell script to be executed.
     */
    protected final String buildScript;

    /**
     * SCM revision to build.
     */
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
