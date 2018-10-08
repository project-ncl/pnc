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
package org.jboss.pnc.causewayclient.remotespi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaggedBuild {

    @NonNull
    private final String tagPrefix;

    private final int brewBuildId;

    @JsonCreator
    public TaggedBuild(@JsonProperty("tagPrefix") String tagPrefix,
            @JsonProperty("brewBuildId") int brewBuildId) {
        this.tagPrefix = Objects.requireNonNull(tagPrefix, "Tag prefix must be specified.");
        this.brewBuildId = brewBuildId;
    }
}
