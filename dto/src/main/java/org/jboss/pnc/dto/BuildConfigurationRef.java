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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.time.Instant;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = BuildConfigurationRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfigurationRef implements DTOEntity {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final String id;

    @PatchSupport({ REPLACE })
    @NotNull(groups = WhenCreatingNew.class)
    @Pattern(
            regexp = "^[a-zA-Z0-9_.][a-zA-Z0-9_.-]*(?<!\\.git)$",
            groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String name;

    @PatchSupport({ REPLACE })
    protected final String description;

    @PatchSupport({ REPLACE })
    protected final String buildScript;

    @PatchSupport({ REPLACE })
    protected final String scmRevision;

    protected final Instant creationTime;

    protected final Instant modificationTime;

    @PatchSupport({ REPLACE })
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final BuildType buildType;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
