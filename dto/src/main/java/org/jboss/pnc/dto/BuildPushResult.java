/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildPushStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 * Result of a build push operation.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = BuildPushResult.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildPushResult extends BuildPushResultRef {

    /**
     * Product milestone close result this build push is a part of.
     */
    private final ProductMilestoneCloseResultRef productMilestoneCloseResult;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    public BuildPushResult(
            @NotNull(groups = WhenUpdating.class) @Null(groups = WhenCreatingNew.class) String id,
            @NotNull String buildId,
            @NotNull BuildPushStatus status,
            Integer brewBuildId,
            String brewBuildUrl,
            String logContext,
            String message,
            ProductMilestoneCloseResultRef productMilestoneCloseResult,
            String userInitiator) {
        super(id, buildId, status, brewBuildId, brewBuildUrl, logContext, message, userInitiator);
        this.productMilestoneCloseResult = productMilestoneCloseResult;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
