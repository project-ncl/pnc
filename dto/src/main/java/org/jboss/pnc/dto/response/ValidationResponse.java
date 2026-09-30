/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.jboss.pnc.enums.ValidationErrorType;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Response to a validation request.
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ValidationResponse.Builder.class)
public class ValidationResponse {

    /**
     * Is the data in the request valid?
     */
    @NotNull
    public final Boolean isValid;

    /**
     * If the data in the request are not valid, the validation error type. If they are valid this is null.
     * 
     * @see ValidationErrorType
     */
    public final ValidationErrorType errorType;

    /**
     * User readable validation hints.
     */
    public List<String> hints;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
