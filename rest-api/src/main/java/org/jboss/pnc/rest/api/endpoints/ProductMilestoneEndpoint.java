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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.response.DeliveredArtifactInMilestones;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.requests.DeliverablesAnalysisRequest;
import org.jboss.pnc.dto.requests.MilestoneCloseRequest;
import org.jboss.pnc.dto.requests.validation.VersionValidationRequest;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneStatistics;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.annotation.RespondWithStatus;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ArtifactPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.DeliverableAnalyzerOperationPage;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_DESCRIPTION;
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

@Tag(name = "Product Milestones")
@Path("/product-milestones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface ProductMilestoneEndpoint {
    static final String PM_ID = "ID of the product milestone";

    static final String CREATE_NEW_DESC = "Creates a new product milestone.";

    /**
     * {@value CREATE_NEW_DESC}
     * 
     * @param productMilestone
     * @return
     */
    @Operation(
            summary = CREATE_NEW_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = ENTITY_CREATED_CODE,
                            description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductMilestone.class))),
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
    @RespondWithStatus(Response.Status.CREATED)
    ProductMilestone createNew(@NotNull ProductMilestone productMilestone);

    static final String GET_SPECIFIC_DESC = "Gets a specific product milestone.";

    /**
     * {@value GET_SPECIFIC_DESC}
     * 
     * @param id {@value PM_ID}
     * @return
     */
    @Operation(
            summary = GET_SPECIFIC_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductMilestone.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON) // workaround for PATCH support
    ProductMilestone getSpecific(@Parameter(description = PM_ID) @PathParam("id") String id);

    static final String UPDATE_DESC = "Updates an existing product milestone.";

    /**
     * {@value UPDATE_DESC}
     * 
     * @param id {@value PM_ID}
     * @param productMilestone
     */
    @Operation(
            summary = UPDATE_DESC,
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
    @Path("/{id}")
    void update(@Parameter(description = PM_ID) @PathParam("id") String id, @NotNull ProductMilestone productMilestone);

    static final String PATCH_SPECIFIC_DESC = "Patch an existing product milestone.";

    /**
     * {@value PATCH_SPECIFIC_DESC}
     * 
     * @param id {@value PM_ID}
     * @param productMilestone
     * @return
     */
    @Operation(
            summary = PATCH_SPECIFIC_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductMilestone.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    ProductMilestone patchSpecific(
            @Parameter(description = PM_ID) @PathParam("id") String id,
            @NotNull ProductMilestone productMilestone);

    static final String GET_BUILDS_DESC = "Gets builds performed during a product milestone cycle.";

    /**
     * {@value GET_BUILDS_DESC}
     * 
     * @param id {@value PM_ID}
     * @param pageParameters
     * @param buildsFilter
     * @return
     */
    @Operation(
            summary = GET_BUILDS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/builds")
    Page<Build> getBuilds(
            @Parameter(description = PM_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters,
            @BeanParam BuildsFilterParameters buildsFilter);

    static final String CLOSE_MILESTONE_DESC = "Close a product milestone.";

    /**
     * {@value CLOSE_MILESTONE_DESC}
     *
     * @param id {@value PM_ID}
     * @return
     */
    @Hidden
    @POST
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/close")
    @Deprecated(forRemoval = true, since = "3.2")
    void closeMilestone(@PathParam("id") String id);

    /**
     * {@value CLOSE_MILESTONE_DESC}
     * 
     * @param id {@value PM_ID}
     * @return
     */
    @Operation(
            summary = CLOSE_MILESTONE_DESC,
            responses = { @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION),
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
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/close")
    void closeMilestone(
            @Parameter(description = PM_ID) @PathParam("id") String id,
            @Valid MilestoneCloseRequest closeRequest);

    static final String CLOSE_MILESTONE_CANCEL_DESC = "Cancel product milestone close process.";

    /**
     * {@value CLOSE_MILESTONE_CANCEL_DESC}
     * 
     * @param id {@value PM_ID}
     */
    @Operation(
            summary = CLOSE_MILESTONE_CANCEL_DESC,
            responses = { @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @DELETE
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/close")
    void cancelMilestoneClose(@Parameter(description = PM_ID) @PathParam("id") String id);

    static final String GET_DELIVERABLES_DESC = "Gets artifacts delivered in this milestone.";

    /**
     * {@value GET_BUILDS_DESC}
     *
     * @param id {@value PM_ID}
     * @param pageParameters
     * @return
     */
    @Operation(
            summary = GET_DELIVERABLES_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ArtifactPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/delivered-artifacts")
    Page<Artifact> getDeliveredArtifacts(
            @Parameter(description = PM_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

    static final String GET_ALL_DELIVERABLE_ANALYZER_OPERATIONS_FILTERED_DESC = "Gets all deliverable analyzer operations executed for this milestone.";

    /**
     * {@value GET_ALL_DELIVERABLE_ANALYZER_OPERATIONS_FILTERED_DESC}
     *
     * @param id {@value PM_ID}
     * @param pageParameters
     * @return
     */
    @Operation(
            summary = GET_ALL_DELIVERABLE_ANALYZER_OPERATIONS_FILTERED_DESC,
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
    @Path("/{id}/deliverables-analyzer-operations")
    Page<DeliverableAnalyzerOperation> getAllDeliverableAnalyzerOperations(
            @Parameter(description = PM_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

    static final String GET_STATISTICS = "Gets statistics about produced and delivered artifacts from this milestone.";

    /**
     * {@value GET_STATISTICS}
     *
     * @param id {@value PM_ID}
     * @return
     */
    @Operation(
            summary = GET_STATISTICS,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductMilestoneStatistics.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("{id}/statistics")
    ProductMilestoneStatistics getStatistics(@Parameter(description = PM_ID) @PathParam("id") String id);

    static final String VALIDATE_VERSION = "Validate product milestone version.";

    /**
     * {@value VALIDATE_VERSION}
     * 
     * @param productMilestone
     * @return
     */
    @Operation(
            summary = VALIDATE_VERSION,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("/validate-version")
    ValidationResponse validateVersion(@Valid VersionValidationRequest productMilestone);

    static final String DELIVERABLES_ANALYSIS_DESC = "Send a request to start analysis of deliverables. This endpoint creates an asynchronous task.";

    /**
     * {@value DELIVERABLES_ANALYSIS_DESC}
     *
     * @param id {@value PM_ID}
     * @return
     */
    @Operation(
            summary = DELIVERABLES_ANALYSIS_DESC,
            responses = { @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION),
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
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/analyze-deliverables")
    DeliverableAnalyzerOperation analyzeDeliverables(
            @Parameter(description = PM_ID) @PathParam("id") String id,
            @Valid DeliverablesAnalysisRequest request);

    static final String GET_DELIVERED_ARTIFACTS_COMPARISON = "Gets Artifacts Delivered in at least two of the Milestones and their versions.";

    /**
     * {@value GET_DELIVERED_ARTIFACTS_COMPARISON}
     *
     * @param milestoneIds
     * @return
     */
    @Operation(
            summary = GET_DELIVERED_ARTIFACTS_COMPARISON,
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/comparisons/delivered-artifacts")
    List<DeliveredArtifactInMilestones> compareArtifactVersionsDeliveredInMilestones(
            @NotEmpty @QueryParam("milestoneIds") List<String> milestoneIds);

    static final String GET_MILESTONES_INTERCONNECTION_GRAPH = "Finds Milestones sharing Delivered Artifacts with a requested Milestones, and so for those Milestones to create a graph. Maximum depth limit is 5.";

    /**
     * {@value GET_MILESTONES_INTERCONNECTION_GRAPH}
     *
     * @param milestoneId
     * @param depthLimit
     * @return
     */
    @Operation(
            summary = GET_MILESTONES_INTERCONNECTION_GRAPH,
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/interconnection-graph")
    Graph<ProductMilestone> getMilestonesSharingDeliveredArtifactsGraph(
            @Parameter(description = PM_ID) @PathParam("id") String milestoneId,
            @QueryParam("depthLimit") @Min(0) @Max(5) @DefaultValue("5") Integer depthLimit);

    static final String GET_DELIVERED_ARTIFACTS_SHARED_IN_MILESTONES = "Fetches Artifacts delivered in both of the Milestones.";

    /**
     * {@value GET_DELIVERED_ARTIFACTS_SHARED_IN_MILESTONES}
     *
     * @param pageParameters
     * @param milestone1Id
     * @param milestone2Id
     * @return
     */
    @Operation(
            summary = GET_DELIVERED_ARTIFACTS_SHARED_IN_MILESTONES,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ArtifactPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/delivered-artifacts/shared")
    Page<Artifact> getDeliveredArtifactsSharedInMilestones(
            @Valid @BeanParam PageParameters pageParameters,
            @Parameter(description = PM_ID) @NotBlank @QueryParam("milestone1") String milestone1Id,
            @Parameter(description = PM_ID) @NotBlank @QueryParam("milestone2") String milestone2Id);
}
