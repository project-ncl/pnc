/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildPushStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Result of a build push operation.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = BuildPushResultRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildPushResultRef implements DTOEntity {

    /**
     * ID of the operation result.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * ID of the build pushed.
     */
    @NotNull
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String buildId;

    /**
     * Status of the build push.
     */
    @NotNull
    protected final BuildPushStatus status;

    /**
     * Build id assigned by brew.
     */
    protected final Integer brewBuildId;

    /**
     * Link to Brew.
     */
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String brewBuildUrl;

    /**
     * Identificator of log context. Logs related to this operation will have this log context id set.
     */
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String logContext;

    /**
     * Used by group push, to describe rejected and error push request (should be used only for non-stored results).
     */
    protected final String message;

    protected final String userInitiator;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
