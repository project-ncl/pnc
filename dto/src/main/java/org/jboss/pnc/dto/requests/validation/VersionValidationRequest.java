/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests.validation;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Request for explicit product milestone version validation.
 *
 * @author jmichalo <jmichalo@redhat.com>
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = VersionValidationRequest.Builder.class)
public class VersionValidationRequest {

    /**
     * Id of the product version. The product version is used to prevent duplicate milestones with the same milestone
     * version.
     */
    @NotBlank
    public final String productVersionId;

    /**
     * The version to be validated.
     */
    @NotBlank
    public final String version;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
