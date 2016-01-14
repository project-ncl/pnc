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
import org.jboss.pnc.core.builder.coordinator.bpm.BpmCompleteListener;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.spi.BuildResult;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
            @ApiParam(value = "Build result", required = true) @FormParam("buildResult") BuildResult buildResult) {
        logger.debug("Received task completed notification for coordinating task id [{}].", taskId);

        bpmCompleteListener.notifyCompleted(taskId, buildResult);
        return Response.ok().build();
    }

}
