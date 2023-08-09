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
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneArtifactQualityStatistics;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneRepositoryTypeStatistics;
import org.jboss.pnc.dto.response.statistics.ProductVersionStatistics;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.annotation.RespondWithStatus;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildConfigPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.GroupConfigPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ProductMilestonePage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ProductReleasePage;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

@Tag(name = "Product Versions")
@Path("/product-versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface ProductVersionEndpoint {
    static final String PV_ID = "ID of the product version";

    static final String CREATE_NEW_DESC = "Creates a new product version.";

    /**
     * {@value CREATE_NEW_DESC}
     * 
     * @param productVersion
     * @return
     */
    @Operation(
            summary = CREATE_NEW_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = ENTITY_CREATED_CODE,
                            description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductVersion.class))),
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
    ProductVersion createNew(@NotNull ProductVersion productVersion);

    static final String GET_SPECIFIC_DESC = "Gets a specific product version.";

    /**
     * {@value GET_SPECIFIC_DESC}
     * 
     * @param id
     * @return
     */
    @Operation(
            summary = GET_SPECIFIC_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductVersion.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON) // workaround for PATCH support
    ProductVersion getSpecific(@Parameter(description = PV_ID) @PathParam("id") String id);

    static final String UPDATE_DESC = "Updates an existing product version.";

    /**
     * {@value UPDATE_DESC}
     * 
     * @param id {@value PV_ID}
     * @param productVersion
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
    void update(@Parameter(description = PV_ID) @PathParam("id") String id, @NotNull ProductVersion productVersion);

    static final String PATCH_SPECIFIC_DESC = "Patch an existing product version.";

    /**
     * {@value PATCH_SPECIFIC_DESC}
     * 
     * @param id {@value PV_ID}
     * @param productVersion
     * @return
     */
    @Operation(
            summary = PATCH_SPECIFIC_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductVersion.class))),
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
    ProductVersion patchSpecific(
            @Parameter(description = PV_ID) @PathParam("id") String id,
            @NotNull ProductVersion productVersion);

    static final String GET_BUILD_CONFIGS_DESC = "Gets all build configs in a specific product version.";

    /**
     * {@value GET_BUILD_CONFIGS_DESC}
     * 
     * @param id {@value PV_ID}
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_BUILD_CONFIGS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfigPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/build-configs")
    Page<BuildConfiguration> getBuildConfigs(
            @Parameter(description = PV_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParams);

    static final String GET_GROUP_CONFIGS = "Gets group configs associated with a specific product version.";

    /**
     * {@value GET_GROUP_CONFIGS}
     * 
     * @param id {@value PV_ID}
     * @param pageParameters
     * @return
     */
    @Operation(
            summary = GET_GROUP_CONFIGS,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = GroupConfigPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/group-configs")
    Page<GroupConfiguration> getGroupConfigs(
            @Parameter(description = PV_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

    static final String GET_MILESTONES = "Gets all product milestones of a specific product version.";

    /**
     * {@value GET_MILESTONES}
     * 
     * @param id {@value PV_ID}
     * @param pageParameters
     * @return
     */
    @Operation(
            summary = GET_MILESTONES,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductMilestonePage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/milestones")
    Page<ProductMilestone> getMilestones(
            @Parameter(description = PV_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

    static final String GET_RELEASES = "Gets all product releases of a specific product version.";

    /**
     * {@value GET_RELEASES}
     * 
     * @param id {@value PV_ID}
     * @param pageParameters
     * @return
     */
    @Operation(
            summary = GET_RELEASES,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductReleasePage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/releases")
    Page<ProductRelease> getReleases(
            @Parameter(description = PV_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

    static final String GET_PV_STATISTICS = "Gets statistics about produced and delivered artifacts from this version.";

    /**
     * {@value GET_PV_STATISTICS}
     *
     * @param id {@value PV_ID}
     * @return
     */
    @Operation(
            summary = GET_PV_STATISTICS,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductVersionStatistics.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/statistics")
    ProductVersionStatistics getStatistics(@Parameter(description = PV_ID) @PathParam("id") String id);

    static final String GET_ARTIFACT_QUALITIES_STATISTICS = "Gets statistics about proportion of quality of delivered artifacts in this version.";

    /**
     * {@value GET_ARTIFACT_QUALITIES_STATISTICS}
     */
    @Operation(
            summary = GET_ARTIFACT_QUALITIES_STATISTICS,
            responses = { @ApiResponse(
                    responseCode = SUCCESS_CODE,
                    description = SUCCESS_DESCRIPTION,
                    content = @Content(
                            schema = @Schema(
                                    implementation = SwaggerPages.ProductVersionArtifactQualityStatisticsPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/artifact-quality-statistics")
    Page<ProductMilestoneArtifactQualityStatistics> getArtifactQualitiesStatistics(
            @Parameter(description = PV_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

    static final String GET_REPOSITORY_TYPES_STATISTICS = "Gets statistics about proportion of repository types of delivered artifacts in this version.";

    @Operation(
            summary = GET_REPOSITORY_TYPES_STATISTICS,
            responses = { @ApiResponse(
                    responseCode = SUCCESS_CODE,
                    description = SUCCESS_DESCRIPTION,
                    content = @Content(
                            schema = @Schema(
                                    implementation = SwaggerPages.ProductVersionRepositoryTypeStatisticsPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/repository-type-statistics")
    Page<ProductMilestoneRepositoryTypeStatistics> getRepositoryTypesStatistics(
            @Parameter(description = PV_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);
}
