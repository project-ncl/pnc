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


import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildConfigurationPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.GroupBuildPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.GroupConfigPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.GroupBuildSingleton;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.GroupConfigSingleton;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
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

import javax.ws.rs.BeanParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "")
@Path("/build-configuration-sets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BuildConfigurationSetEndpoint{

    @Operation(summary = "Gets all Build Configuration Sets",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupConfigPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupConfigPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    public Response getAll(@BeanParam PageParameters pageParameters);

    @Operation(summary = "Creates a new Build Configuration Set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupConfigSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    public Response createNew(@NotNull GroupConfiguration buildConfigurationSet);

    @Operation(summary = "Gets a specific Build Configuration Set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupConfigSingleton.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupConfigSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @Parameter(description = "Build Configuration Set id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Updates an existing Build Configuration Set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PUT
    @Path("/{id}")
    public Response update(@Parameter(description = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            @NotNull GroupConfiguration buildConfigurationSet);

    @Operation(summary = "Removes a specific Build Configuration Set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@Parameter(description = "Build Configuration Set id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Gets the Configurations for the Specified Set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/build-configurations")
    public Response getConfigurations(@BeanParam PageParameters pageParameters,
            @Parameter(description = "Build Configuration Set id", required = true) @PathParam("id") Integer id);

    @PUT
    @Path("/{id}/build-configurations")
    public Response updateConfigurations(@Parameter(description = "Build Configuration Set Id", required = true) @PathParam("id") Integer id,
            List<BuildConfiguration> buildConfigurations);

    @Operation(summary = "Adds a configuration to the Specified Set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/build-configurations")
    public Response addConfiguration(
            @Parameter(description = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            BuildConfiguration buildConfig);

    @Operation(summary = "Removes a configuration from the specified config set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}/build-configurations/{configId}")
    public Response removeConfiguration(
            @Parameter(description = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @Parameter(description = "Build configuration id", required = true) @PathParam("configId") Integer configId);

    @Operation(summary = "Gets all build records associated with the contained build configurations",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/build-records")
    public Response getBuilds(
            @Parameter(description = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @BeanParam PageParameters pageParameters);

    @Operation(summary = "Builds the Configurations for the Specified Set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupBuildSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/build")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response build(
            @Parameter(description = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            @Parameter(description = "Optional Callback URL", required = false) @QueryParam("callbackUrl") String callbackUrl,
            @Parameter(description = "Is it a temporary build or a standard build?") @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @Parameter(description = "Should we force the rebuild of all build configurations?") @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @Parameter(description = "Should we add a timestamp during the alignment? Valid only for temporary builds.") @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment);

    @Operation(summary = "Builds the configurations for the Specified Set with an option to specify exact revision of a BC",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupBuildSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/build-versioned")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buildVersioned(
            @Parameter(description = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            @Parameter(description = "Optional Callback URL", required = false) @QueryParam("callbackUrl") String callbackUrl,
            @Parameter(description = "Is it a temporary build or a standard build?") @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @Parameter(description = "Should we force the rebuild of all build configurations?") @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @Parameter(description = "Should we add a timestamp during the alignment? Valid only for temporary builds.") @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment,
            GroupBuildRequest buildConfigurationAudited);

    @Operation(summary = "Get all build config set execution records associated with this build config set, returns empty list if none are found",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupBuildPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupBuildPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/build-config-set-records")
    public Response getAllGroupBuilds(
            @BeanParam PageParameters pageParameters,
            @Parameter(description = "Build config set id", required = true) @PathParam("id") Integer id);

}
