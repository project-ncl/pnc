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
package org.jboss.pnc.dto;

import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.ArtifactQuality;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
public class ArtifactRef implements DTOEntity {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final Integer id;

    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    protected final String identifier;

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

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
