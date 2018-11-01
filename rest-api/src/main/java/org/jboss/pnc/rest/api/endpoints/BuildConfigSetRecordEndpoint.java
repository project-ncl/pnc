/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.rest.api.endpoints;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.GroupBuildSingleton;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.GroupBuildPage;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

import javax.ws.rs.BeanParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "")
@Path("/build-config-set-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BuildConfigSetRecordEndpoint {

    @Operation(summary = "Gets all build config set execution records",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupBuildPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupBuildPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    public Response getAll(@BeanParam PageParameters pageParameters);

    @Operation(summary = "Gets specific build config set execution record",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupBuildSingleton.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupBuildSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(@Parameter(description = "GroupBuild id", required = true) @PathParam("id") @NotNull Integer id);

    @Operation(summary = "Delete specific Build Config Set Record (it must be from a temporary build). Operation is async, for the result subscribe to 'build-config-set-records#delete' events with optional qualifier buildRecord.id.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}")
    public Response delete(@Parameter(description = "GroupBuild id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Gets the build records associated with this set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/build-records")
    public Response getBuilds(@BeanParam PageParameters pageParameters,
            @Parameter(description = "Build Config set record id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Gets dependency graph for a Build Group Record (running and completed).",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/dependency-graph")
    public Response getDependencyGraphForSet(@Parameter(description = "Build record set id.", required = true) @PathParam("id") Integer bcSetRecordId);

}
