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

    @Operation(summary = "Gets all build configs.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    Page<BuildConfiguration> getAll(@BeanParam PageParameters pageParams);

    @Operation(summary = "Creates a new build config.",
            responses = {
                @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @RespondWithStatus(Response.Status.CREATED)
    BuildConfiguration createNew(@NotNull BuildConfiguration buildConfiguration);

    @Operation(summary = "Gets a specific build config.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON) //workaround for PATCH support
    BuildConfiguration getSpecific(@Parameter(description = BC_ID) @PathParam("id") int id);

    @Operation(summary = "Updates an existing build config.",
            responses = {
                @ApiResponse(responseCode = ENTITY_UPDATED_CODE, description = ENTITY_UPDATED_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PUT
    @Path("/{id}")
    void update(@Parameter(description = BC_ID) @PathParam("id") int id,
                @NotNull BuildConfiguration buildConfiguration);

    @Operation(summary = "Patch a specific build config.",
            responses = {
                    @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    BuildConfiguration patchSpecific(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            BuildConfiguration buildConfiguration);

    @Operation(summary = "Removes a specific build config.",
            responses = {
                @ApiResponse(responseCode = ENTITY_DELETED_CODE, description = ENTITY_DELETED_DESCRIPTION),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}")
    void deleteSpecific(@Parameter(description = BC_ID) @PathParam("id") int id);

    @Operation(summary = "Triggers a build of a specific build config.",
            responses = {
                @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = Build.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/build")
    Build trigger(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            @BeanParam BuildParameters buildParams);

    @Operation(summary = "Get all builds associated with this build config.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/builds")
    Page<Build> getBuilds(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            @BeanParam PageParameters pageParams,
            @BeanParam BuildsFilterParameters buildsFilter);

    @Operation(summary = "Clones an existing build config.",
            responses = {
                @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @RespondWithStatus(Response.Status.CREATED)
    @Path("/{id}/clone")
    BuildConfiguration clone(@Parameter(description = BC_ID) @PathParam("id") int id);

    @Operation(summary = "Gets group configs associated with the specified build config.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupConfigPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/group-configs")
    Page<GroupConfiguration> getGroupConfigs(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            @BeanParam PageParameters pageParams);

    @Operation(summary = "Get the direct dependencies of the specified build config.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/dependencies")
    Page<BuildConfiguration> getDependencies(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            @BeanParam PageParameters pageParams);

    @Operation(summary = "Adds a dependency to the specified build config.",
            responses = {
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/dependencies")
    void addDependency(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            BuildConfigurationRef dependency);

    @Operation(summary = "Removes a dependency from the specified build config.",
            responses = {
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}/dependencies/{depId}")
    void removeDependency(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            @Parameter(description = "ID of the dependency") @PathParam("depId") int dependencyId);
    
    @Operation(summary = "Gets audited revisions of this build config.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigRevisionPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/revisions")
    Page<BuildConfigurationRevision> getRevisions(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            @BeanParam PageParameters pageParams);

    @Operation(summary = "Creates new build config revision.",
            description = "This endpoint can be used for updating build config while returning the new revision.",
            responses = {
                @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationRevision.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @RespondWithStatus(Response.Status.CREATED)
    @Path("/{id}/revisions")
    BuildConfigurationRevision createRevision(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            BuildConfiguration buildConfiguration);

    @Operation(summary = "Get specific audited revision of this build config.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationRevision.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/revisions/{rev}")
    BuildConfigurationRevision getRevision(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            @Parameter(description = REV) @PathParam("rev") int rev);

    @Operation(summary = "Triggers a build of a build config in a specific revision.",
            responses = {
                @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = Build.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/{id}/revisions/{rev}/build")
    Build triggerRevision(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            @Parameter(description = REV) @PathParam("rev") int rev,
            @BeanParam BuildParameters buildParams);

    @Operation(summary = "Restores a build config to a specific audited revision",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfiguration.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/revisions/{rev}/restore")
    BuildConfiguration restoreRevision(
            @Parameter(description = BC_ID) @PathParam("id") int id,
            @Parameter(description = REV) @PathParam("rev") int rev);

    @Operation(summary = "Starts a task of creating a new build config with a given SCM URL.",
            description = "The given SCM URL is automatically analyzed and if it's an external URL"
                    + "the content of the SCM repository is cloned into an internal repository.",
            responses = {
                @ApiResponse(responseCode = ACCEPTED_CODE, description = ACCEPTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigCreationResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @RespondWithStatus(Response.Status.ACCEPTED)
    @Path("/create-with-scm")
    BuildConfigCreationResponse createWithSCM(BuildConfigWithSCMRequest request);

    @Operation(summary = "Provides list of supported parameters.",
            description = "Provides list of parameters supported by core, there can be also other parameters not known by core.",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = org.jboss.pnc.dto.response.Parameter.class)))),
    })
    @GET
    @Path("/supported-parameters")
    Set<org.jboss.pnc.dto.response.Parameter> getSupportedParameters();

}
