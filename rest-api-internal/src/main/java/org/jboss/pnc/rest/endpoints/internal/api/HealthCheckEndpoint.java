/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints.internal.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = SwaggerConstants.TAG_INTERNAL)
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Client
public interface HealthCheckEndpoint {

    @Operation(
            summary = "Performs health checks on the system to see if all the components are go",
            responses = {
                    @ApiResponse(responseCode = SwaggerConstants.SUCCESS_CODE, description = "Success"),
                    @ApiResponse(
                            responseCode = SwaggerConstants.SERVER_ERROR_CODE,
                            description = "At least one of the health checks has failed") })
    @GET
    Response check();
}
