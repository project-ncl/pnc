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
package org.jboss.pnc.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Default parameters for build configuration for a specific build type
 *
 * @author dbrazdil
 */
@Getter
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = AlignmentParameters.Builder.class)
public class AlignmentParameters {
    /**
     * Build type for which the default parameters are provided.
     */
    public final String buildType;

    /**
     * The default parameters for the buildType.
     */
    public final String parameters;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
