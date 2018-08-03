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
import org.jboss.pnc.coordinator.maintenance.Result;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.rest.provider.BuildConfigSetRecordProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.graph.GraphRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.BuildConfigSetRecordSingleton;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationSetRecordPage;
import org.jboss.pnc.rest.swagger.response.BuildRecordPage;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.rest.validation.exceptions.RepositoryViolationException;
import org.jboss.pnc.spi.exception.ValidationException;
import org.jboss.pnc.spi.notifications.Notifier;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.function.Consumer;

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

@Api(value = "/build-config-set-records", description = "Records of the build config set executions")
@Path("/build-config-set-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigSetRecordEndpoint extends AbstractEndpoint<BuildConfigSetRecord, BuildConfigSetRecordRest> {

    private BuildRecordProvider buildRecordProvider;

    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    private EndpointAuthenticationProvider authenticationProvider;

    @Context
    private HttpServletRequest httpServletRequest;

    private Notifier notifier;

    @Deprecated //CDI workaround
    public BuildConfigSetRecordEndpoint() {
    }

    @Inject
    public BuildConfigSetRecordEndpoint(
            BuildConfigSetRecordProvider buildConfigSetRecordProvider,
            BuildRecordProvider buildRecordProvider,
            TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker,
            EndpointAuthenticationProvider authenticationProvider, Notifier notifier) {
        super(buildConfigSetRecordProvider);
        this.buildRecordProvider = buildRecordProvider;
        this.temporaryBuildsCleanerAsyncInvoker = temporaryBuildsCleanerAsyncInvoker;
        this.authenticationProvider = authenticationProvider;
        this.notifier = notifier;
    }

    @ApiOperation(value = "Gets all build config set execution records")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSetRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationSetRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @TimedMetric
    public Response getAll(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @ApiOperation(value = "Gets specific build config set execution record")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigSetRecordSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildConfigSetRecordSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "BuildConfigSetRecord id", required = true) @PathParam("id") @NotNull Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Delete specific Build Config Set Record (it must be from a temporary build). Operation is async, for the result subscribe to 'build-config-set-records#delete' events with optional qualifier buildRecord.id.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}")
    public Response delete(@ApiParam(value = "BuildConfigSetRecord id", required = true) @PathParam("id") Integer id)
            throws RepositoryViolationException {
        User currentUser = authenticationProvider.getCurrentUser(httpServletRequest);

        Consumer<Result> onComplete = (result) -> {
            notifier.sendToSubscribers(result.isSuccess(), Notifier.Topic.BUILD_CONFIG_SET_RECORDS_DELETE.getId(), result.getId().toString());
        };

        try {
            temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuildConfigSetRecord(id, currentUser.getLoginToken(), onComplete);
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }
        return Response.ok().build();
    }

    @ApiOperation(value = "Gets the build records associated with this set")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-records")
    public Response getBuildRecords(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build Config set record id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildRecordProvider.getAllForBuildConfigSetRecord(pageIndex, pageSize, sort, q, id));
    }

    @ApiOperation(value = "Gets dependency graph for a Build Group Record (running and completed).")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/dependency-graph")
    public Response getDependencyGraphForSet(@ApiParam(value = "Build record set id.", required = true) @PathParam("id") Integer bcSetRecordId) {
        GraphRest<BuildRecordRest> dependencyGraph = buildRecordProvider.getBCSetRecordRestGraph(bcSetRecordId);
        return fromSingleton(dependencyGraph);
    }
}
