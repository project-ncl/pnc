/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A request validation error.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor()
@JsonDeserialize(builder = Validation.Builder.class)
public class Validation {

    /**
     * Identifier of the attribute which didn't pass validation.
     */
    private final String attribute;

    /**
     * User readable validation messages.
     */
    private final List<String> messages;

    /**
     * The original non-valid value. Based on the validation this may not be set.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Object value;

    public Validation(String attribute, String message, Object value) {
        this.attribute = attribute;
        this.messages = Collections.singletonList(message);
        this.value = value;
    }

    public Validation(String attribute, String message) {
        this.attribute = attribute;
        this.messages = Collections.singletonList(message);
        this.value = null;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
