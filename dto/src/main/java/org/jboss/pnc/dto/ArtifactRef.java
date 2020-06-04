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
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ArtifactRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactRef implements DTOEntity {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final String id;

    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String identifier;

    @PatchSupport({ REPLACE })
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final ArtifactQuality artifactQuality;

    protected final String md5;

    protected final String sha1;

    protected final String sha256;

    protected final String filename;

    protected final String deployPath;

    protected final Instant importDate;

    protected final String originUrl;

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
     * The creation time of this artifact.
     */
    protected final Instant creationTime;

    /**
     * The time at which the Quality label of this artifact was last modified.
     */
    protected final Instant modificationTime;

    /**
     * The reason why the Quality label of the artifact was modified.
     */
    @PatchSupport({ REPLACE })
    protected final String reason;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
