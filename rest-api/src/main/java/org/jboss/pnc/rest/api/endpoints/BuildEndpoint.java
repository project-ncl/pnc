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

import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.requests.BuildPushRequest;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.rest.api.parameters.BuildAttributeParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerGraphs.BuildsGraph;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ArtifactPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.BuildConfigurationRevisionSingleton;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.BuildSingleton;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ACCEPTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_DELETED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_DELETED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Builds")
@Path("/builds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BuildEndpoint{
    static final String B_ID = "ID of the build";

    @Operation(summary = "Gets all builds.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    Response getAll(@BeanParam PageParameters pageParams,
            @BeanParam BuildsFilterParameters filterParams,
            @BeanParam BuildAttributeParameters attributes);

    @Operation(summary = "Gets specific build.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildSingleton.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}")
    Response getSpecific(@Parameter(description = B_ID) @PathParam("id") int id);

    @Operation(summary = "Delete specific temporary build.",
            description = "The operation is async, for the result subscribe to 'build-records#delete' events with optional qualifier buildRecord.id.", // TODO buildRecord.id. ??
            responses = {
                @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}")
    Response delete(@Parameter(description = B_ID) @PathParam("id") int id);

    @Operation(summary = "Gets artifacts built in a specific build.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ArtifactPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/artifacts/built")
    Response getBuiltArtifacts(
            @Parameter(description = B_ID) @PathParam("id") int id,
            @BeanParam PageParameters pageParameters);

    @Operation(summary = "Gets dependency artifacts for specific build.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ArtifactPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/artifacts/dependencies")
    Response getDependencyArtifacts(
            @Parameter(description = B_ID) @PathParam("id") int id,
            @BeanParam PageParameters pageParameters);

    @Operation(summary = "Add attribute to a specific build.",
            responses = {
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/attributes")
    Response addAttribute(
            @Parameter(description = B_ID) @PathParam("id") int id,
            @Parameter(description = "Attribute key", required = true) @QueryParam("key") String key,
            @Parameter(description = "Attribute value", required = true) @QueryParam("value") String value);

    @Operation(summary = "Remove attribute from a specific build.",
            responses = {
                @ApiResponse(responseCode = ENTITY_DELETED_CODE, description = ENTITY_DELETED_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}/attributes")
    Response removeAttribute(
            @Parameter(description = B_ID) @PathParam("id") int id,
            @Parameter(description = "Attribute key", required = true) @QueryParam("key") String key);

    @Operation(summary = "Get Brew push result for specific build.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPushResult.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/brew-push")
    Response getPushResult(@Parameter(description = B_ID) @PathParam("id") int id);

    @Operation(summary = "Push build to Brew.",
            responses = {
                @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION,
                    content = @Content(array = @ArraySchema( schema = @Schema(implementation = BuildPushResult.class)))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/brew-push")
    Response push(BuildPushRequest buildRecordPushRequest);

    @Operation(summary = "Cancels push of build to Brew.",
            responses = {
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_CODE),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = "Can not find any Brew push in progress."),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}/brew-push")
    public Response cancelPush(@Parameter(description = B_ID) @PathParam("id") int id);

    @Operation(summary = "Notifies that the Brew push finished.",
            tags = "Internal",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPushResult.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/brew-push/complete/")
    public Response completePush(
            @Parameter(description = B_ID) @PathParam("id") int id,
            BuildPushResult buildRecordPushResult);

    @Operation(summary = "Gets the build config revision for specific build.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationRevisionSingleton.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/build-configuration-revision")
    Response getBuildConfigurationRevision(@Parameter(description = B_ID) @PathParam("id") int id);

    @Operation(summary = "Cancel running build.",
            responses = {
                @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/cancel")
    Response cancel(@Parameter(description = B_ID) @PathParam("id") int id);

    @Operation(summary = "Gets dependency graph for a build.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildsGraph.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/dependency-graph")
    Response getDependencyGraph(@Parameter(description = B_ID) @PathParam("id") int id);

    @Operation(summary = "Gets alignment logs for specific build.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/logs/align")
    @Produces(MediaType.TEXT_PLAIN)
    Response getAlignLogs(@Parameter(description = B_ID) @PathParam("id") int id);


    @Operation(summary = "Gets build logs for specific build.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/logs/build")
    @Produces(MediaType.TEXT_PLAIN)
    Response getBuildLogs(@Parameter(description = B_ID) @PathParam("id") int id);

    @Operation(summary = "Gets ssh credentials to log into the build pod.",
            description = "This GET requests require authentication",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = SSHCredentials.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/ssh-credentials/{id}") // TODO: Check that we can change to /{id}/ssh-credentials and keep auth: ("The path for the endpoint is not restful to be able to authenticate this GET request only.")
    Response getSshCredentials(@Parameter(description = B_ID) @PathParam("id") int id);


}
