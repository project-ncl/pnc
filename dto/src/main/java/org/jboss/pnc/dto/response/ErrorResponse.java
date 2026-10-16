/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
