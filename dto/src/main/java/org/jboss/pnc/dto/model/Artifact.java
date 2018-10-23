/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.dto.model;

import org.jboss.pnc.enums.ArtifactQuality;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class Artifact extends ArtifactRef {

    private final TargetRepositoryRef targetRepository;

    private final Set<Integer> buildIds;

    private final Set<Integer> dependantBuildIds;

    @lombok.Builder(builderClassName = "Builder")
    public Artifact(TargetRepositoryRef targetRepository, Set<Integer> buildIds, Set<Integer> dependantBuildIds, Integer id, String identifier, ArtifactQuality artifactQuality, String md5, String sha1, String sha256, String filename, String deployPath, Instant importDate, String originUrl, Long size, String deployUrl, String publicUrl) {
        super(id, identifier, artifactQuality, md5, sha1, sha256, filename, deployPath, importDate, originUrl, size, deployUrl, publicUrl);
        this.targetRepository = targetRepository;
        this.buildIds = buildIds;
        this.dependantBuildIds = dependantBuildIds;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
