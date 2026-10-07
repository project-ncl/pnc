/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class EnvironmentDeprecationRequest {
    /**
     * The ID of the environment, that should be used instead of the one being deprecated.
     */
    @NotBlank
    String replacementEnvironmentId;
}
