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
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleconfig.UIModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.mdc.BuildTaskContext;
import org.jboss.pnc.common.mdc.MDCUtils;
import org.jboss.pnc.rest.restmodel.BuildExecutionConfigurationRest;
import org.jboss.pnc.rest.restmodel.bpm.BuildResultRest;
import org.jboss.pnc.rest.restmodel.response.AcceptedResponse;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.trigger.BuildExecutorTriggerer;
import org.jboss.pnc.rest.utils.ErrorResponse;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;
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
import java.util.Optional;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Api(value = "/build-tasks", description = "Build tasks.")
@Path("/build-tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildTaskEndpoint {

    @Inject
    private BpmManager bpmManager;

    @Inject
    private BuildExecutorTriggerer buildExecutorTriggerer;

    @Inject
    private AuthenticationProviderFactory authenticationProviderFactory;

    @Inject
    Configuration configuration;

    @Inject
    SystemConfig systemConfig;

    private static final Logger logger = LoggerFactory.getLogger(BuildTaskEndpoint.class);

    @Deprecated
    public BuildTaskEndpoint() {} // CDI workaround

    @ApiOperation(value = "Notifies the completion of externally managed build task process.", response = Singleton.class)
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION)
    })
    @POST
    @Path("/{taskId}/completed")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response buildTaskCompleted(
            @ApiParam(value = "Build task id", required = true) @PathParam("taskId") Integer buildId,
            @ApiParam(value = "Build result", required = true) @FormParam("buildResult") BuildResultRest buildResult)
            throws CoreException, InvalidEntityException {

//TODO set MDC from request headers instead of business data
//        logger.debug("Received task completed notification for coordinating task id [{}].", buildId);
//        BuildExecutionConfigurationRest buildExecutionConfiguration = buildResult.getBuildExecutionConfiguration();
//        buildResult.getRepositoryManagerResult().getBuildContentId();
//        if (buildExecutionConfiguration == null) {
//            logger.error("Missing buildExecutionConfiguration in buildResult for buildTaskId [{}].", buildId);
//            throw new CoreException("Missing buildExecutionConfiguration in buildResult for buildTaskId " + buildId);
//        }
//        MDCUtils.addContext(buildExecutionConfiguration.getBuildContentId(), buildExecutionConfiguration.isTempBuild(), systemConfig.getTemporalBuildExpireDate());
        logger.info("Received build task completed notification for id {}.", buildId);

        ValidationBuilder.validateObject(buildResult, WhenCreatingNew.class)
                .validateAnnotations();

        Integer taskId = bpmManager.getTaskIdByBuildId(buildId);
        if(taskId == null) {
            logger.error("No task for id [{}].", buildId);
            throw new CoreException("Could not find BPM task for build with ID " + buildId);
        }

        //check if task is already completed
        //required workaround as we don't remove the BpmTasks immediately after the completion
        Optional<BpmTask> taskOptional = bpmManager.getTaskById(taskId);
        if (taskOptional.isPresent()) {
            BpmBuildTask bpmBuildTask = (BpmBuildTask) taskOptional.get();
            BuildTask buildTask = bpmBuildTask.getBuildTask();

            MDCUtils.addBuildContext(buildTask.getContentId(), buildTask.getBuildOptions().isTemporaryBuild(), systemConfig.getTemporalBuildExpireDate());
            if (buildTask.getStatus().isCompleted()) {
                logger.warn("Task with id: {} is already completed with status: {}", buildTask.getId(), buildTask.getStatus());
                return Response.status(Response.Status.GONE).entity("Task with id: " + buildTask.getId() + " is already completed with status: " + buildTask.getStatus() + ".").build();
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Received build result wit full log: {}.", buildResult.toFullLogString());
        }
        logger.debug("Will notify for bpmTaskId[{}] linked to buildTaskId [{}].", taskId, buildId);
        bpmManager.notify(taskId, buildResult);
        logger.debug("Notified for bpmTaskId[{}] linked to buildTaskId [{}].", taskId, buildId);
        return Response.ok().build();
    }

    @ApiOperation(value = "Triggers the build execution for a given configuration.", response = Singleton.class)
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(code = FORBIDDEN_CODE, message = FORBIDDEN_DESCRIPTION),
    })
    @POST
    @Path("/execute-build")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) //TODO accept single json
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
            MDCUtils.addBuildContext(buildExecutionConfiguration.getBuildContentId(), buildExecutionConfiguration.isTempBuild(), systemConfig.getTemporalBuildExpireDate());
            logger.info("Build execution requested.");
            logger.debug("Staring new build execution for configuration: {}. Caller requested a callback to {}.", buildExecutionConfiguration.toString(), callbackUrl);

            AuthenticationProvider authenticationProvider = authenticationProviderFactory.getProvider();
            LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(request);

            BuildExecutionSession buildExecutionSession = buildExecutorTriggerer.executeBuild(
                    buildExecutionConfiguration.toBuildExecutionConfiguration(),
                    callbackUrl,
                    loginInUser.getTokenString());

            UIModuleConfig uiModuleConfig = configuration.getModuleConfig(new PncConfigProvider<>(UIModuleConfig.class));
            UriBuilder uriBuilder = UriBuilder.fromUri(uiModuleConfig.getPncUrl()).path("/ws/executor/notifications");

            String id = Integer.toString(buildExecutionConfiguration.getId());
            AcceptedResponse acceptedResponse = new AcceptedResponse(id, uriBuilder.build().toString());

            Response response = Response.ok().entity(new Singleton(acceptedResponse)).build();
            return response;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ErrorResponse.toResponse(e);
        }
    }

    @ApiOperation(value = "Cancel the build execution defined with given executionConfigurationId.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(code = FORBIDDEN_CODE, message = FORBIDDEN_DESCRIPTION),
    })
    @POST
    @Path("/cancel-build/{buildExecutionConfigurationId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response cancelBbuild(
            @ApiParam(value = "Build Execution Configuration ID. See org.jboss.pnc.spi.executor.BuildExecutionConfiguration.", required = true)
            @PathParam("buildExecutionConfigurationId")
            Integer buildExecutionConfigurationId,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request) {
        logger.debug("Endpoint /cancel-build requested for buildTaskId [{}], from [{}]", buildExecutionConfigurationId, request.getRemoteAddr());
        try {
            Optional<BuildTaskContext> mdcMeta = buildExecutorTriggerer.getMdcMeta(buildExecutionConfigurationId);
            if (mdcMeta.isPresent()) {
                MDCUtils.addContext(mdcMeta.get());
            } else {
                logger.warn("Unable to retrieve MDC meta. There is no running build for buildExecutionConfigurationId: {}.", buildExecutionConfigurationId);
            }

            logger.info("Cancelling build execution for configuration.id: {}.", buildExecutionConfigurationId);
            buildExecutorTriggerer.cancelBuild(buildExecutionConfigurationId);

            Response response = Response.ok().build();
            return response;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ErrorResponse.toResponse(e);
        }
    }

}
