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

import java.time.Instant;

import org.jboss.pnc.enums.ArtifactQuality;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author Andrea Vibelli &lt;avibelli@redhat.com&gt;
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = ArtifactRevision.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactRevision extends ArtifactRevisionRef {

    private final User creationUser;

    private final User modificationUser;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private ArtifactRevision(
            String id,
            Integer rev,
            String reason,
            Instant creationTime,
            Instant modificationTime,
            ArtifactQuality artifactQuality,
            User creationUser,
            User modificationUser) {
        super(id, rev, reason, creationTime, modificationTime, artifactQuality);
        this.creationUser = creationUser;
        this.modificationUser = modificationUser;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
