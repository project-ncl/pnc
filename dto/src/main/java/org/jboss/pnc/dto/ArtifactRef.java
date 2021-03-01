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

import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;
import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * An artifact created or used by build.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ArtifactRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactRef implements DTOEntity {

    /**
     * ID of the artifact.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final String id;

    /**
     * A unique identifier of the artifact in a repository. For example, for a maven artifact this is the GATVC
     * (groupId:artifactId:type:version[:classifier] The format of the identifier string is determined by the repository
     * type.
     */
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String identifier;

    /**
     * Quality level of the artifact.
     */
    @PatchSupport({ REPLACE })
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final ArtifactQuality artifactQuality;

    /**
     * Category of the build denoting its support and usage
     */
    @PatchSupport({ REPLACE })
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final BuildCategory buildCategory;

    /**
     * MD5 checksum of the artifact.
     */
    protected final String md5;

    /**
     * SHA-1 checksum of the artifact.
     */
    protected final String sha1;

    /**
     * SHA-256 checksum of the artifact.
     */
    protected final String sha256;

    /**
     * Filename of the artifact.
     */
    protected final String filename;

    /**
     * Path in the repository where the artifact file is available.
     */
    protected final String deployPath;

    /**
     * The time when this artifact was originally imported. When this artifact was built by PNC the value is null.
     */
    protected final Instant importDate;

    /**
     * The location from which this artifact was originally downloaded for import. When this artifact was built by PNC
     * the value is null.
     */
    protected final String originUrl;

    /**
     * Size of the artifact in bytes.
     */
    protected final Long size;

    /**
     * Internal url to the artifact using internal (cloud) network domain.
     */
    protected final String deployUrl;

    /**
     * Public url to the artifact using public network domain.
     */
    protected final String publicUrl;
    /**
     * The time when the artifact was created.
     */
    protected final Instant creationTime;

    /**
     * The time when the quality level of this artifact was last modified.
     */
    protected final Instant modificationTime;

    /**
     * The reason for the quality level setting (change) of this artifact.
     */
    @PatchSupport({ REPLACE })
    protected final String qualityLevelReason;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
