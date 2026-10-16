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
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Revision of an artifact created or used by build.
 *
 * @author Andrea Vibelli &lt;avibelli@redhat.com&gt;
 */

@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ArtifactRevisionRef.Builder.class)
public class ArtifactRevisionRef implements DTOEntity {

    /**
     * ID of the artifact.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * Revision id of the artifact.
     */
    protected final Integer rev;

    /**
     * The reason for the quality level setting (change) of this artifact.
     */
    protected final String qualityLevelReason;

    /**
     * The time when the quality level of this artifact was last modified.
     */
    protected final Instant modificationTime;

    /**
     * Quality level of the artifact.
     */
    protected final ArtifactQuality artifactQuality;

    /**
     * Category of the build denoting its support and usage
     */
    protected final BuildCategory buildCategory;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }

}
