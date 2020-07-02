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
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RunningBuildCount;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.annotation.RespondWithStatus;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerGraphs.BuildsGraph;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ArtifactPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPage;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.List;

import org.jboss.pnc.rest.configuration.SwaggerConstants;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CALLBACK_URL;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_DELETED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_DELETED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_UPDATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_UPDATED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.MOVED_TEMPORARILY_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.MOVED_TEMPORARILY_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_PUSH_DESCRIPTION;

@Tag(name = "Builds")
@Path("/builds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface BuildEndpoint {
    static final String B_ID = "ID of the build";
    static final String BUILD_STATUS = "Status of the build";
    static final String ARTIFACT_IDS = "List of artifact ids";
    static final String ATTRIBUTE_KEY = "Attribute key. The key must match '[a-zA-Z_0-9]+'.";
    static final String ATTRIBUTE_VALUE = "Attribute value";

    static final String GET_ALL_DESC = "Gets all builds.";
    static final String GET_ALL_DESC2 = "Query by attribute: when the attributes are specified only the completed "
            + "builds are searched. The query format is: attribute=KEY:VALUE&attribute=KEY2:VALUE2 which translates to "
            + "'where KEY=VALUE AND KEY2=VALUE2' To search for the records without certain key the key must be "
            + "prefixed with '!': attribute=!KEY";

    /**
     * {@value GET_ALL_DESC} {@value GET_ALL_DESC2}
     *
     * @param pageParams
     * @param filterParams
     * @param attributes
     * @return
     */
    @Operation(
            summary = GET_ALL_DESC,
            description = GET_ALL_DESC2,
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
    Page<Build> getAll(
            @Valid @BeanParam PageParameters pageParams,
            @BeanParam BuildsFilterParameters filterParams,
            @QueryParam("attribute") List<String> attributes);

    static final String GET_SPECIFIS_DESC = "Gets specific build.";

    /**
     * {@value GET_SPECIFIS_DESC}
     *
     * @param id {@value B_ID}
     * @return
     */
    @Operation(
            summary = GET_SPECIFIS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Build.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}")
    Build getSpecific(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String DELETE_DESC = "Delete a specific temporary build.";
    static final String DELETE_DESC2 = "Operation is async. Once completed, a callback can be sent with a JSON body "
            + "containing information about the operation completion using object "
            + "org.jboss.pnc.dto.DeleteOperationResult";

    /**
     * {@value DELETE} {@value DELETE_DESC2} {@value SwaggerConstants#REQUIRES_ADMIN}
     *
     * @param id {@value B_ID}
     * @param callback {@value SwaggerConstants#CALLBACK_URL}
     */
    @Operation(
            summary = "[role:admin] " + DELETE_DESC,
            description = DELETE_DESC2,
            tags = "internal",
            responses = { @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @DELETE
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}")
    void delete(
            @Parameter(description = B_ID) @PathParam("id") String id,
            @Parameter(description = CALLBACK_URL) @QueryParam("callback") String callback);

    static final String UPDATE_DESC = "Updates an existing build.";

    /**
     * {@value UPDATE_DESC} {@value SwaggerConstants#REQUIRES_ADMIN}
     *
     * @param id {@value B_ID}
     * @param build
     */
    @Operation(
            summary = "[role:admin] " + UPDATE_DESC,
            tags = "internal",
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
    void update(@Parameter(description = B_ID) @PathParam("id") String id, @NotNull Build build);

    static final String GET_BUILT_ARTIFACTS_DESC = "Gets artifacts built in a specific build.";

    /**
     * {@value GET_BUILT_ARTIFACTS_DESC}
     *
     * @param id {@value B_ID}
     * @param pageParameters
     * @return
     */
    @Operation(
            summary = GET_BUILT_ARTIFACTS_DESC,
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
    @Path("/{id}/artifacts/built")
    Page<Artifact> getBuiltArtifacts(
            @Parameter(description = B_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

    static final String SET_BUILT_ARTIFACTS = "Set built artifacts on the Build. Note that operation replaces existing collection!";

    /**
     * {@value SET_BUILT_ARTIFACTS} {@value SwaggerConstants#REQUIRES_ADMIN}
     *
     * @param id {@value B_ID}
     * @param artifactIds {@value ARTIFACT_IDS}
     */
    @Operation(
            summary = "[role:admin] " + SET_BUILT_ARTIFACTS,
            tags = "internal",
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
    @PUT
    @Path("/{id}/artifacts/built")
    void setBuiltArtifacts(
            @Parameter(description = B_ID) @PathParam("id") String id,
            @Parameter(description = ARTIFACT_IDS) List<String> artifactIds);

    static final String GET_DEPENDENCY_ARTIFACTS_DESC = "Gets dependency artifacts for specific build.";

    /**
     * {@value GET_DEPENDENCY_ARTIFACTS_DESC}
     *
     * @param id {@value B_ID}
     * @param pageParameters
     * @return
     */
    @Operation(
            summary = GET_DEPENDENCY_ARTIFACTS_DESC,
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
    @Path("/{id}/artifacts/dependencies")
    Page<Artifact> getDependencyArtifacts(
            @Parameter(description = B_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters);

    static final String SET_DEPENDANT_ARTIFACTS_DESC = "Set dependent artifacts on the Build. Note that operation replaces existing collection!";

    /**
     * {@value SET_DEPENDANT_ARTIFACTS_DESC} {@value SwaggerConstants#REQUIRES_ADMIN}
     *
     * @param id {@value B_ID}
     * @param artifactIds {@value ARTIFACT_IDS}
     */
    @Operation(
            summary = "[role:admin] " + SET_DEPENDANT_ARTIFACTS_DESC,
            tags = "internal",
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
    @PUT
    @Path("/{id}/artifacts/dependencies")
    void setDependentArtifacts(
            @Parameter(description = B_ID) @PathParam("id") String id,
            @Parameter(description = ARTIFACT_IDS) List<String> artifactIds);

    static final String GET_INTERNAL_SCM_ARCHIVE_DESC = "Redirects to the SCM archive link";

    /**
     * {@value GET_INTERNAL_SCM_ARCHIVE_DESC}
     *
     * @param id {@value B_ID}
     * @return
     */
    @Operation(
            summary = GET_INTERNAL_SCM_ARCHIVE_DESC,
            responses = {
                    @ApiResponse(responseCode = MOVED_TEMPORARILY_CODE, description = MOVED_TEMPORARILY_DESCRIPTION),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION) })
    @GET
    @Path("/{id}/scm-archive")
    Response getInternalScmArchiveLink(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String ADD_ATTRIBUTE_DESC = "Add attribute to a specific build.";

    /**
     * {@value ADD_ATTRIBUTE_DESC}
     *
     * @param id {@value B_ID}
     * @param key {@value ATTRIBUTE_KEY}
     * @param value {@value ATTRIBUTE_VALUE}
     */
    @Operation(
            summary = ADD_ATTRIBUTE_DESC,
            responses = { @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @RespondWithStatus(Response.Status.CREATED)
    @Path("/{id}/attributes")
    void addAttribute(
            @Parameter(description = B_ID) @PathParam("id") String id,
            @Parameter(description = ATTRIBUTE_KEY, required = true) @QueryParam("key") String key,
            @Parameter(description = ATTRIBUTE_VALUE, required = true) @QueryParam("value") String value);

    static final String REMOVE_ATTRIBUTE_DESC = "Remove attribute from a specific build.";

    /**
     * {@value REMOVE_ATTRIBUTE_DESC}
     *
     * @param id {@value B_ID}
     * @param key {@value ATTRIBUTE_KEY}
     */
    @Operation(
            summary = REMOVE_ATTRIBUTE_DESC,
            responses = { @ApiResponse(responseCode = ENTITY_DELETED_CODE, description = ENTITY_DELETED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @DELETE
    @Path("/{id}/attributes")
    void removeAttribute(
            @Parameter(description = B_ID) @PathParam("id") String id,
            @Parameter(description = ATTRIBUTE_KEY, required = true) @QueryParam("key") String key);

    static final String GET_PUSH_RESULT_DESC = "Get Brew push result for specific build.";

    /**
     * {@value GET_PUSH_RESULT_DESC}
     *
     * @param id {@value B_ID}
     * @return
     */
    @Operation(
            summary = GET_PUSH_RESULT_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildPushResult.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/brew-push")
    BuildPushResult getPushResult(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String PUSH_DESC = "Push build to Brew.";

    /**
     * {@value PUSH_DESC}
     *
     * @param id {@value B_ID}
     * @param buildPushParameters
     * @return
     */
    @Operation(
            summary = PUSH_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = ACCEPTED_CODE,
                            description = ACCEPTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildPushResult.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = FORBIDDEN_CODE,
                            description = FORBIDDEN_PUSH_DESCRIPTION,
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
    @Path("/{id}/brew-push")
    BuildPushResult push(
            @Parameter(description = B_ID) @PathParam("id") String id,
            BuildPushParameters buildPushParameters);

    static final String CANCEL_PUSH_DESC = "Cancels push of build to Brew.";

    /**
     * {@value CANCEL_PUSH_DESC}
     *
     * @param id {@value B_ID}
     */
    @Operation(
            summary = CANCEL_PUSH_DESC,
            responses = { @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = NOT_FOUND_CODE,
                            description = "Can not find any Brew push in progress."),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @DELETE
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/brew-push")
    void cancelPush(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String COMPLETE_PUSH_DESC = "Notifies that the Brew push finished.";

    /**
     * {@value COMPLETE_PUSH_DESC}
     *
     * @param id {@value B_ID}
     * @param buildPushResult
     * @return
     */
    @Operation(
            summary = COMPLETE_PUSH_DESC,
            tags = "Internal",
            responses = {
                    @ApiResponse(
                            responseCode = ENTITY_CREATED_CODE,
                            description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildPushResult.class))),
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
    @Path("/{id}/brew-push/complete")
    BuildPushResult completePush(
            @Parameter(description = B_ID) @PathParam("id") String id,
            BuildPushResult buildPushResult);

    static final String GET_BUILD_CONFIG_REVISION = "Gets the build config revision for specific build.";

    /**
     * {@value GET_BUILD_CONFIG_REVISION}
     *
     * @param id {@value B_ID}
     * @return
     */
    @Operation(
            summary = GET_BUILD_CONFIG_REVISION,
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
    @Path("/{id}/build-config-revision")
    BuildConfigurationRevision getBuildConfigRevision(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String CANCEL_DESC = "Cancel running build.";

    /**
     * {@value CANCEL_DESC}
     *
     * @param id {@value B_ID}
     */
    @Operation(
            summary = CANCEL_DESC,
            responses = { @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/cancel")
    void cancel(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String GET_DEPENDENCY_GRAPH = "Gets dependency graph for a build.";

    /**
     * {@value GET_DEPENDENCY_GRAPH}
     *
     * @param id {@value B_ID}
     * @return
     */
    @Operation(
            summary = GET_DEPENDENCY_GRAPH,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildsGraph.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/dependency-graph")
    Graph<Build> getDependencyGraph(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String GET_ALIGN_LOGS_DESC = "Gets alignment logs for specific build.";

    /**
     * {@value GET_ALIGN_LOGS_DESC}
     *
     * @param id {@value B_ID}
     * @return
     */
    @Operation(
            summary = GET_ALIGN_LOGS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/logs/align")
    @Produces(MediaType.TEXT_PLAIN)
    StreamingOutput getAlignLogs(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String GET_BUILD_LOGS_DESC = "Gets build logs for specific build.";

    /**
     * {@value GET_BUILD_LOGS_DESC}
     *
     * @param id {@value B_ID}
     * @return
     */
    @Operation(
            summary = GET_BUILD_LOGS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/logs/build")
    @Produces(MediaType.TEXT_PLAIN)
    StreamingOutput getBuildLogs(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String GET_SSH_CREDENTIALS_DESC = "Gets ssh credentials to log into the build pod.";
    static final String GET_SSH_CREDENTIALS_DESC2 = "This GET requests require authentication";

    /**
     * {@value GET_SSH_CREDENTIALS_DESC} {@value GET_SSH_CREDENTIALS_DESC2}
     *
     * @param id {@value B_ID}
     * @return
     */
    @Operation(
            summary = GET_SSH_CREDENTIALS_DESC,
            description = GET_SSH_CREDENTIALS_DESC2,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = SSHCredentials.class))),
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
    @Path("/ssh-credentials/{id}") // To allow authentication the path must be inverted (not /{id}/ssh-credentials)
    SSHCredentials getSshCredentials(@Parameter(description = B_ID) @PathParam("id") String id);

    static final String LOG_SEARCH = "Log search string";
    static final String GET_ALL_BY_STATUS_AND_LOG_CONTAINING_DESC = "Gets the Builds by given status and with specific"
            + " string in the build logs.";

    /**
     * {@value GET_ALL_BY_STATUS_AND_LOG_CONTAINING_DESC}
     *
     * @param status {@value BUILD_STATUS}
     * @param search {@value LOG_SEARCH}
     * @param pageParameters
     * @return
     */
    @Operation(
            summary = GET_ALL_BY_STATUS_AND_LOG_CONTAINING_DESC,
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
    @Path("/with-status-and-log")
    Page<Build> getAllByStatusAndLogContaining(
            @Parameter(description = BUILD_STATUS) @QueryParam("status") BuildStatus status,
            @Parameter(description = LOG_SEARCH) @QueryParam("search") String search,
            @Valid @BeanParam PageParameters pageParameters);

    static final String GET_RUNNING_COUNT_DESC = "Get count of running builds in their stages: running, waiting for dependencies, or enqueued";

    /**
     * {@value GET_RUNNING_COUNT_DESC}
     *
     * @return
     */
    @Operation(
            summary = GET_RUNNING_COUNT_DESC,
            description = GET_RUNNING_COUNT_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RunningBuildCount.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/count")
    public RunningBuildCount getCount();
}
