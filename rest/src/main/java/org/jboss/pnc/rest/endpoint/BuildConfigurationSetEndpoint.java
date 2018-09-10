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
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildConfigSetRecordProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.*;
import org.jboss.pnc.rest.trigger.BuildConfigurationSetTriggerResult;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.rest.validation.exceptions.EmptyEntityException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
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

@Api(value = "/build-configuration-sets", description = "Set of related build configurations")
@Path("/build-configuration-sets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationSetEndpoint extends AbstractEndpoint<BuildConfigurationSet, BuildConfigurationSetRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildTriggerer buildTriggerer;
    
    @Context
    private HttpServletRequest httpServletRequest;

    private Datastore datastore;
    private EndpointAuthenticationProvider endpointAuthProvider;
    private BuildConfigurationSetProvider buildConfigurationSetProvider;
    private BuildConfigurationProvider buildConfigurationProvider;
    private BuildRecordProvider buildRecordProvider;
    private BuildConfigSetRecordProvider buildConfigSetRecordProvider;

    public BuildConfigurationSetEndpoint() {

    }

    @Inject
    public BuildConfigurationSetEndpoint(BuildConfigurationSetProvider buildConfigurationSetProvider,
                                         BuildTriggerer buildTriggerer,
                                         BuildConfigurationProvider buildConfigurationProvider,
                                         BuildRecordProvider buildRecordProvider,
                                         BuildConfigSetRecordProvider buildConfigSetRecordProvider,
                                         Datastore datastore,
                                         EndpointAuthenticationProvider endpointAuthProvider) {
        super(buildConfigurationSetProvider);
        this.buildConfigurationSetProvider = buildConfigurationSetProvider;
        this.buildTriggerer = buildTriggerer;
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildRecordProvider = buildRecordProvider;
        this.buildConfigSetRecordProvider = buildConfigSetRecordProvider;
        this.endpointAuthProvider = endpointAuthProvider;
        this.datastore = datastore;
    }

    @ApiOperation(value = "Gets all Build Configuration Sets")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSetPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationSetPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    public Response getAll(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @ApiOperation(value = "Creates a new Build Configuration Set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSetSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response createNew(@NotNull BuildConfigurationSetRest buildConfigurationSetRest, @Context UriInfo uriInfo)
            throws RestValidationException {
        logger.debug("Creating new BuildConfigurationSet: {}", buildConfigurationSetRest.toString());
        return super.createNew(buildConfigurationSetRest, uriInfo);
    }

    @ApiOperation(value = "Gets a specific Build Configuration Set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSetSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildConfigurationSetSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Updates an existing Build Configuration Set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            @NotNull BuildConfigurationSetRest buildConfigurationSetRest) throws RestValidationException {
        return super.update(id, buildConfigurationSetRest);
    }

    @ApiOperation(value = "Removes a specific Build Configuration Set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id)
            throws RestValidationException {
        return super.delete(id);
    }

    @ApiOperation(value = "Gets the Configurations for the Specified Set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-configurations")
    public Response getConfigurations(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id) {
        return fromCollection(
                buildConfigurationProvider.getAllForBuildConfigurationSet(pageIndex, pageSize, sort, q, id));
    }

    @PUT
    @Path("/{id}/build-configurations")
    public Response updateConfigurations(@ApiParam(value = "Build Configuration Set Id", required = true) @PathParam("id") Integer id,
            List<BuildConfigurationRest> buildConfigurationRests) throws RestValidationException {
        buildConfigurationSetProvider.updateConfigurations(id, buildConfigurationRests);
        return Response.ok().build();
    }

    @ApiOperation(value = "Adds a configuration to the Specified Set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/build-configurations")
    public Response addConfiguration(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            BuildConfigurationRest buildConfig) throws RestValidationException {
        if (buildConfig == null || buildConfig.getId() == null) {
            throw new EmptyEntityException("No valid build config included in request to add config to set id: " + id);
        }
        buildConfigurationSetProvider.addConfiguration(id, buildConfig.getId());
        return fromEmpty();
    }

    @ApiOperation(value = "Removes a configuration from the specified config set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}/build-configurations/{configId}")
    public Response removeConfiguration(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("configId") Integer configId) throws
            RestValidationException {
        buildConfigurationSetProvider.removeConfiguration(id, configId);
        return fromEmpty();
    }

    @ApiOperation(value = "Gets all build records associated with the contained build configurations")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-records")
    public Response getBuildRecords(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(buildRecordProvider.getAllForBuildConfigSet(pageIndex, pageSize, sort, q, id));
    }

    @ApiOperation(value = "Builds the Configurations for the Specified Set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigSetRecordSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/build")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response build(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Optional Callback URL", required = false) @QueryParam("callbackUrl") String callbackUrl,
            @ApiParam(value = "Is it a temporary build or a standard build?") @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @ApiParam(value = "Should we force the rebuild of all build configurations?") @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @ApiParam(value = "Should we add a timestamp during the alignment? Valid only for temporary builds.") @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment,
            @Context UriInfo uriInfo)
            throws CoreException, MalformedURLException, InvalidEntityException {
        logger.info("Executing build configuration set: [id: {}, temporaryBuild: {}, forceRebuild: {}, timestampAlignment: {}]",
                id, temporaryBuild, forceRebuild, timestampAlignment);
        return triggerBuild(Optional.empty(), Optional.of(id), callbackUrl, temporaryBuild, forceRebuild, timestampAlignment, uriInfo);
    }

    @ApiOperation(value = "Builds the configurations for the Specified Set with an option to specify exact revision of a BC")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigSetRecordSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/build-versioned")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buildVersioned(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Optional Callback URL", required = false) @QueryParam("callbackUrl") String callbackUrl,
            @ApiParam(value = "Is it a temporary build or a standard build?") @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @ApiParam(value = "Should we force the rebuild of all build configurations?") @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @ApiParam(value = "Should we add a timestamp during the alignment? Valid only for temporary builds.") @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment,
            BuildConfigurationSetAuditedRest buildConfigurationAuditedRest,
            @Context UriInfo uriInfo)
            throws CoreException, MalformedURLException, InvalidEntityException {
        logger.info("Executing build configuration set with build configurations in specific values: " +
                "[BuildConfigurationSetAuditedRest: {}, temporaryBuild: {}, forceRebuild: {}, timestampAlignment: {}]",
                buildConfigurationAuditedRest, temporaryBuild, forceRebuild, timestampAlignment);
        return triggerBuild(Optional.of(buildConfigurationAuditedRest), Optional.empty(), callbackUrl, temporaryBuild, forceRebuild, timestampAlignment, uriInfo);
    }

    private Response triggerBuild(
            Optional<BuildConfigurationSetAuditedRest> buildConfigurationAuditedRest,
            Optional<Integer> buildConfigurationSetId,
            String callbackUrl,
            boolean temporaryBuild,
            boolean forceRebuild,
            boolean timestampAlignment,
            UriInfo uriInfo) throws InvalidEntityException, CoreException, MalformedURLException {
        User currentUser = getCurrentUser();

        BuildOptions buildOptions = new BuildOptions(temporaryBuild, forceRebuild, false, false, timestampAlignment);
        BuildConfigurationEndpoint.checkBuildOptionsValidity(buildOptions);

        BuildConfigurationSetTriggerResult result;
        if(buildConfigurationSetId.isPresent()) {
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                result = buildTriggerer.triggerBuildConfigurationSet(buildConfigurationSetId.get(), currentUser, buildOptions, new URL(callbackUrl));
            } else {
                result = buildTriggerer.triggerBuildConfigurationSet(buildConfigurationSetId.get(), currentUser, buildOptions);
            }
        }
        else {
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                result = buildTriggerer.triggerBuildConfigurationSet(buildConfigurationAuditedRest.get(), currentUser, buildOptions, new URL(callbackUrl));
            } else {
                result = buildTriggerer.triggerBuildConfigurationSet(buildConfigurationAuditedRest.get(), currentUser, buildOptions);
            }
        }

        logger.info("Started build BuildConfigurationSetAuditedRest: {}. Build Tasks: {}",
                buildConfigurationAuditedRest,
                result.getBuildTasks().stream().map(bt -> Integer.toString(bt.getId())).collect(
                        Collectors.joining()));

        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-config-set-records/{id}");
        URI uri = uriBuilder.build(result.getBuildRecordSetId());
        return Response.ok(uri).header("location", uri).entity(new Singleton<>(buildConfigSetRecordProvider.getSpecific(result.getBuildRecordSetId()))).build();
    }

    private User getCurrentUser() throws InvalidEntityException {
        User currentUser = endpointAuthProvider.getCurrentUser(httpServletRequest);
        if (currentUser == null) {
            throw new InvalidEntityException("No such user exists to trigger builds. Before triggering builds"
                    + " user must be initialized through /users/getLoggedUser");
        }
        return currentUser;
    }

    @ApiOperation(value = "Get all build config set execution records associated with this build config set, returns empty list if none are found")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSetRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationSetRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-config-set-records")
    public Response getAllBuildConfigSetRecords(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build config set id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildConfigSetRecordProvider.getAllForBuildConfigSet(pageIndex, pageSize, sort, q, id));
    }

}
