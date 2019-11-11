/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jboss.pnc.enums.ArtifactQuality;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = Artifact.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artifact extends ArtifactRef {

    private final TargetRepository targetRepository;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Artifact(TargetRepository targetRepository, String id, String identifier, ArtifactQuality artifactQuality, String md5, String sha1, String sha256, String filename, String deployPath, Instant importDate, String originUrl, Long size, String deployUrl, String publicUrl) {
        super(id, identifier, artifactQuality, md5, sha1, sha256, filename, deployPath, importDate, originUrl, size, deployUrl, publicUrl);
        this.targetRepository = targetRepository;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
