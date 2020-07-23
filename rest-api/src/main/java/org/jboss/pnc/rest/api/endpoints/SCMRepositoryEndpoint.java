/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.requests.CreateAndSyncSCMRequest;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildConfigPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.SCMRepositoryPage;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_UPDATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_UPDATED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.MATCH_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SCM_REPOSITORY_CREATING;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SEARCH_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SCM_REPOSITORY_CREATED;

/**
 * @author Jakub Bartecek
 */
@Tag(name = "SCM Repositories")
@Path("/scm-repositories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface SCMRepositoryEndpoint {
    static final String SCM_ID = "ID of the SCM repository";

    static final String MATCH_URL = "Url to search for";
    static final String SEARCH_URL = "Url part to search for";
    static final String GET_ALL = "Gets all SCM repositories.";

    /**
     * {@value GET_ALL}
     *
     * @param pageParameters
     * @param matchUrl {@value MATCH_URL}
     * @param searchUrl {@value SEARCH_URL}
     * @return
     */
    @Operation(
            summary = GET_ALL,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = SCMRepositoryPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    Page<SCMRepository> getAll(
            @Valid @BeanParam PageParameters pageParameters,
            @Parameter(description = MATCH_URL) @QueryParam(MATCH_QUERY_PARAM) String matchUrl,
            @Parameter(description = SEARCH_URL) @QueryParam(SEARCH_QUERY_PARAM) String searchUrl);

    static final String GET_SPECIFIC_DESC = "Gets a specific SCM repository.";

    /**
     * {@value GET_SPECIFIC_DESC}
     *
     * @param id {@value SCM_ID}
     * @return
     */
    @Operation(
            summary = GET_SPECIFIC_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = SCMRepository.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON) // workaround for PATCH support
    SCMRepository getSpecific(@Parameter(description = SCM_ID) @PathParam("id") String id);

    static final String UPDATE_DESC = "Updates an existing SCM repository.";

    /**
     * {@value UPDATE_DESC}
     *
     * @param id {@value SCM_ID}
     * @param scmRepository
     */
    @Operation(
            summary = UPDATE_DESC,
            responses = {
                    @ApiResponse(responseCode = ENTITY_UPDATED_CODE, description = ENTITY_UPDATED_DESCRIPTION),
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
    void update(@Parameter(description = SCM_ID) @PathParam("id") String id, @NotNull SCMRepository scmRepository);

    static final String PATCH_SPECIFIC = "Patch an existing SCM repository.";

    /**
     * {@value PATCH_SPECIFIC}
     *
     * @param id {@value SCM_ID}
     * @param scmRepository
     * @return
     */
    @Operation(
            summary = PATCH_SPECIFIC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = SCMRepository.class))),
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
    SCMRepository patchSpecific(
            @Parameter(description = SCM_ID) @PathParam("id") String id,
            @NotNull SCMRepository scmRepository);

    static final String CREATE_NEW_DESC = "Creates a new SCM repository.";
    static final String CREATE_NEW_DESC2 = "If the given URL is external, it does create the repository in the scm server.";

    /**
     * {@value CREATE_NEW_DESC} {@value CREATE_NEW_DESC2}
     *
     * @param request
     * @return
     */
    @Operation(
            summary = CREATE_NEW_DESC,
            description = CREATE_NEW_DESC2,
            responses = {
                    @ApiResponse(
                            responseCode = ACCEPTED_CODE,
                            description = SCM_REPOSITORY_CREATING,
                            content = @Content(schema = @Schema(implementation = RepositoryCreationResponse.class))),
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SCM_REPOSITORY_CREATED,
                            content = @Content(schema = @Schema(implementation = RepositoryCreationResponse.class))),
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
    @Path("/create-and-sync")
    RepositoryCreationResponse createNew(@NotNull CreateAndSyncSCMRequest request);

    static final String GET_BUILD_CONFIGS = "Gets all build configs associated with a specific SCM repository.";

    /**
     * {@value GET_BUILD_CONFIGS}
     *
     * @param id {@value SCM_ID}
     * @param pageParameters
     * @return
     */
    @Operation(
            summary = GET_BUILD_CONFIGS,
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
            @Parameter(description = SCM_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

}
