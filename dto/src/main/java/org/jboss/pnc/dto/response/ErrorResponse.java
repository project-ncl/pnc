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
import lombok.Data;

/**
 * Response indicating there was an error processing request.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor()
@JsonDeserialize(builder = ErrorResponse.Builder.class)
public class ErrorResponse {

    /**
     * Type of the error.
     */
    private final String errorType;

    /**
     * User readable error message.
     */
    private final String errorMessage;

    /**
     * Object with more detailed information. This can be null, otherwise the object type depends on the error type.
     */
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

    public ErrorResponse(Exception e, Object details) {
        this.errorType = e.getClass().getSimpleName();
        this.errorMessage = e.getMessage();
        this.details = details;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
