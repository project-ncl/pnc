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
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildConfigurationRevisionPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.BuildConfigurationRevisionSingleton;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildConfigurationPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.GroupConfigPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.BuildConfigurationSingleton;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.BuildSingleton;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ProductVersionPage;

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

import java.util.Map;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;
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
@Path("/build-configurations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BuildConfigurationEndpoint {

    @Operation(summary = "Gets all Build Configurations",
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
    public Response getAll(@BeanParam PageParameters pageParameters);

    @Operation(summary = "Creates a new Build Configuration.",
            responses = {
                @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    public Response createNew(BuildConfiguration buildConfiguration);

    @Operation(summary = "There can be also other supported parameters not know by core.",
            responses = {
            @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                content = @Content(schema = @Schema(implementation = Map.class))),
    })
    @GET
    @Path("/supported-generic-parameters")
    public Response getSupportedGenericParameters();

    @Operation(summary = "Gets a specific Build Configuration",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationSingleton.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Updates an existing Build Configuration",
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
    public Response update(@Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id,
            BuildConfiguration buildConfiguration);

    @Operation(summary = "Updates an existing Build Configuration and returns BuildConfigurationRevision entity",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationRevisionSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/update-and-get-audited")
    public Response updateAndGetAudited(@Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id,
            BuildConfiguration buildConfiguration);

    @Operation(summary = "Removes a specific Build Configuration",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Clones an existing Build Configuration",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationSingleton.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationSingleton.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationSingleton.class)))
    })
    @POST
    @Path("/{id}/clone")
    public Response clone(@Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Triggers a build of a specific Build Configuration",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildSingleton.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/build")
    public Response trigger(@Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @Parameter(description = "Optional Callback URL") @QueryParam("callbackUrl") String callbackUrl,
            @Parameter(description = "Is it a temporary build or a standard build?") @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @Parameter(description = "Should we force the rebuild?") @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @Parameter(description = "Should we build also dependencies of this BuildConfiguration?") @QueryParam("buildDependencies") @DefaultValue("true") boolean buildDependencies,
            @Parameter(description = "Should we keep the build container running, if the build fails?") @QueryParam("keepPodOnFailure") @DefaultValue("false") boolean keepPodOnFailure,
            @Parameter(description = "Should we add a timestamp during the alignment? Valid only for temporary builds.") @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment);


    @Operation(summary = "Triggers a build of a specific Build Configuration in a specific revision",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildSingleton.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/revisions/{rev}/build")
    public Response triggerAudited(@Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @Parameter(description = "Revision of a Build Configuration", required = true) @PathParam("rev") Integer rev,
            @Parameter(description = "Optional Callback URL") @QueryParam("callbackUrl") String callbackUrl,
            @Parameter(description = "Is it a temporary build or a standard build?") @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @Parameter(description = "Should we force the rebuild?") @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @Parameter(description = "Should we build also dependencies of this BuildConfiguration?") @QueryParam("buildDependencies") @DefaultValue("true") boolean buildDependencies,
            @Parameter(description = "Should we keep the build container running, if the build fails?") @QueryParam("keepPodOnFailure") @DefaultValue("false") boolean keepPodOnFailure,
            @Parameter(description = "Should we add a timestamp during the alignment? Valid only for temporary builds.") @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment);

    @Operation(summary = "Gets all Build Configurations of a Project",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class)))
    })
    @GET
    @Path("/projects/{projectId}")
    public Response getAllByProjectId(@BeanParam PageParameters pageParameters,
            @Parameter(description = "Project id", required = true) @PathParam("projectId") Integer projectId);

    @Operation(summary = "Gets all Build Configurations of a Product",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class)))
    })
    @GET
    @Path("/products/{productId}")
    public Response getAllByProductId(@BeanParam PageParameters pageParameters,
            @Parameter(description = "Product id", required = true) @PathParam("productId") Integer productId);

    @Operation(summary = "Gets all Build Configurations of the Specified Product Version",
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
    @Path("/products/{productId}/product-versions/{versionId}")
    public Response getAllByProductVersionId(@BeanParam PageParameters pageParameters,
            @Parameter(description = "Product id", required = true) @PathParam("productId") Integer productId,
            @Parameter(description = "Product Version id", required = true) @PathParam("versionId") Integer versionId);

    @Operation(summary = "Get the direct dependencies of the specified configuration",
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
    @Path("/{id}/dependencies")
    public Response getDependencies(@BeanParam PageParameters pageParameters,
            @Parameter(description = "Build configuration id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Adds a dependency to the specified config",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/dependencies")
    public Response addDependency(@Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id,
            BuildConfiguration dependency);

    @Operation(summary = "Removes a configuration from the specified config set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}/dependencies/{dependencyId}")
    public Response removeDependency(
            @Parameter(description = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @Parameter(description = "Build configuration id", required = true) @PathParam("dependencyId") Integer dependencyId);

    @Operation(summary = "Gets BuildConfiguration Sets associated with the specified BuildConfiguration",
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
    @Path("/{id}/build-configuration-sets")
    public Response getGroupConfigs(
            @BeanParam PageParameters pageParameters,
            @Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id);

    /**
     * @deprecated use the productVersionId field instead
     */
    @Operation(summary = "Get associated Product Versions of the specified Configuration",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductVersionPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductVersionPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/product-versions")
    @Deprecated
    public Response getProductVersions(@BeanParam PageParameters pageParameters,
            @Parameter(description = "Build configuration id", required = true) @PathParam("id") Integer id);

    /**
     * @deprecated use the productVersionId field instead
     */
    @Operation(summary = "Associates a product version to the specified config",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    @Path("/{id}/product-versions")
    @Deprecated
    public Response addProductVersion(
            @Parameter(description = "Build Configuration id", required = true) @PathParam("id") Integer id,
            ProductVersion productVersion);

    /**
     * @deprecated use the productVersionId field instead
     */
    @Operation(summary = "Removes a product version from the specified config set",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DELETE
    @Path("/{id}/product-versions/{productVersionId}")
    @Deprecated
    public Response removeProductVersion(
            @Parameter(description = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @Parameter(description = "Product version id", required = true) @PathParam("productVersionId") Integer productVersionId);

    @Operation(summary = "Gets audited revisions of this build configuration",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationRevisionPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationRevisionPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/revisions")
    public Response getRevisions(@Parameter(description = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @Parameter(description = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @Parameter(description = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @Parameter(description = "Build configuration id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Get specific audited revision of this build configuration",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationRevisionSingleton.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildConfigurationRevisionSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/revisions/{rev}")
    public Response getRevision(@Parameter(description = "Build configuration id", required = true) @PathParam("id") Integer id,
            @Parameter(description = "Build configuration rev", required = true) @PathParam("rev") Integer rev);

    @Operation(summary = "Get all build record associated with this build configuration, returns empty list if no build records are found",
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
    public Response getBuildRecords(
            @BeanParam PageParameters pageParameters,
            @Parameter(description = "Build configuration id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Get latest build record associated with this build configuration, returns no content if no build records are found",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/build-records/latest")
    public Response getLatestBuild(@Parameter(description = "Build configuration id", required = true) @PathParam("id") Integer id);

    //TODO To be removed after testing, will be available via pnc-rest/rest/builds?q=buildConfigurationAuditedId==1
    @Operation(summary = "Get all Builds (running and archived) associated with this Build Configuration, returns empty list if no build records are found",
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
    @Path("/{id}/builds")
    public Response getBuilds(
            @BeanParam PageParameters pageParameters,
            @Parameter(description = "Build configuration id", required = true) @PathParam("id") Integer id);

}
