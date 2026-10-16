/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
