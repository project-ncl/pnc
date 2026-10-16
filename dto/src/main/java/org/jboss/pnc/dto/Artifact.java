/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.jboss.pnc.dto.validation.groups.WhenImporting;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An artifact created or used by build.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@PatchSupport
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = Artifact.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artifact extends ArtifactRef {

    /**
     * Repository that stores this artifact.
     */
    @NotNull(groups = WhenImporting.class)
    private final TargetRepository targetRepository;

    /**
     * Build that produced the artifact.
     */
    private final Build build;

    /**
     * The user who created this artifact.
     */
    @Null(groups = WhenImporting.class)
    private final User creationUser;

    /**
     * The user who last modified the quality level of this artifact.
     */
    @Null(groups = WhenImporting.class)
    private final User modificationUser;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Artifact(
            TargetRepository targetRepository,
            Build build,
            String id,
            String identifier,
            String purl,
            ArtifactQuality artifactQuality,
            BuildCategory buildCategory,
            String md5,
            String sha1,
            String sha256,
            String filename,
            String deployPath,
            Instant importDate,
            String originUrl,
            Long size,
            String deployUrl,
            String publicUrl,
            User creationUser,
            User modificationUser,
            Instant creationTime,
            Instant modificationTime,
            String qualityLevelReason) {
        super(
                id,
                identifier,
                purl,
                artifactQuality,
                buildCategory,
                md5,
                sha1,
                sha256,
                filename,
                deployPath,
                importDate,
                originUrl,
                size,
                deployUrl,
                publicUrl,
                creationTime,
                modificationTime,
                qualityLevelReason);
        this.targetRepository = targetRepository;
        this.build = build;
        this.creationUser = creationUser;
        this.modificationUser = modificationUser;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
