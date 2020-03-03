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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonDeserialize(builder = ArtifactImportError.Builder.class)
public class ArtifactImportError {

    private final String artifactId;

    private final String errorMessage;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }

    public static String combineMessages(List<ArtifactImportError> artifactImportErrors) {
        if (artifactImportErrors == null || artifactImportErrors.isEmpty()) {
            return "";
        }
        return " --- artifact errors ---\n" + artifactImportErrors.stream()
                .map(e -> e.getArtifactId() + ": " + e.getErrorMessage())
                .collect(Collectors.joining("\n"));
    }

    /**
     * @return append artifacts errors to the prefix and return combined string. When artifactImportErrors is empty only
     *         a prefix is returned.
     */
    public static String combineMessages(String prefix, List<ArtifactImportError> artifactImportErrors) {
        String errors = ArtifactImportError.combineMessages(artifactImportErrors);
        if (!errors.isEmpty()) {
            return prefix + "\n\n" + errors;
        } else {
            return prefix;
        }

    }
}
