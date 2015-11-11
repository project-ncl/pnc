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
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.core.builder.coordinator.BuildCoordinator;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.core.builder.executor.BuildExecutionTask;
import org.jboss.pnc.core.builder.executor.BuildExecutor;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationAuditedSingleton;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationPage;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationSingleton;
import org.jboss.pnc.rest.swagger.response.BuildRecordPage;
import org.jboss.pnc.rest.swagger.response.BuildRecordSingleton;
import org.jboss.pnc.rest.swagger.response.ProductVersionPage;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.rest.utils.BpmNotifier;
import org.jboss.pnc.rest.utils.HibernateLazyInitializer;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.exception.BuildConflictException;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVLID_CODE;
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
    private BuildTriggerer buildTriggerer;
    private BuildRecordProvider buildRecordProvider;
    private ProductVersionProvider productVersionProvider;
    private Datastore datastore;
    private BpmNotifier bpmNotifier;
    private HibernateLazyInitializer hibernateLazyInitializer;
    private BuildCoordinator buildCoordinator;

    @Context
    private HttpServletRequest httpServletRequest;
    

    public BuildConfigurationEndpoint() {
    }

    @Inject
    public BuildConfigurationEndpoint(
            BuildConfigurationProvider buildConfigurationProvider,
            BuildTriggerer buildTriggerer,
            BuildRecordProvider buildRecordProvider,
            ProductVersionProvider productVersionProvider,
            Datastore datastore,
            BpmNotifier bpmNotifier,
            HibernateLazyInitializer hibernateLazyInitializer,
            BuildCoordinator buildCoordinator) {
        super(buildConfigurationProvider);
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildTriggerer = buildTriggerer;
        this.buildRecordProvider = buildRecordProvider;
        this.productVersionProvider = productVersionProvider;
        this.datastore = datastore;
        this.bpmNotifier = bpmNotifier;
        this.hibernateLazyInitializer = hibernateLazyInitializer;
        this.buildCoordinator = buildCoordinator;
    }

    @ApiOperation(value = "Gets all Build Configurations")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    public Response getAll(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @ApiOperation(value = "Creates a new Build Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response createNew(BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo)
            throws ValidationException {
        return super.createNew(buildConfigurationRest, uriInfo);
    }

    @ApiOperation(value = "Gets a specific Build Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id)
            throws ValidationException {
        return super.delete(id);
    }

    @ApiOperation(value = "Clones an existing Build Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = BuildConfigurationSingleton.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = BuildConfigurationSingleton.class)
    })
    @POST
    @Path("/{id}/clone")
    public Response clone(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @Context UriInfo uriInfo) throws ValidationException {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-configurations/{id}");
        int newId = buildConfigurationProvider.clone(id);
        return Response.created(uriBuilder.build(newId)).entity(buildConfigurationProvider.getSpecific(newId)).build();
    }

    @ApiOperation(value = "Triggers the build of a specific Build Configuration", response = BuildRecordSingleton.class)
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/build")
    public Response trigger(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Optional Callback URL") @QueryParam("callbackUrl") String callbackUrl,
            @ApiParam(value = "Rebuild all dependencies") @QueryParam("rebuildAll") @DefaultValue("false") boolean rebuildAll,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request) throws InvalidEntityException, MalformedURLException {
        try {

            logger.debug("Endpoint /build requested for buildConfigurationId [{}], by [{}]", id, request.getRemoteAddr());

            AuthenticationProvider authProvider = new AuthenticationProvider(httpServletRequest);
            String loggedUser = authProvider.getUserName();
            User currentUser = null;
            if(loggedUser != null && loggedUser != "") {
                currentUser = datastore.retrieveUserByUsername(loggedUser);
            }
            if(currentUser != null) {
                currentUser.setLoginToken(authProvider.getTokenString());
            }
            else{
                throw new InvalidEntityException("No such user exists to trigger builds. Before triggering builds"
                        + " user must be initialized through /users/getLoggedUser"); 
            }
            
            Integer runningBuildId = null;
            // if callbackUrl is provided trigger build accordingly
            if (callbackUrl == null || callbackUrl.isEmpty()) {
                runningBuildId = buildTriggerer.triggerBuild(id, currentUser, rebuildAll);
            } else {
                runningBuildId = buildTriggerer.triggerBuild(id, currentUser, rebuildAll, new URL(callbackUrl));
            }
            
            UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-config-set-records/{id}");
            URI uri = uriBuilder.build(runningBuildId);
            return Response.ok(uri).header("location", uri).entity(new Singleton(buildRecordProvider.getSpecificRunning(runningBuildId))).build();
        } catch (BuildConflictException e) {
            return Response.status(Response.Status.CONFLICT).entity(
                    new Singleton(buildRecordProvider.getSpecificRunning(e.getBuildTaskId()))).build();
        }
    }

    @ApiOperation(value = "Triggers the build execution only for a specific Build Configuration", response = Singleton.class)
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(code = FORBIDDEN_CODE, message = FORBIDDEN_DESCRIPTION),
    })
    @POST
    @Path("/{id}/execute-build")
    public Response build(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer buildConfigurationId,
                          @ApiParam(value = "Build Configuration revision", required = true) @QueryParam("buildConfigurationRevision") String buildConfigurationRevisionParam,
                          @ApiParam(value = "Build task id", required = true) @QueryParam("buildTaskId") String buildTaskIdParam,
                          @ApiParam(value = "Build set task id", required = false) @QueryParam("buildSetTaskId") String buildSetTaskId, //TODO redundant
                          @ApiParam(value = "Optional Callback URL", required = false) @QueryParam("callbackUrl") String callbackUrl,
                          @ApiParam(value = "A CSV list of build record set ids.", required = false) @QueryParam("buildRecordSetIdsCSV") String buildRecordSetIdsCSV,
                          @ApiParam(value = "Build configuration set record id.", required = false) @QueryParam("buildConfigSetRecordId") String buildConfigSetRecordId,
                          @ApiParam(value = "BuildTask submit time in number of millis since epoch.", required = true) @QueryParam("submitTimeMillis") String submitTimeMillisParam,
                          @ApiParam(value = "Username who triggered the build. If empty current user is used.", required = false) @QueryParam("usernameTriggered") String usernameTriggered,
                          @Context UriInfo uriInfo,
                          @Context HttpServletRequest request) {
        try {

            logger.debug("Endpoint /execute-build requested for buildTaskId [{}], by [{}]", buildTaskIdParam, request.getRemoteAddr());

            Integer buildTaskId;
            Response errorResponse = validateRequiredField(buildTaskIdParam, "buildTaskId");
            if (errorResponse != null) {
                return errorResponse;
            } else {
                buildTaskId = Integer.parseInt(buildTaskIdParam);
            }

            Integer buildConfigurationRevision;
            errorResponse = validateRequiredField(buildConfigurationRevisionParam, "buildConfigurationRevision");
            if (errorResponse != null) {
                return errorResponse;
            } else {
                buildConfigurationRevision = Integer.parseInt(buildConfigurationRevisionParam);
            }

            Long submitTimeMillis;
            if (submitTimeMillisParam == null || submitTimeMillisParam.equals("")) {
                logger.warn("Missing required submitTimeMillis parameter. Using 'now' instead.");
                submitTimeMillis = System.currentTimeMillis();
            } else {
                submitTimeMillis = Long.parseLong(submitTimeMillisParam);
            }

            AuthenticationProvider authProvider = new AuthenticationProvider(httpServletRequest);
            String loggedUser = authProvider.getUserName();
            User currentUser;
            if(loggedUser == null || loggedUser == "") {
                return Response.status(Response.Status.FORBIDDEN).build();
            } else {
                currentUser = datastore.retrieveUserByUsername(loggedUser);
            }

            User userTriggered;
            if (usernameTriggered != null && !usernameTriggered.equals("") && !usernameTriggered.equals("null")) {
                userTriggered = datastore.retrieveUserByUsername(usernameTriggered);
            } else {
                //TODO remove dependency on buildCoordinator as buildExecutor must be decoupled from buildCoordinator (different deployment). The dependency was introduced to workaround the missing user parameter which should be passed to restEndpoint.
                Optional<BuildTask> taskOptional = buildCoordinator.getActiveBuildTasks().stream().filter(buildTask -> buildTask.getId() == buildTaskId.intValue()).findFirst();
                if (taskOptional.isPresent()) {
                    userTriggered = taskOptional.get().getUser();
                } else {
                    userTriggered = currentUser; //in case of BPM this is a predefined user that BPM uses to authenticate
                }
            }

            BuildExecutionTask buildExecutionTask = buildTriggerer.executeBuild(
                    buildTaskId,
                    buildConfigurationId,
                    buildConfigurationRevision,
                    buildRecordSetIdsCSV,
                    buildConfigSetRecordId,
                    userTriggered,
                    submitTimeMillis,
                    callbackUrl);

            UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/result/running/{id}");
            URI uri = uriBuilder.build(buildTaskId);
            BuildRecordRest buildRecordRest = new BuildRecordRest(buildExecutionTask, new Date(submitTimeMillis));
            Response response = Response.ok(uri).header("location", uri).entity(new Singleton(buildRecordRest)).build();
            return response;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = BuildConfigurationPage.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = BuildConfigurationPage.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}/dependencies/{dependencyId}")
    public Response removeDependency(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("dependencyId") Integer dependencyId) {
        buildConfigurationProvider.removeDependency(id, dependencyId);
        return fromEmpty();
    }

    @ApiOperation(value = "Get associated Product Versions of the specified Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductVersionPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ProductVersionPage.class),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/product-versions")
    public Response getProductVersions(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(productVersionProvider.getAllForBuildConfiguration(pageIndex, pageSize, sort, q, id));
    }

    @ApiOperation(value = "Associates a product version to the specified config")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/product-versions")
    public Response addProductVersion(
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            ProductVersionRest productVersion) {
        buildConfigurationProvider.addProductVersion(id, productVersion.getId());
        return fromEmpty();
    }

    @ApiOperation(value = "Removes a product version from the specified config set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}/product-versions/{productVersionId}")
    public Response removeProductVersion(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Product version id", required = true) @PathParam("productVersionId") Integer productVersionId) {
        buildConfigurationProvider.removeProductVersion(id, productVersionId);
        return fromEmpty();
    }

    @ApiOperation(value = "Gets audited revisions of this build configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
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
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-records/latest")
    public Response getLatestBuildRecord(@ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return this.fromSingleton(buildRecordProvider.getLatestBuildRecord(id));
    }

}
