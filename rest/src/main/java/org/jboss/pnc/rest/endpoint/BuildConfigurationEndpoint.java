/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.configuration.BuildConfigurationSupportedGenericParameters;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationAuditedPage;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationAuditedSingleton;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationPage;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationSetPage;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationSingleton;
import org.jboss.pnc.rest.swagger.response.BuildRecordPage;
import org.jboss.pnc.rest.swagger.response.BuildRecordSingleton;
import org.jboss.pnc.rest.swagger.response.ProductVersionPage;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.spi.BuildScope;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Api(value = "/build-configurations", description = "Build configuration entities")
@Path("/build-configurations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationEndpoint extends AbstractEndpoint<BuildConfiguration, BuildConfigurationRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildConfigurationProvider buildConfigurationProvider;
    private BuildConfigurationSetProvider buildConfigurationSetProvider;
    private BuildTriggerer buildTriggerer;
    private BuildRecordProvider buildRecordProvider;
    private ProductVersionProvider productVersionProvider;
    private EndpointAuthenticationProvider authenticationProvider;
    @Context
    private HttpServletRequest httpServletRequest;
    
    private java.util.Map<String, String> buildConfigurationSupportedGenericParameters;


    public BuildConfigurationEndpoint() {
    }

    @Inject
    public BuildConfigurationEndpoint(
            BuildConfigurationProvider buildConfigurationProvider,
            BuildConfigurationSetProvider buildConfigurationSetProvider,
            BuildTriggerer buildTriggerer,
            BuildRecordProvider buildRecordProvider,
            ProductVersionProvider productVersionProvider,
            EndpointAuthenticationProvider authenticationProvider,
            BuildConfigurationSupportedGenericParameters supportedGenericParameters) {

        super(buildConfigurationProvider);
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildConfigurationSetProvider = buildConfigurationSetProvider;
        this.buildTriggerer = buildTriggerer;
        this.buildRecordProvider = buildRecordProvider;
        this.productVersionProvider = productVersionProvider;
        this.authenticationProvider = authenticationProvider;
        
        this.buildConfigurationSupportedGenericParameters = supportedGenericParameters
                .getSupportedGenericParameters();
    }

    @ApiOperation(value = "Gets all Build Configurations")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    public Response getAll(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(buildConfigurationProvider.getAllNonArchived(pageIndex, pageSize, sort, q));
    }

    @ApiOperation(value = "Creates a new Build Configuration.")
    @ApiResponses(value = {
            @ApiResponse(code = ENTITY_CREATED_CODE, message = ENTITY_CREATED_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response createNew(BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo)
            throws ValidationException {
        return super.createNew(buildConfigurationRest, uriInfo);
    }
    
    @GET
    @Path("/supported-generic-parameters")
    @ApiOperation(value = "Gets the minimal set of supported genericParameters and their description for the BuildConfiguration. "
            + "There can be also other supported parameters not know by core.")
    @ApiResponse(response = Map.class, code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION)
    public Response getSupportedGenericParameters() {
        return Response.ok().entity(buildConfigurationSupportedGenericParameters).build();
    }

    @ApiOperation(value = "Gets a specific Build Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Updates an existing Build Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            BuildConfigurationRest buildConfigurationRest) throws ValidationException {
        return super.update(id, buildConfigurationRest);
    }

    @ApiOperation(value = "Removes a specific Build Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id)
            throws ValidationException {
        buildConfigurationProvider.archive(id);
        return Response.ok().build();
    }

    @ApiOperation(value = "Clones an existing Build Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = BuildConfigurationSingleton.class)
    })
    @POST
    @Path("/{id}/clone")
    public Response clone(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @Context UriInfo uriInfo) throws ValidationException {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-configurations/{id}");
        int newId = buildConfigurationProvider.clone(id);
        return Response.created(uriBuilder.build(newId)).entity(new Singleton(buildConfigurationProvider.getSpecific(newId))).build();
    }

    @ApiOperation(value = "Triggers the build of a specific Build Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordSingleton.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/build")
    public Response trigger(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Optional Callback URL") @QueryParam("callbackUrl") String callbackUrl,
            @ApiParam(value = "Build scope: SINGLE, WITH_DEPENDENCIES, REBUILD.") @QueryParam("scope") @DefaultValue("WITH_DEPENDENCIES") BuildScope scope,
            @ApiParam(value = "Keep pod alive when the build fails") @QueryParam("keepPodAliveOnFailure") @DefaultValue("false") boolean keepPodAliveOnFailure,
            @ApiParam(value = "Build the unbuilt dependencies") @QueryParam("buildDependencies") @DefaultValue("false") boolean buildDependencies,
            @Context UriInfo uriInfo) throws InvalidEntityException, MalformedURLException, BuildConflictException, CoreException {

        logger.debug("Endpoint /build requested for buildConfigurationId [{}]", id);

        User currentUser = authenticationProvider.getCurrentUser(httpServletRequest);
        if (currentUser == null) {
            throw new InvalidEntityException("No such user exists to trigger builds. Before triggering builds"
                    + " user must be initialized through /users/getLoggedUser");
        }

        Integer runningBuildId = null;
        // if callbackUrl is provided trigger build accordingly
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            logger.debug("Triggering build for buildConfigurationId {} without callback URL.", id);
            runningBuildId = buildTriggerer.triggerBuild(id, currentUser, keepPodAliveOnFailure, scope);
        } else {
            logger.debug("Triggering build for buildConfigurationId {} with callback URL {}.", id, callbackUrl);
            runningBuildId = buildTriggerer.triggerBuild(id, currentUser, keepPodAliveOnFailure, scope, new URL(callbackUrl));
        }

        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-config-set-records/{id}");
        URI uri = uriBuilder.build(runningBuildId);
        return Response.ok(uri).header("location", uri).entity(new Singleton<>(buildRecordProvider.getSpecificRunning(runningBuildId))).build();
    }

    private Response validateRequiredField(String parameter, String parameterName) {
        if (parameter == null || parameter.equals("")) {
            String msg = "Missing required " + parameterName + " parameter.";
            logger.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } else {
            return null;
        }
    }


    @ApiOperation(value = "Gets all Build Configurations of a Project")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = BuildConfigurationPage.class)
    })
    @GET
    @Path("/projects/{projectId}")
    public Response getAllByProjectId(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId) {
        return fromCollection(buildConfigurationProvider.getAllForProject(pageIndex, pageSize, sort, q, projectId));
    }

    @ApiOperation(value = "Gets all Build Configurations of a Product")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = BuildConfigurationPage.class)
    })
    @GET
    @Path("/products/{productId}")
    public Response getAllByProductId(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId) {
        return fromCollection(buildConfigurationProvider.getAllForProduct(pageIndex, pageSize, sort, q, productId));
    }

    @ApiOperation(value = "Gets all Build Configurations of the Specified Product Version")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/products/{productId}/product-versions/{versionId}")
    public Response getAllByProductVersionId(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId) {
        return fromCollection(buildConfigurationProvider
                .getAllForProductAndProductVersion(pageIndex, pageSize, sort, q, productId, versionId));
    }

    @ApiOperation(value = "Get the direct dependencies of the specified configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/dependencies")
    public Response getDependencies(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildConfigurationProvider.getDependencies(pageIndex, pageSize, sort, q, id));
    }

    @ApiOperation(value = "Adds a dependency to the specified config")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/dependencies")
    public Response addDependency(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            BuildConfigurationRest dependency) throws ValidationException {
        buildConfigurationProvider.addDependency(id, dependency.getId());
        return fromEmpty();
    }

    @ApiOperation(value = "Removes a configuration from the specified config set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}/dependencies/{dependencyId}")
    public Response removeDependency(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("dependencyId") Integer dependencyId) throws ValidationException {
        buildConfigurationProvider.removeDependency(id, dependencyId);
        return fromEmpty();
    }

    @ApiOperation(value = "Gets BuildConfiguration Sets associated with the specified BuildConfiguration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSetPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationSetPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-configuration-sets")
    public Response getBuildConfigurationSets(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id) {

        return Response.ok().entity(buildConfigurationSetProvider.getAllForBuildConfiguration(pageIndex, pageSize, sort,
                q, id)).build();
    }

    /**
     * @deprecated use the productVersionId field instead
     */
    @ApiOperation(value = "Get associated Product Versions of the specified Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductVersionPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ProductVersionPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/product-versions")
    @Deprecated
    public Response getProductVersions(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(productVersionProvider.getAllForBuildConfiguration(pageIndex, pageSize, sort, q, id));
    }

    /**
     * @deprecated use the productVersionId field instead
     */
    @ApiOperation(value = "Associates a product version to the specified config")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/product-versions")
    @Deprecated
    public Response addProductVersion(
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            ProductVersionRest productVersion) throws ValidationException {        
        buildConfigurationProvider.setProductVersion(id, productVersion.getId());
        return fromEmpty();
    }

    /**
     * @deprecated use the productVersionId field instead
     */
    @ApiOperation(value = "Removes a product version from the specified config set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}/product-versions/{productVersionId}")
    @Deprecated
    public Response removeProductVersion(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Product version id", required = true) @PathParam("productVersionId") Integer productVersionId) throws ValidationException {
        buildConfigurationProvider.setProductVersion(id, null);
        return fromEmpty();
    }

    @ApiOperation(value = "Gets audited revisions of this build configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationAuditedPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationAuditedPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/revisions")
    public Response getRevisions(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildConfigurationProvider.getRevisions(pageIndex, pageSize, id));
    }

    @ApiOperation(value = "Get specific audited revision of this build configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationAuditedSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildConfigurationAuditedSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/revisions/{rev}")
    public Response getRevision(@ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Build configuration rev", required = true) @PathParam("rev") Integer rev) {
        return fromSingleton(buildConfigurationProvider.getRevision(id, rev));
    }

    @ApiOperation(value = "Get all build record associated with this build configuration, returns empty list if no build records are found")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-records")
    public Response getBuildRecords(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildRecordProvider.getAllForBuildConfiguration(pageIndex, pageSize, sort, q, id));
    }

    @ApiOperation(value = "Get latest build record associated with this build configuration, returns no content if no build records are found")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-records/latest")
    public Response getLatestBuildRecord(@ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return this.fromSingleton(buildRecordProvider.getLatestBuildRecord(id));
    }

    //TODO To be removed after testing, will be available via pnc-rest/rest/builds?q=buildConfigurationAudited.idRev.id==1
    @ApiOperation(value = "Get all BuildRecords (running and archived) associated with this Build Configuration, returns empty list if no build records are found")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/builds")
    public Response getBuilds(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildRecordProvider.getRunningAndCompletedBuildRecordsByBuildConfigurationId(pageIndex, pageSize, sort, q, id));
    }
}
