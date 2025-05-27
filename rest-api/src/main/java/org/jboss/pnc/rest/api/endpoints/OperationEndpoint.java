/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.requests.ScratchDeliverablesAnalysisRequest;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.annotation.RespondWithStatus;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPushOperationPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.DeliverableAnalyzerOperationPage;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_UPDATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_UPDATED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Tag(name = "Operations")
@Path("/operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface OperationEndpoint {

    static final String GET_ALL_DELIVERABLE_ANALYZER_OPERATIONS_DESC = "Gets all deliverable analyzer operations.";
    static final String GET_ALL_BUILD_PUSH_OPERATIONS_DESC = "Gets all build push operations.";
    static final String COMPLETE_OPERATION_DESC = "Complete a running operation.";
    static final String OPERATION_RESULT = "Result of completed operation.";
    static final String UPDATE_DEL_ANALYZER_DESC = "Updates an existing deliverable analyzer operation.";
    static final String START_DELIVERABLE_ANALYSIS = "Starts a new deliverable analysis of deliverables given by URLs";
    static final String GET_SPECIFIC_DEL_ANALYZER_DESC = "Gets a specific delivarable analyzer operation.";
    static final String GET_SPECIFIC_BUILD_PUSH_DESC = "Gets a specific build push operation.";
    static final String OP_ID = "ID of the operation";

    /**
     * {@value COMPLETE_OPERATION_DESC}
     *
     * @param id {@value OP_ID}
     */
    @Operation(
            summary = COMPLETE_OPERATION_DESC,
            tags = SwaggerConstants.TAG_INTERNAL,
            responses = { @ApiResponse(responseCode = ENTITY_UPDATED_CODE, description = ENTITY_UPDATED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = CONFLICTED_CODE,
                            description = CONFLICTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("/{id}/complete")
    void finish(
            @Parameter(description = OP_ID) @PathParam("id") String id,
            @Parameter(description = OPERATION_RESULT, required = true) @QueryParam("result") OperationResult result);

    /**
     * {@value UPDATE_DEL_ANALYZER_DESC}
     *
     * @param id {@value OP_ID}
     * @return
     */
    @Operation(
            summary = "[role:pnc-users-admin] " + UPDATE_DEL_ANALYZER_DESC,
            tags = SwaggerConstants.TAG_INTERNAL,
            responses = { @ApiResponse(responseCode = ENTITY_UPDATED_CODE, description = ENTITY_UPDATED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = CONFLICTED_CODE,
                            description = CONFLICTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @PUT
    @Path("/deliverable-analyzer/{id}")
    DeliverableAnalyzerOperation updateDeliverableAnalyzer(
            @Parameter(description = OP_ID) @PathParam("id") String id,
            @NotNull DeliverableAnalyzerOperation operation);

    /**
     * {@value GET_SPECIFIC_DEL_ANALYZER_DESC}
     *
     * @param id {@value OP_ID}
     * @return
     */
    @Operation(
            summary = GET_SPECIFIC_DEL_ANALYZER_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = DeliverableAnalyzerOperation.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/deliverable-analyzer/{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON) // workaround for PATCH support
    DeliverableAnalyzerOperation getSpecificDeliverableAnalyzer(
            @Parameter(description = OP_ID) @PathParam("id") String id);

    /**
     * {@value GET_ALL_DELIVERABLE_ANALYZER_OPERATIONS_DESC}
     *
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_ALL_DELIVERABLE_ANALYZER_OPERATIONS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(
                                    schema = @Schema(implementation = DeliverableAnalyzerOperationPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/deliverable-analyzer")
    Page<DeliverableAnalyzerOperation> getAllDeliverableAnalyzerOperation(@Valid @BeanParam PageParameters pageParams);

    @Operation(
            summary = START_DELIVERABLE_ANALYSIS,
            responses = {
                    @ApiResponse(
                            responseCode = ACCEPTED_CODE,
                            description = ACCEPTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = DeliverableAnalyzerOperation.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("/deliverable-analyzer/start")
    @RespondWithStatus(Response.Status.ACCEPTED)
    DeliverableAnalyzerOperation startScratchDeliverableAnalysis(
            @Valid ScratchDeliverablesAnalysisRequest scratchDeliverablesAnalysisRequest);

    /**
     * {@value GET_SPECIFIC_BUILD_PUSH_DESC}
     *
     * @param id {@value OP_ID}
     * @return
     */
    @Operation(
            summary = GET_SPECIFIC_BUILD_PUSH_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildPushOperation.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/build-pushes/{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON) // workaround for PATCH support
    BuildPushOperation getSpecificBuildPush(@Parameter(description = OP_ID) @PathParam("id") String id);

    /**
     * {@value GET_ALL_BUILD_PUSH_OPERATIONS_DESC}
     *
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_ALL_BUILD_PUSH_OPERATIONS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildPushOperationPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/build-pushes")
    Page<BuildPushOperation> getAllBuildPushOperation(@Valid @BeanParam PageParameters pageParams);
}
