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
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This endpoint is used for starting and interacting with BPM processes.
 *
 * @author Jakub Senko
 */
@Tag(name = "Internal")
@Path("/bpm")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BpmEndpoint{

    @Operation(summary = "Notify PNC about a BPM task event. Accepts polymorphic JSON {\"eventType\": \"string\"} based on \"eventType\" field.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = "Success")
    })
    @POST
    @Path("/tasks/{taskId}/notify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response notifyTask(@Parameter(description = "BPM task ID") @PathParam("taskId") int taskId);
    
    @Operation(summary = "List of (recently) active BPM tasks.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BpmTaskPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BpmTaskPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/tasks")
    public Response getBPMTasks(
            @Parameter(description = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @Parameter(description = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize
    );

    @Operation(summary = "Get single (recently) active BPM task.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BpmTaskSingleton.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
    })
    @GET
    @Path("/tasks/{taskId}")
    public Response getBPMTaskById(@Parameter(description = "BPM task ID") @PathParam("taskId") int taskId);

}
