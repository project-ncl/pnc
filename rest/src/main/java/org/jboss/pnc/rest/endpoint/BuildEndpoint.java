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
import org.jboss.pnc.common.mdc.MDCMeta;
import org.jboss.pnc.common.mdc.MDCUtils;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.BuildRecordPage;
import org.jboss.pnc.rest.swagger.response.BuildRecordSingleton;
import org.jboss.pnc.rest.swagger.response.SshCredentialsSingleton;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import java.util.Optional;

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

@Api(value = "/builds", description = "Active and archived Build Records endpoint")
@Path("/builds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildEndpoint extends AbstractEndpoint<BuildRecord, BuildRecordRest> {

    private static final Logger logger = LoggerFactory.getLogger(BuildEndpoint.class);

    private BuildRecordProvider buildRecordProvider;
    private EndpointAuthenticationProvider endpointAuthProvider;
    private BuildTriggerer buildTriggerer;

    @Context
    private HttpServletRequest request;

    @Deprecated
    public BuildEndpoint() {
    }

    @Inject
    public BuildEndpoint(BuildRecordProvider buildRecordProvider, EndpointAuthenticationProvider endpointAuthProvider,
            BuildTriggerer buildTriggerer) {
        super(buildRecordProvider);
        this.buildRecordProvider = buildRecordProvider;
        this.endpointAuthProvider = endpointAuthProvider;
        this.buildTriggerer = buildTriggerer;
    }

    @ApiOperation(value = "Gets all Build Records")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @TimedMetric
    public Response getAll(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Find by BuildConfigurationName (query is combined with other criteria using OR.).", required = false)
            @QueryParam("orFindByBuildConfigurationName") String orFindByBuildConfigurationName,
            @ApiParam(value = "Find by BuildConfigurationName (query is combined with other criteria using AND.).", required = false)
                @QueryParam("andFindByBuildConfigurationName") String andFindByBuildConfigurationName) {
        return fromCollection(buildRecordProvider.getRunningAndCompletedBuildRecords(pageIndex, pageSize, sort, orFindByBuildConfigurationName, andFindByBuildConfigurationName, q));
    }

    @ApiOperation(value = "Gets a BuildRecord (active or archived)")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildRecordSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        BuildRecordRest record = buildRecordProvider.getSpecific(id);
        if (record == null) {
          record = buildRecordProvider.getSpecificRunning(id);
        }
        return fromSingleton(record);
    }

    @ApiOperation(value = "Gets ssh credentials for a build",
            notes = "This GET request is for authenticated users only. " +
                    "The path for the endpoint is not restful to be able to authenticate this GET request only.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = SshCredentialsSingleton.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NOT_FOUND_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/ssh-credentials/{id}")
    public Response getSshCredentials(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        User currentUser = endpointAuthProvider.getCurrentUser(request);
        if (currentUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        SshCredentials credentials = buildRecordProvider.getSshCredentialsForUser(id, currentUser);
        if (credentials != null) {
            return fromSingleton(credentials);
        } else {
            return Response.status(NO_CONTENT_CODE).build();
        }
    }

    @ApiOperation(value = "Cancel running build.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION)
    })
    @POST
    @Path("/{id}/cancel")
    public Response cancel(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer buildTaskId) {
        boolean success = false;
        try {
            logger.debug("Received cancel request for buildTaskId: {}.", buildTaskId);
            Optional<MDCMeta> mdcMeta = buildTriggerer.getMdcMeta(buildTaskId);
            if (mdcMeta.isPresent()) {
                MDCUtils.setMDC(mdcMeta.get());
            } else {
                logger.warn("Unable to retrieve MDC meta. There is no running build for buildTaskId: {}.", buildTaskId);
            }
            success = buildTriggerer.cancelBuild(buildTaskId);
        } catch (BuildConflictException | CoreException e) {
            logger.error("Unable to cancel the build [" + buildTaskId + "].", e);
            return Response.serverError().build();
        }
        if (success) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
