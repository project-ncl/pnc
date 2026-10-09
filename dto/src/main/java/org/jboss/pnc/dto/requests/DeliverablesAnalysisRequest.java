/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.URL;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(builderClassName = "Builder")
@Jacksonized
public class DeliverablesAnalysisRequest {

    @NotEmpty
    private final List<@URL String> deliverablesUrls;

    private boolean runAsScratchAnalysis;
}
