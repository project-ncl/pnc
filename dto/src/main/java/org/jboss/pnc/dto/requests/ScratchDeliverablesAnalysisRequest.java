/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.URL;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * This type of request differs from {@link DeliverablesAnalysisRequest} in a way that this request is used to start
 * only "SCRATCH" analysis.
 */
@Value
@Builder(builderClassName = "Builder")
@Jacksonized
public class ScratchDeliverablesAnalysisRequest {

    @NotEmpty
    List<@URL String> deliverablesUrls;
}
