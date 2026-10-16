/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.enums.BuildStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Build of a group config.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = GroupBuild.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupBuild extends GroupBuildRef {

    /**
     * Group config that this is a build of.
     */
    private final GroupConfigurationRef groupConfig;

    /**
     * User who started this group build.
     */
    private final User user;

    /**
     * Product version this group build is part of.
     */
    private final ProductVersionRef productVersion;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private GroupBuild(
            GroupConfigurationRef groupConfig,
            User user,
            ProductVersionRef productVersion,
            String id,
            Instant startTime,
            Instant endTime,
            BuildStatus status,
            Boolean temporaryBuild,
            AlignmentPreference alignmentPreference,
            RebuildMode rebuildMode) {
        super(id, startTime, endTime, status, temporaryBuild, alignmentPreference, rebuildMode);
        this.groupConfig = groupConfig;
        this.user = user;
        this.productVersion = productVersion;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
