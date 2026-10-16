/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.SystemImageType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Build environment that builds are run in.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonDeserialize(builder = Environment.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Environment implements DTOEntity {

    /**
     * ID of the build environment.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private final String id;

    /**
     * Environment name.
     */
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private final String name;

    /**
     * Environment description.
     */
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private final String description;

    /**
     * The URL of the repository which contains the build system image.
     */
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private final String systemImageRepositoryUrl;

    /**
     * A unique identifier representing the system image, for example a Docker container ID or a checksum of a VM image.
     */
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private final String systemImageId;

    /**
     * Map of environment attributes.
     */
    private final Map<String, String> attributes;

    /**
     * Type of the build environment system image which will be used for the build.
     */
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private final SystemImageType systemImageType;

    /**
     * Is the environment deprecated and no longer advisable to be used by new builds?
     */
    private final boolean deprecated;

    /**
     * Is the environment to be hidden and not available anymore to user?
     */
    private final boolean hidden;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
