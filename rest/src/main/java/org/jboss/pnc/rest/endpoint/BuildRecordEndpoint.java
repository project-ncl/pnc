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
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.graph.GraphRest;
import org.jboss.pnc.rest.restmodel.response.Page;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.ArtifactPage;
import org.jboss.pnc.rest.swagger.response.AttributeSingleton;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationAuditedSingleton;
import org.jboss.pnc.rest.swagger.response.BuildRecordPage;
import org.jboss.pnc.rest.swagger.response.BuildRecordSingleton;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.rest.validation.exceptions.RepositoryViolationException;
import org.jboss.pnc.spi.exception.ValidationException;
import org.jboss.pnc.spi.notifications.Notifier;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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

@Api(value = "/build-records", description = "Records of build executions")
@Path("/build-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildRecordEndpoint extends AbstractEndpoint<BuildRecord, BuildRecordRest> {

    private BuildRecordProvider buildRecordProvider;
    private ArtifactProvider artifactProvider;
    private EndpointAuthenticationProvider authProvider;

    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    private Notifier notifier;

    @Context
    private HttpServletRequest httpServletRequest;

    @Deprecated //CDI workaround
    public BuildRecordEndpoint() {
    }

    @Inject
    public BuildRecordEndpoint(
            BuildRecordProvider buildRecordProvider,
            ArtifactProvider artifactProvider,
            EndpointAuthenticationProvider authProvider,
            TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker,
            Notifier notifier) {
        super(buildRecordProvider);
        this.buildRecordProvider = buildRecordProvider;
        this.artifactProvider = artifactProvider;
        this.authProvider = authProvider;
        this.temporaryBuildsCleanerAsyncInvoker = temporaryBuildsCleanerAsyncInvoker;
        this.notifier = notifier;
    }

    @ApiOperation(value = "Gets all Build Records")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Override
    @TimedMetric
    public Response getAll(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @ApiOperation(value = "Gets specific Build Record")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildRecordSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Delete specific Build Record (it must be from a temporary build). Operation is async, for the result subscribe to 'build-records#delete' events with optional qualifier buildRecord.id.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}")
    public Response delete(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id)
            throws RepositoryViolationException {

        Consumer<Result> onComplete = (result) -> {
            notifier.sendToSubscribers(result.isSuccess(), Notifier.Topic.BUILD_RECORDS_DELETE.getId(), result.getId().toString());
        };

        User currentUser = authProvider.getCurrentUser(httpServletRequest);
        try {
            temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuild(id, currentUser.getLoginToken(), onComplete);
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }
        return Response.ok().build();
    }

    @ApiOperation(value = "Gets logs for specific Build Record")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = String.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/log")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLogs(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        String buildRecordLog = buildRecordProvider.getBuildRecordLog(id);
        if (buildRecordLog == null)
            return Response.status(Status.NOT_FOUND).build();

        if (buildRecordLog.isEmpty())
            return Response.noContent().build();
        else
            return Response.ok(buildRecordProvider.getLogsForBuild(buildRecordLog)).build();
    }

    @ApiOperation(value = "Gets repour logs for specific Build Record")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = String.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/repour-log")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getRepourLogs(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        String log = buildRecordProvider.getBuildRecordRepourLog(id);
        if (log == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (log.isEmpty()) {
            return Response.noContent().build();
        } else {
            return Response.ok(buildRecordProvider.getRepourLogsForBuild(log)).build();
        }
    }

    @ApiOperation(value = "Gets artifacts built for specific Build Record")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ArtifactPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ArtifactPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/built-artifacts")
    public Response getBuiltArtifacts(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        this.getSpecific(id);
        return fromCollection(artifactProvider.getBuiltArtifactsForBuildRecord(pageIndex, pageSize, sort, q, id));
    }

    @ApiOperation(value = "Gets dependency artifacts for specific Build Record")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ArtifactPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ArtifactPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/dependency-artifacts")
    public Response getDependencyArtifacts(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(artifactProvider.getDependencyArtifactsForBuildRecord(pageIndex, pageSize, sort, q, id));
    }

    /**
     * @deprecated
     * Use /build-configuration/{id}/build-records
     */
    @Deprecated
    @ApiOperation(value = "Gets the Build Records linked to a specific Build Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/build-configurations/{configurationId}")
    public Response getAllForBuildConfiguration(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("configurationId") Integer configurationId) {
        return fromCollection(
                buildRecordProvider.getAllForBuildConfiguration(pageIndex, pageSize, sort, q, configurationId));
    }

    @ApiOperation(value = "Gets the Build Records linked to a specific Project")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/projects/{projectId}")
    public Response getAllForProject(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return fromCollection(buildRecordProvider.getAllForProject(pageIndex, pageSize, sortingRsql, rsql, projectId));
    }

    @ApiOperation(value = "Gets the Build Records produced from the BuildConfiguration by name.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/build-configuration-or-project-name/{name}")
    public Response getAllForProject(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "BuildConfiguration name", required = true) @PathParam("name") String name,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return fromCollection(buildRecordProvider.getAllForConfigurationOrProjectName(pageIndex, pageSize, sortingRsql, rsql, name));
    }

    @ApiOperation(value = "Gets the audited build configuration for specific build record")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationAuditedSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildConfigurationAuditedSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-configuration-audited")
    public Response getBuildConfigurationAudited(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return fromSingleton(buildRecordProvider.getBuildConfigurationAudited(id));
    }

    @ApiOperation(value = "Add attribute to the BuildRecord.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/put-attribute")
    public Response putAttribute(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id,
                              @ApiParam(value = "Attribute key", required = true) @QueryParam("key") String key,
                              @ApiParam(value = "Attribute value", required = true) @QueryParam("value") String value) {
        buildRecordProvider.putAttribute(id, key, value);
        return Response.ok().build();
    }

    @ApiOperation(value = "Remove attribute from BuildRecord.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}/remove-attribute")
    public Response removeAttribute(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id,
                              @ApiParam(value = "Attribute key", required = true) @QueryParam("key") String key) {
        buildRecordProvider.removeAttribute(id, key);
        return Response.ok().build();
    }

    @ApiOperation(value = "Get Build Record attributes.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = AttributeSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = AttributeSingleton.class
            ),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/get-attributes")
    public Response getAttributes(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return fromSingleton(buildRecordProvider.getAttributes(id));
    }

    @ApiOperation(value = "Get Build Records by attribute.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/get-by-attribute")
    public Response queryByAttribute(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Attribute key", required = true) @QueryParam("key") String key,
            @ApiParam(value = "Attribute value", required = true) @QueryParam("value") String value) {
        return fromCollection(buildRecordProvider.getByAttribute(pageIndex,pageSize,sort,q, key, value));
    }

    /**
     * @deprecated
     * Use /builds/{id}
     *
     * Gets a BuildRecord which is completed or in running state
     */
    @ApiOperation(value = "Deprecated, use /builds/{id}")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildRecordSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/completed-or-running")
    public Response getCompletedOrRunnning(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {

        Response resp = getSpecific(id);
        if (resp.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
          resp = fromSingleton(buildRecordProvider.getSpecificRunning(id));
        }
        return resp;
    }

    @ApiOperation(value = "Gets dependency graph for a Build Record (running or completed).")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = Singleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = Singleton.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/dependency-graph")
    public Response getDependencyGraph(@ApiParam(value = "Build id.", required = true) @PathParam("id") Integer bcId) {
        GraphRest<BuildRecordRest> dependencyGraph = buildRecordProvider.getDependencyGraphRest(bcId);
        return fromSingleton(dependencyGraph);
    }

}
