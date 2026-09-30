/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(builderClassName = "Builder")
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class MilestoneCloseRequest {
    private Boolean skipBrewPush;
}
