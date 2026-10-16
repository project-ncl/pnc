/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Revision of a an artifact created or used by build.
 *
 * @author Andrea Vibelli &lt;avibelli@redhat.com&gt;
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = ArtifactRevision.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactRevision extends ArtifactRevisionRef {

    /**
     * The user who last modified the quality level of this artifact.
     */
    private final User modificationUser;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private ArtifactRevision(
            String id,
            Integer rev,
            String qualityLevelReason,
            Instant modificationTime,
            ArtifactQuality artifactQuality,
            BuildCategory buildCategory,
            User modificationUser) {
        super(id, rev, qualityLevelReason, modificationTime, artifactQuality, buildCategory);
        this.modificationUser = modificationUser;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
