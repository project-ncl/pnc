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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.requests.BuildConfigWithSCMRequest;
import org.jboss.pnc.dto.response.AlignmentParameters;
import org.jboss.pnc.dto.response.BuildConfigCreationResponse;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.annotation.RespondWithStatus;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildConfigPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildConfigRevisionPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.GroupConfigPage;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.BUILD_CONFIG_CREATED;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.BUILD_CONFIG_CREATING;
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
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Tag(name = "Build Configs")
@Path("/build-configs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface BuildConfigurationEndpoint {
    static final String BC_ID = "ID of the build config";
    static final String REV = "Revision number of the build config";
    static final String B_TYPE = "Build type specified in build config (MVN, NPM or GRADLE)";

    static final String GET_ALL_DESC = "Gets all build configs.";

    /**
     * {@value GET_ALL_DESC}
     *
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_ALL_DESC,
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
    Page<BuildConfiguration> getAll(@Valid @BeanParam PageParameters pageParams);

    static final String CREATE_NEW_DESC = "Creates a new build config.";

    /**
     * {@value CREATE_NEW_DESC}
     *
     * @param buildConfig
     * @return
     */
    @Operation(
            summary = CREATE_NEW_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = ENTITY_CREATED_CODE,
                            description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
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
    BuildConfiguration createNew(@NotNull BuildConfiguration buildConfig);

    static final String GET_SPECIFIC_DESC = "Gets a specific build config.";

    /**
     * {@value GET_SPECIFIC_DESC}
     *
     * @param id {@value BC_ID}
     * @return
     */
    @Operation(
            summary = GET_SPECIFIC_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON) // workaround for PATCH support
    BuildConfiguration getSpecific(@Parameter(description = BC_ID) @PathParam("id") String id);

    static final String UPDATE_DESC = "Updates an existing build config.";

    /**
     * {@value UPDATE_DESC}
     *
     * @param id {@value BC_ID}
     * @param buildConfig
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
    void update(@Parameter(description = BC_ID) @PathParam("id") String id, @NotNull BuildConfiguration buildConfig);

    static final String PATCH_SPECIFIC_DESC = "Patch a specific build config.";

    /**
     * {@value PATCH_SPECIFIC_DESC}
     *
     * @param id {@value BC_ID}
     * @param buildConfig
     * @return
     */
    @Operation(
            summary = PATCH_SPECIFIC_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
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
    BuildConfiguration patchSpecific(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            BuildConfiguration buildConfig);

    static final String TRIGGER_DESC = "Triggers a build of a specific build config.";

    /**
     * {@value TRIGGER_DESC}
     *
     * @param id {@value BC_ID}
     * @param buildParams
     * @return
     */
    @Operation(
            summary = TRIGGER_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = ACCEPTED_CODE,
                            description = ACCEPTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Build.class))),
                    @ApiResponse(
                            responseCode = CONFLICTED_CODE,
                            description = CONFLICTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/build")
    Build trigger(@Parameter(description = BC_ID) @PathParam("id") String id, @BeanParam BuildParameters buildParams);

    static final String GET_BUILDS_DESC = "Get all builds associated with this build config.";

    /**
     * {@value GET_BUILDS_DESC}
     *
     * @param id {@value BC_ID}
     * @param pageParams
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
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParams,
            @BeanParam BuildsFilterParameters buildsFilter);

    static final String CLONE_DESC = "Clones an existing build config.";

    /**
     * {@value CLONE_DESC}
     *
     * @param id {@value BC_ID}
     * @return
     */
    @Operation(
            summary = CLONE_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = ENTITY_CREATED_CODE,
                            description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @RespondWithStatus(Response.Status.CREATED)
    @Path("/{id}/clone")
    BuildConfiguration clone(@Parameter(description = BC_ID) @PathParam("id") String id);

    static final String GET_GROUP_CONFIGS_DESC = "Gets group configs associated with the specified build config.";

    /**
     * {@value GET_GROUP_CONFIGS_DESC}
     *
     * @param id {@value BC_ID}
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_GROUP_CONFIGS_DESC,
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
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParams);

    static final String GET_DEPENDENCIES_DESC = "Get the direct dependencies of the specified build config.";

    /**
     * {@value GET_DEPENDENCIES_DESC}
     *
     * @param id {@value BC_ID}
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_DEPENDENCIES_DESC,
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
    @Path("/{id}/dependencies")
    Page<BuildConfiguration> getDependencies(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParams);

    static final String GET_DEPENDANTS_DESC = "Get the direct dependants of the specified build config.";

    /**
     * {@value GET_DEPENDANTS_DESC}
     *
     * @param id {@value BC_ID}
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_DEPENDANTS_DESC,
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
    @Path("/{id}/dependants")
    Page<BuildConfiguration> getDependants(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParams);

    static final String ADD_DEPENDENCY_DESC = "Adds a dependency to the specified build config.";
    static final String DEPENDENCY_ADD_DESC = "Depenendency to be added to the build config. Only id is important.";

    /**
     * {@value ADD_DEPENDENCY_DESC}
     *
     * @param id {@value BC_ID}
     * @param dependency {@value DEPENDENCY_ADD_DESC}
     */
    @Operation(
            summary = ADD_DEPENDENCY_DESC,
            responses = { @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("/{id}/dependencies")
    void addDependency(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Parameter(description = DEPENDENCY_ADD_DESC) BuildConfigurationRef dependency);

    static final String REMOVE_DEPENDECY_DESC = "Removes a dependency from the specified build config.";
    static final String DEPENDENCY_REMOVE_DESC = "Depenendency to be removed from the build config. Only id is important.";

    /**
     * {@value REMOVE_DEPENDECY_DESC}
     *
     * @param id {@value BC_ID}
     * @param dependencyId {@value DEPENDENCY_REMOVE_DESC}
     */
    @Operation(
            summary = REMOVE_DEPENDECY_DESC,
            responses = { @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @DELETE
    @Path("/{id}/dependencies/{depId}")
    void removeDependency(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Parameter(description = DEPENDENCY_REMOVE_DESC) @PathParam("depId") String dependencyId);

    static final String GET_REVISIONS_DESC = "Gets audited revisions of this build config.";

    /**
     * {@value GET_REVISIONS_DESC}
     *
     * @param id {@value BC_ID}
     * @param pageParams
     * @return
     */
    @Operation(
            summary = GET_REVISIONS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfigRevisionPage.class))),
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
    Page<BuildConfigurationRevision> getRevisions(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParams);

    static final String CREATE_REVISION_DESC = "Creates new build config revision.";
    static final String CREATE_REVISION_DESC2 = "This endpoint can be used for updating build config while returning the new revision.";

    /**
     * {@value CREATE_REVISION_DESC} {@value CREATE_REVISION_DESC2}
     *
     * @param id {@value BC_ID}
     * @param buildConfig
     * @return
     */
    @Operation(
            summary = CREATE_REVISION_DESC,
            description = CREATE_REVISION_DESC2,
            responses = {
                    @ApiResponse(
                            responseCode = ENTITY_CREATED_CODE,
                            description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfigurationRevision.class))),
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
    @Path("/{id}/revisions")
    BuildConfigurationRevision createRevision(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            BuildConfiguration buildConfig);

    static final String GET_REVISION_DESC = "Get specific audited revision of this build config.";

    /**
     * {@value GET_REVISION_DESC}
     *
     * @param id {@value BC_ID}
     * @param rev {@value REV}
     * @return
     */
    @Operation(
            summary = GET_REVISION_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfigurationRevision.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/revisions/{rev}")
    BuildConfigurationRevision getRevision(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Parameter(description = REV) @PathParam("rev") int rev);

    static final String TRIGGER_REVISION_DESC = "Triggers a build of a build config in a specific revision.";

    /**
     * {@value TRIGGER_REVISION_DESC}
     *
     * @param id {@value BC_ID}
     * @param rev {@value REV}
     * @param buildParams
     * @return
     */
    @Operation(
            summary = TRIGGER_REVISION_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = ACCEPTED_CODE,
                            description = ACCEPTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Build.class))),
                    @ApiResponse(
                            responseCode = CONFLICTED_CODE,
                            description = CONFLICTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/revisions/{rev}/build")
    Build triggerRevision(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Parameter(description = REV) @PathParam("rev") int rev,
            @BeanParam BuildParameters buildParams);

    static final String RESTORE_REVISION_DESC = "Restores a build config to a specific audited revision";

    /**
     * {@value RESTORE_REVISION_DESC}
     *
     * @param id {@value BC_ID}
     * @param rev {@value REV}
     * @return
     */
    @Operation(
            summary = RESTORE_REVISION_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("/{id}/revisions/{rev}/restore")
    BuildConfiguration restoreRevision(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @Parameter(description = REV) @PathParam("rev") int rev);

    static final String CREATE_WITH_SCM_DESC = "Starts a task of creating a new build config with a given SCM URL.";
    static final String CREATE_WITH_SCM_DESC2 = "The given SCM URL is automatically analyzed and if it's an external "
            + "URL the content of the SCM repository is cloned into an internal repository.";

    /**
     * {@value CREATE_WITH_SCM_DESC} {@value CREATE_WITH_SCM_DESC2}
     *
     * @param request
     * @return
     */
    @Operation(
            summary = CREATE_WITH_SCM_DESC,
            description = CREATE_WITH_SCM_DESC2,
            responses = {
                    @ApiResponse(
                            responseCode = ACCEPTED_CODE,
                            description = BUILD_CONFIG_CREATING,
                            content = @Content(schema = @Schema(implementation = BuildConfigCreationResponse.class))),
                    @ApiResponse(
                            responseCode = ENTITY_CREATED_CODE,
                            description = BUILD_CONFIG_CREATED,
                            content = @Content(schema = @Schema(implementation = BuildConfigCreationResponse.class))),
                    @ApiResponse(
                            responseCode = CONFLICTED_CODE,
                            description = CONFLICTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("/create-with-scm")
    BuildConfigCreationResponse createWithSCM(BuildConfigWithSCMRequest request);

    static final String GET_SUPPORTED_PARAMS_DESC = "Provides list of supported parameters.";
    static final String GET_SUPPORTED_PARAMS_DESC2 = "Provides list of parameters supported by core, there can be also "
            + "other parameters not known by core.";

    /**
     * {@value GET_SUPPORTED_PARAMS_DESC} {@value GET_SUPPORTED_PARAMS_DESC2}
     *
     * @return
     */
    @Operation(
            summary = GET_SUPPORTED_PARAMS_DESC,
            description = GET_SUPPORTED_PARAMS_DESC2,
            responses = { @ApiResponse(
                    responseCode = SUCCESS_CODE,
                    description = SUCCESS_DESCRIPTION,
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = org.jboss.pnc.dto.response.Parameter.class)))) })
    @GET
    @Path("/supported-parameters")
    Set<org.jboss.pnc.dto.response.Parameter> getSupportedParameters();

    static final String GET_DEFAULT_ALIGNMENT_PARAMS_DESC = "Provides string of default alignment parameters.";
    static final String GET_DEFAULT_ALIGNMENT_PARAMS_DESC2 = "Provides default parameters for build config "
            + "according to a build type chosen by a user.";

    /**
     * {@value GET_DEFAULT_ALIGNMENT_PARAMS_DESC} {@value GET_DEFAULT_ALIGNMENT_PARAMS_DESC2}
     *
     * @param buildType {@value B_TYPE}
     * @return
     */
    @Operation(
            summary = GET_DEFAULT_ALIGNMENT_PARAMS_DESC,
            description = GET_DEFAULT_ALIGNMENT_PARAMS_DESC2,
            responses = { @ApiResponse(
                    responseCode = SUCCESS_CODE,
                    description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = AlignmentParameters.class))) })
    @GET
    @Path("/default-alignment-parameters/{buildType}")
    AlignmentParameters getBuildTypeDefaultAlignmentParameters(
            @Parameter(description = B_TYPE) @PathParam("buildType") String buildType);

}
