/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints.internal.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.pnc.api.deliverablesanalyzer.dto.AnalysisResult;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.annotation.RespondWithStatus;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This endpoint is used for interacting with Deliverable Analyzer processes.
 *
 * @author Honza Brázdil
 */
@Tag(name = SwaggerConstants.TAG_INTERNAL)
@Path("/deliverable-analyses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface DeliverableAnalysisEndpoint {

    @Operation(
            summary = "Notify PNC about finished Deliverable anaylysis.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerConstants.ACCEPTED_CODE,
                            description = SwaggerConstants.ACCEPTED_DESCRIPTION) })
    @POST
    @Path("/complete")
    @Consumes(MediaType.APPLICATION_JSON)
    @RespondWithStatus(Response.Status.ACCEPTED)
    void completeAnalysis(@Parameter(description = "Analysis response") AnalysisResult response);

}
