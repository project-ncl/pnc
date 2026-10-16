/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.api.endpoints;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.BREW_NOT_SUPPORTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_CODE;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.processor.annotation.Client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This endpoint provides API for manipulating with {@code BuildPushReport}.
 */
@Tag(name = "Build pushes")
@Path("/build-pushes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface BuildPushesEndpoint {

    static final String GET_PUSH_REPORT_DESC = "Get specific Report of Build Push Operation.";
    static final String BUILD_PUSH_RESULT_ID = "ID of the Build Push Report.";

    /**
     * {@value GET_PUSH_REPORT_DESC}
     *
     * @param operationId {@value BUILD_PUSH_RESULT_ID}
     * @return
     *
     * @deprecated Brew is not available, the endpoint rejects every request
     */
    @Operation(
            summary = GET_PUSH_REPORT_DESC,
            responses = @ApiResponse(
                    responseCode = FORBIDDEN_CODE,
                    description = BREW_NOT_SUPPORTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))))
    @GET
    @Path("/{id}")
    @TimedMetric
    @Deprecated
    BuildPushReport getPushReport(@Parameter(description = BUILD_PUSH_RESULT_ID) @PathParam("id") String operationId);

}
