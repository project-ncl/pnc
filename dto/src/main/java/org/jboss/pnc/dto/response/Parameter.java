/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Generic parameter for build configs.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Parameter.Builder.class)
public class Parameter {

    /**
     * Parameter name.
     */
    public final String name;

    /**
     * Parameter description.
     */
    public final String description;

    /**
     * List of possible values for a parameter.
     */
    public final List<String> values;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
