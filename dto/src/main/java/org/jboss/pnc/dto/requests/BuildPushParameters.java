/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Request to push build to Koji.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = BuildPushParameters.Builder.class)
public class BuildPushParameters {

    /**
     * Koji tag prefix, to which the build should be tagged upon import.
     */
    @NotBlank
    private final String tagPrefix;

    /**
     * Indicator whether new koji build should be created if it was already imported before. Defaults to false.
     */
    private final boolean reimport;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
