/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.core.builder.coordinator.bpm.BpmCompleteListener;
import org.jboss.pnc.rest.restmodel.BuildExecutionConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.BuildResultRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.trigger.BuildExecutorTriggerer;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVLID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Api(value = "/build-tasks", description = "Build tasks.")
@Path("/build-tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildTaskEndpoint {

    @Context
    private HttpServletRequest httpServletRequest;

    @Inject
    private BpmCompleteListener bpmCompleteListener;

    @Inject
    private BuildExecutorTriggerer buildExecutorTriggerer;

    private static final Logger logger = LoggerFactory.getLogger(BuildTaskEndpoint.class);

    @Deprecated
    public BuildTaskEndpoint() {} // CDI workaround

    @ApiOperation(value = "Notifies the completion of externally managed build task process.", response = Singleton.class)
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION)
    })
    @POST
    @Path("/{taskId}/completed")
    public Response buildTaskCompleted(
            @ApiParam(value = "Build task id", required = true) @PathParam("taskId") Integer taskId,
            @ApiParam(value = "Build result", required = true) @FormParam("buildResult") BuildResultRest buildResult) {
        logger.debug("Received task completed notification for coordinating task id [{}].", taskId);

        bpmCompleteListener.notifyCompleted(taskId, buildResult.toBuildResult());
        return Response.ok().build();
    }

    @ApiOperation(value = "Triggers the build execution for a given configuration.", response = Singleton.class)
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVLID_CODE, message = INVALID_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(code = FORBIDDEN_CODE, message = FORBIDDEN_DESCRIPTION),
    })
    @POST
    @Path("/execute-build")
    public Response build(
            @ApiParam(value = "Build Execution Configuration. See org.jboss.pnc.spi.executor.BuildExecutionConfiguration.", required = true)
            @FormParam("buildExecutionConfiguration")
            BuildExecutionConfigurationRest buildExecutionConfiguration,
            @ApiParam(value = "Username who triggered the build. If empty current user is used.", required = false)
            @FormParam("usernameTriggered")
            String usernameTriggered,
            @ApiParam(value = "Optional Callback URL", required = false)
            @FormParam("callbackUrl")
            String callbackUrl,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request) {

        try {

            logger.debug("Endpoint /execute-build requested for buildTaskId [{}], from [{}]", buildExecutionConfiguration.getId(), request.getRemoteAddr());

//TODO input validation
//            Integer buildTaskId;
//            Response errorResponse = validateRequiredField(buildTaskIdParam, "buildTaskId");
//            if (errorResponse != null) {
//                return errorResponse;
//            } else {
//                buildTaskId = Integer.parseInt(buildTaskIdParam);
//            }


            AuthenticationProvider authProvider = new AuthenticationProvider(httpServletRequest);
            String loggedUser = authProvider.getUserName();
            if(loggedUser == null || loggedUser == "") {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            BuildExecutionSession buildExecutionSession = buildExecutorTriggerer.executeBuild(buildExecutionConfiguration.toBuildExecutionConfiguration(), callbackUrl);

            UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/result/running/{id}");
            URI uri = uriBuilder.build(buildExecutionConfiguration.getId());
            BuildRecordRest buildRecordRest = new BuildRecordRest(buildExecutionSession, null, buildExecutionConfiguration.getUser());
            Response response = Response.ok(uri).header("location", uri).entity(new Singleton(buildRecordRest)).build();
            return response;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
    }

}
