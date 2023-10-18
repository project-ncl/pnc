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
import org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.dto.requests.labels.DeliverableAnalyzerReportLabelRequest;
import org.jboss.pnc.dto.response.AnalyzedArtifact;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages;

import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_DELETED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_DELETED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

/**
 * This endpoint provides API for manipulating with {@code DeliverableAnalyzerReport} for external usage, not internal,
 * as it does {@code DeliverableAnalysisEndpoint} in the rest module.
 */
@Tag(name = "Deliverable Analysis")
@Path("/deliverable-analysis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface DeliverableAnalyzerReportEndpoint {

    String DEL_AN_ID = "Id of the Deliverable Analysis Report";

    String GET_ANALYZED_ARTIFACTS = "Gets analyzed artifacts of this deliverable analysis report";

    String ADD_DEL_AN_REPORT_LABEL = "Adds label to this deliverable analyzer report";

    String REMOVE_DEL_AN_REPORT_LABEL = "Removes label from this deliverable analyzer report";

    String GET_DEL_AN_REPORT_LABEL_HISTORY = "Gets the label history of this deliverable analyzer report";

    @Operation(
            summary = GET_ANALYZED_ARTIFACTS,
            responses = {
                    @ApiResponse(
                            description = SUCCESS_DESCRIPTION,
                            responseCode = SUCCESS_CODE,
                            content = @Content(
                                    schema = @Schema(implementation = SwaggerPages.AnalyzedArtifactPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @Path("/{id}/analyzed-artifacts")
    @GET
    Page<AnalyzedArtifact> getAnalyzedArtifacts(
            @Parameter(description = DEL_AN_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

    @Operation(
            summary = ADD_DEL_AN_REPORT_LABEL,
            responses = { @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @Path("/{id}/label")
    @POST
    void addLabel(
            @Parameter(description = DEL_AN_ID) @PathParam("id") String id,
            @Valid DeliverableAnalyzerReportLabelRequest request);

    @Operation(
            summary = REMOVE_DEL_AN_REPORT_LABEL,
            responses = { @ApiResponse(responseCode = ENTITY_DELETED_CODE, description = ENTITY_DELETED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @Path("/{id}/label")
    @DELETE
    void removeLabel(
            @Parameter(description = DEL_AN_ID) @PathParam("id") String id,
            @Valid DeliverableAnalyzerReportLabelRequest request);

    @Operation(
            summary = GET_DEL_AN_REPORT_LABEL_HISTORY,
            responses = { @ApiResponse(
                    responseCode = SUCCESS_CODE,
                    description = SUCCESS_DESCRIPTION,
                    content = @Content(
                            schema = @Schema(implementation = SwaggerPages.DeliverableAnalyzerLabelEntryPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @Path("/{id}/label-history")
    @GET
    Page<DeliverableAnalyzerLabelEntry> getLabelHistory(
            @Parameter(description = DEL_AN_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);
}
