/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.dto.requests.BuildPushRequest;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.annotation.RespondWithStatus;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerGraphs.BuildsGraph;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ArtifactPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPushResultPage;

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

import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_DESCRIPTION;
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

    @Operation(
            summary = "Gets all builds.",
            description = "Query by attribute: when the attributes are specified only the completed builds are searched. "
                    + "The query format is: attribute=KEY:VALUE&attribute=KEY2:VALUE2 "
                    + "which translates to 'where KEY=VALUE AND KEY2=VALUE2' "
                    + "To search for the records without certain key the key must be prefixed with '!': attribute=!KEY",
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

    @Operation(
            summary = "Gets specific build.",
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

    @Operation(
            summary = "Delete a specific temporary build.",
            description = "Operation is async. Once completed, a callback can be sent with a JSON body containing information "
                    + "about the operation completion using object org.jboss.pnc.dto.DeleteOperationResult",
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
            @Parameter(description = "Optional Callback URL") @QueryParam("callback") String callback);

    @Operation(
            summary = "Updates an existing build.",
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

    @Operation(
            summary = "Gets artifacts built in a specific build.",
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

    @Operation(
            summary = "[role:admin] Set built artifacts on the BuildRecord. Note that operation replaces existing collection!",
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
            @Parameter(description = "List of artifact ids") List<String> artifactIds);

    @Operation(
            summary = "Gets dependency artifacts for specific build.",
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

    @Operation(
            summary = "[role:admin] Set dependent artifacts on the BuildRecord. Note that operation replaces existing collection!",
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
            @Parameter(description = "List of artifact ids") List<String> artifactIds);

    @Operation(
            summary = "Redirects to the SCM archive link",
            responses = {
                    @ApiResponse(responseCode = MOVED_TEMPORARILY_CODE, description = MOVED_TEMPORARILY_DESCRIPTION),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION) })
    @GET
    @Path("/{id}/scm-archive")
    Response getInternalScmArchiveLink(@Parameter(description = B_ID) @PathParam("id") String id);

    @Operation(
            summary = "Add attribute to a specific build.",
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
            @Parameter(
                    description = "Attribute key. The key must match '[a-zA-Z_0-9]+'.",
                    required = true) @QueryParam("key") String key,
            @Parameter(description = "Attribute value", required = true) @QueryParam("value") String value);

    @Operation(
            summary = "Remove attribute from a specific build.",
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
            @Parameter(description = "Attribute key", required = true) @QueryParam("key") String key);

    @Operation(
            summary = "Get Brew push result for specific build.",
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

    @Operation(
            summary = "Push build to Brew.",
            responses = {
                    @ApiResponse(
                            responseCode = ACCEPTED_CODE,
                            description = ACCEPTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildPushResultPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = FORBIDDEN_CODE,
                            description = FORBIDDEN_PUSH_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildPushResultPage.class))),
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
    BuildPushResult push(@Parameter(description = B_ID) @PathParam("id") String id, BuildPushRequest buildPushRequest);

    @Operation(
            summary = "Cancels push of build to Brew.",
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

    @Operation(
            summary = "Notifies that the Brew push finished.",
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

    @Operation(
            summary = "Gets the build config revision for specific build.",
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
    BuildConfigurationRevision getBuildConfigurationRevision(@Parameter(description = B_ID) @PathParam("id") String id);

    @Operation(
            summary = "Cancel running build.",
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

    @Operation(
            summary = "Gets dependency graph for a build.",
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

    @Operation(
            summary = "Gets alignment logs for specific build.",
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

    @Operation(
            summary = "Gets build logs for specific build.",
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

    @Operation(
            summary = "Gets ssh credentials to log into the build pod.",
            description = "This GET requests require authentication",
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
    @Path("/ssh-credentials/{id}") // TODO: Check that we can change to /{id}/ssh-credentials and keep auth: ("The path
                                   // for the endpoint is not restful to be able to authenticate this GET request
                                   // only.")
    SSHCredentials getSshCredentials(@Parameter(description = B_ID) @PathParam("id") String id);

    @Operation(
            summary = "Gets the Build Records produced from the BuildConfiguration by name.",
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
            @Parameter(description = "Log search string") @QueryParam("search") String search,
            @Valid @BeanParam PageParameters pageParameters);
}
