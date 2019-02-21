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
package org.jboss.pnc.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor()
@JsonDeserialize(builder = ErrorResponse.Builder.class)
public class ErrorResponse {

    private final String errorType;

    private final String errorMessage;

    private final Object details;

    public ErrorResponse(String errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.details = null;
    }

    public ErrorResponse(Exception e) {
        this.errorType = e.getClass().getSimpleName();
        this.errorMessage = e.getMessage();
        this.details = null;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
