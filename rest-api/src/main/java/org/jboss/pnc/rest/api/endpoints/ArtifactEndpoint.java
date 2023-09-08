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
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.requests.QValue;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.annotation.RespondWithStatus;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.parameters.PaginationParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ArtifactPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ArtifactInfoPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ArtifactRevisionPage;
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
import java.util.Set;

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

@Tag(name = "Artifacts")
@Path("/artifacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface ArtifactEndpoint {
    static final String A_ID = "ID of the artifact";
    static final String A_PURL = "Purl of the artifact";
    static final String A_REV = "Revision number of the artifact";

    static final String GET_ALL_DESC = "Gets all artifacts.";
    static final String FILTER_SHA256_DESC = "Filter by sha256 of the artifact.";
    static final String FILTER_SHA1_DESC = "Filter by sha1 of the artifact.";
    static final String FILTER_MD5_DESC = "Filter by md5 of the artifact.";

    /**
     * {@value GET_ALL_DESC}
     *
     * @param pageParams
     * @param sha256 {@value FILTER_SHA256_DESC}
     * @param md5 {@value FILTER_MD5_DESC}
     * @param sha1 {@value FILTER_SHA1_DESC}
     * @return
     */
    @Operation(
            summary = GET_ALL_DESC,
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
    Page<Artifact> getAll(
            @Valid @BeanParam PageParameters pageParams,
            @Parameter(description = FILTER_SHA256_DESC) @QueryParam("sha256") String sha256,
            @Parameter(description = FILTER_MD5_DESC) @QueryParam("md5") String md5,
            @Parameter(description = FILTER_SHA1_DESC) @QueryParam("sha1") String sha1);

    static final String GET_ALL_FILTERED_DESC = "Gets all artifacts according to specified filters.";
    static final String FILTER_IDENTIFIER_DESC = "Filter by artifact identifier or its part.";
    static final String FILTER_QUALITY_DESC = "List of artifact qualities to include in result.";
    static final String FILTER_BUILD_CATEGORY_DESC = "List of artifact build categories to include in result.";
    static final String FILTER_REPOSITORY_TYPE_DESC = "Type of target repository.";
    static final String FETCH_QUALIFIERS_DESC = "List of Qualifiers with values. Returned artifacts will be enhanced"
            + " with these values if they are qualified. Endpoint DOES NOT return a Qualifier with value if it is not"
            + " listed in the request even though an artifact may have additional values for the Qualifier.";

    /**
     * {@value GET_ALL_FILTERED_DESC}
     *
     * @param paginationParameters
     * @param identifier {@value FILTER_IDENTIFIER_DESC}
     * @param qualities {@value FILTER_QUALITY_DESC}
     * @param repoType {@value FILTER_REPOSITORY_TYPE_DESC}
     * @param buildCategories {@value FILTER_BUILD_CATEGORY_DESC}
     * @param {@value FILTER_BUILD_CATEGORY_DESC}
     * @return
     */
    @Operation(
            summary = GET_ALL_FILTERED_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ArtifactInfoPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/filter")
    Page<ArtifactInfo> getAllFiltered(
            @Valid @BeanParam PaginationParameters paginationParameters,
            @Parameter(description = FILTER_IDENTIFIER_DESC) @QueryParam("identifier") String identifier,
            @Parameter(description = FILTER_QUALITY_DESC) @QueryParam("qualities") Set<ArtifactQuality> qualities,
            @Parameter(description = FILTER_REPOSITORY_TYPE_DESC) @QueryParam("repoType") RepositoryType repoType,
            @Parameter(
                    description = FILTER_BUILD_CATEGORY_DESC) @QueryParam("buildCategories") Set<BuildCategory> buildCategories,
            @Parameter(description = FETCH_QUALIFIERS_DESC) @QueryParam("qualifiers") Set<QValue> qualifiers);

    static final String GET_SPECIFIC_DESC = "Gets a specific build config.";

    /**
     * {@value GET_SPECIFIC_DESC}
     *
     * @param id {@value A_ID}
     * @return
     */
    @Operation(
            summary = GET_SPECIFIC_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Artifact.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}")
    Artifact getSpecific(@Parameter(description = "ID of the Artifact") @PathParam("id") String id);

    /**
     * {@value GET_SPECIFIC_DESC}
     *
     * @param purl {@value A_PURL}
     * @return
     */
    @Operation(
            summary = GET_SPECIFIC_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Artifact.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/purl/{purl}")
    Artifact getSpecificFromPurl(@Parameter(description = "Purl of the Artifact") @PathParam("purl") String purl);

    static final String CREATE_DESC = "Creates a new Artifact.";

    /**
     * {@value CREATE_DESC} {@value SwaggerConstants#REQUIRES_ADMIN}
     *
     * @param artifact
     * @return
     */
    @Operation(
            summary = "[role:pnc-users-artifact-admin, pnc-users-admin] " + CREATE_DESC,
            tags = SwaggerConstants.TAG_INTERNAL,
            responses = {
                    @ApiResponse(
                            responseCode = ENTITY_CREATED_CODE,
                            description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Artifact.class))),
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
    Artifact create(@NotNull Artifact artifact);

    static final String UPDATE_DESC = "Updates an existing Artifact";

    /**
     * {@value UPDATE_DESC} {@value SwaggerConstants#REQUIRES_ADMIN}
     *
     * @param id {@value A_ID}
     * @param artifact
     */
    @Operation(
            summary = "[role:pnc-users-artifact-admin, pnc-users-admin] " + UPDATE_DESC,
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
    @Path("/{id}")
    void update(@PathParam("id") String id, @NotNull Artifact artifact);

    static final String CREATE_ARTIFACT_QUALITY_REVISION = "Add a new quality level revision for this artifact. Accepted values from standard users are NEW, VERIFIED, TESTED, DEPRECATED. Users with pnc-users-artifact-admin, pnc-users-build-admin, pnc-users-admin role can also specify BLACKLISTED and DELETED quality levels.";
    static final String ARTIFACT_QUALITY = "Quality level of the artifact.";
    static final String ARTIFACT_QUALITY_REASON = "The reason for adding a new quality level for this artifact.";

    /**
     * {@value CREATE_ARTIFACT_QUALITY_REVISION}
     *
     * @param id {@value A_ID}
     * @param quality {@value ARTIFACT_QUALITY}
     * @param reason {@value ARTIFACT_QUALITY_REASON}
     */
    @Operation(
            summary = CREATE_ARTIFACT_QUALITY_REVISION,
            responses = {
                    @ApiResponse(
                            responseCode = ENTITY_CREATED_CODE,
                            description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ArtifactRevision.class))),
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
    @Path("/{id}/artifacts/quality")
    ArtifactRevision createQualityLevelRevision(
            @Parameter(description = A_ID) @PathParam("id") String id,
            @Parameter(description = ARTIFACT_QUALITY, required = true) @QueryParam("quality") String quality,
            @Parameter(description = ARTIFACT_QUALITY_REASON, required = true) @QueryParam("reason") String reason);

    static final String GET_DEPENDANT_BUILDS_DESC = "Gets the build(s) that depends on this artifact.";

    /**
     * {@value GET_DEPENDANT_BUILDS_DESC}
     *
     * @param id {@value A_ID}
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_DEPENDANT_BUILDS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = SwaggerPages.BuildPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/dependant-builds")
    Page<Build> getDependantBuilds(
            @Parameter(description = A_ID) @PathParam("id") String id,
            @BeanParam PageParameters pageParams);

    static final String GET_MILESTONES_INFO_DESC = "Gets the milestones that produced or consumed this artifact.";

    /**
     * {@value GET_MILESTONES_INFO_DESC}
     *
     * @param id {@value A_ID}
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_MILESTONES_INFO_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(
                                    schema = @Schema(implementation = SwaggerPages.MilestoneInfoPage.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/milestones")
    Page<MilestoneInfo> getMilestonesInfo(
            @Parameter(description = A_ID) @PathParam("id") String id,
            @BeanParam PaginationParameters pageParams);

    static final String GET_ARTIFACT_REVISIONS_DESC = "Gets audited revisions of this artifact.";

    /**
     * {@value GET_ARTIFACT_REVISIONS_DESC}
     *
     * @param id {@value A_ID}
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_ARTIFACT_REVISIONS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ArtifactRevisionPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/revisions")
    Page<ArtifactRevision> getRevisions(
            @Parameter(description = A_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParams);

    static final String GET_ARTIFACT_REVISION_DESC = "Get specific audited revision of this artifact.";

    /**
     * {@value GET_ARTIFACT_REVISION_DESC}
     *
     * @param id {@value A_ID}
     * @param rev {@value A_REV}
     * @return
     */
    @Operation(
            summary = GET_ARTIFACT_REVISION_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ArtifactRevision.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/revisions/{rev}")
    ArtifactRevision getRevision(
            @Parameter(description = A_ID) @PathParam("id") String id,
            @Parameter(description = A_REV) @PathParam("rev") int rev);
}
