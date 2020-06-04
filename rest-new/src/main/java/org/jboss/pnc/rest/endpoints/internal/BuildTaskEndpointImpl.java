/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoints.internal;

import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationRest;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationWithCallbackRest;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.Date.ExpiresDate;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleconfig.UIModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.facade.executor.BuildExecutorTriggerer;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.rest.endpoints.internal.api.BuildTaskEndpoint;
import org.jboss.pnc.rest.restmodel.response.AcceptedResponse;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

@Dependent
public class BuildTaskEndpointImpl implements BuildTaskEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BuildTaskEndpointImpl.class);

    @Context
    private HttpServletRequest request;

    @Inject
    private BpmManager bpmManager;

    @Inject
    private BuildExecutorTriggerer buildExecutorTriggerer;

    @Inject
    Configuration configuration;

    @Inject
    SystemConfig systemConfig;

    @Inject
    UserService userService;

    @Override
    public Response buildTaskCompleted(int buildId, BuildResultRest buildResult) throws InvalidEntityException {

        // TODO set MDC from request headers instead of business data
        // logger.debug("Received task completed notification for coordinating task id [{}].", buildId);
        // BuildExecutionConfigurationRest buildExecutionConfiguration = buildResult.getBuildExecutionConfiguration();
        // buildResult.getRepositoryManagerResult().getBuildContentId();
        // if (buildExecutionConfiguration == null) {
        // logger.error("Missing buildExecutionConfiguration in buildResult for buildTaskId [{}].", buildId);
        // throw new CoreException("Missing buildExecutionConfiguration in buildResult for buildTaskId " + buildId);
        // }
        // MDCUtils.addContext(buildExecutionConfiguration.getBuildContentId(),
        // buildExecutionConfiguration.isTempBuild(), systemConfig.getTemporaryBuildExpireDate());
        logger.info("Received build task completed notification for id {}.", buildId);

        ValidationBuilder.validateObject(buildResult, WhenCreatingNew.class).validateAnnotations();

        Integer taskId = bpmManager.getTaskIdByBuildId(buildId);
        if (taskId == null) {
            logger.error("No task for id [{}].", buildId);
            throw new RuntimeException("Could not find BPM task for build with ID " + buildId);
        }

        // check if task is already completed
        // required workaround as we don't remove the BpmTasks immediately after the completion
        Optional<BpmTask> taskOptional = bpmManager.getTaskById(taskId);
        if (taskOptional.isPresent()) {
            BpmBuildTask bpmBuildTask = (BpmBuildTask) taskOptional.get();
            BuildTask buildTask = bpmBuildTask.getBuildTask();

            boolean temporaryBuild = buildTask.getBuildOptions().isTemporaryBuild();
            MDCUtils.addBuildContext(
                    buildTask.getContentId(),
                    temporaryBuild,
                    ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), temporaryBuild),
                    userService.currentUser().getId().toString());
            try {
                if (buildTask.getStatus().isCompleted()) {
                    logger.warn(
                            "Task with id: {} is already completed with status: {}",
                            buildTask.getId(),
                            buildTask.getStatus());
                    return Response.status(Response.Status.GONE)
                            .entity(
                                    "Task with id: " + buildTask.getId() + " is already completed with status: "
                                            + buildTask.getStatus() + ".")
                            .build();
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Received build result wit full log: {}.", buildResult.toFullLogString());
                }
                logger.debug("Will notify for bpmTaskId[{}] linked to buildTaskId [{}].", taskId, buildId);
                bpmManager.notify(taskId, buildResult);
                logger.debug("Notified for bpmTaskId[{}] linked to buildTaskId [{}].", taskId, buildId);
                bpmManager.remove(taskId);
                return Response.ok().build();
            } finally {
                MDCUtils.removeBuildContext();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("No active build with id: " + buildId).build();
        }

    }

    @Override
    public Response buildTaskCompletedJson(int buildId, BuildResultRest buildResult) throws InvalidEntityException {
        return buildTaskCompleted(buildId, buildResult);
    }

    @Override
    public Response build(
            BuildExecutionConfigurationRest buildExecutionConfiguration,
            String usernameTriggered,
            String callbackUrl) {
        try {
            logger.debug(
                    "Endpoint /execute-build requested for buildTaskId [{}], from [{}]",
                    buildExecutionConfiguration.getId(),
                    request.getRemoteAddr());

            boolean temporaryBuild = buildExecutionConfiguration.isTempBuild();
            MDCUtils.addBuildContext(
                    buildExecutionConfiguration.getBuildContentId(),
                    temporaryBuild,
                    ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), temporaryBuild),
                    userService.currentUser().getId().toString());

            logger.info("Build execution requested.");
            logger.debug(
                    "Staring new build execution for configuration: {}. Caller requested a callback to {}.",
                    buildExecutionConfiguration.toString(),
                    callbackUrl);

            BuildExecutionSession buildExecutionSession = buildExecutorTriggerer.executeBuild(
                    buildExecutionConfiguration.toBuildExecutionConfiguration(),
                    callbackUrl,
                    userService.currentUserToken());

            GlobalModuleGroup globalConfig = configuration.getGlobalConfig();
            UriBuilder uriBuilder = UriBuilder.fromUri(globalConfig.getExternalPncUrl())
                    .path("/ws/executor/notifications");

            String id = Integer.toString(buildExecutionConfiguration.getId());
            AcceptedResponse acceptedResponse = new AcceptedResponse(id, uriBuilder.build().toString());

            return Response.ok().entity(new Singleton(acceptedResponse)).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            MDCUtils.removeBuildContext();
        }
    }

    @Override
    public Response build(BuildExecutionConfigurationWithCallbackRest buildExecutionConfiguration) {
        try {
            String callbackUrl = buildExecutionConfiguration.getCompletionCallbackUrl();

            boolean temporaryBuild = buildExecutionConfiguration.isTempBuild();
            MDCUtils.addBuildContext(
                    buildExecutionConfiguration.getBuildContentId(),
                    temporaryBuild,
                    ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), temporaryBuild),
                    userService.currentUser().getId().toString());

            logger.info("Build execution requested.");
            logger.debug(
                    "Staring new build execution for configuration: {}. Caller requested a callback to {}.",
                    buildExecutionConfiguration.toString(),
                    callbackUrl);

            BuildExecutionSession buildExecutionSession = buildExecutorTriggerer.executeBuild(
                    buildExecutionConfiguration.toBuildExecutionConfiguration(),
                    callbackUrl,
                    userService.currentUserToken());

            GlobalModuleGroup globalConfig = configuration.getGlobalConfig();
            UriBuilder uriBuilder = UriBuilder.fromUri(globalConfig.getExternalPncUrl())
                    .path("/ws/executor/notifications");

            String id = Integer.toString(buildExecutionConfiguration.getId());
            AcceptedResponse acceptedResponse = new AcceptedResponse(id, uriBuilder.build().toString());

            return Response.ok().entity(new Singleton(acceptedResponse)).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            MDCUtils.removeBuildContext();
        }
    }

    @Override
    public Response cancelBuild(int buildExecutionConfigurationId) {

        logger.debug(
                "Endpoint /cancel-build requested for buildTaskId [{}], from [{}]",
                buildExecutionConfigurationId,
                request.getRemoteAddr());

        try {

            Optional<BuildTaskContext> mdcMeta = buildExecutorTriggerer
                    .getMdcMeta(buildExecutionConfigurationId, userService.currentUsername());

            if (mdcMeta.isPresent()) {
                MDCUtils.addBuildContext(mdcMeta.get());
            } else {
                logger.warn(
                        "Unable to retrieve MDC meta. There is no running build for buildExecutionConfigurationId: {}.",
                        buildExecutionConfigurationId);
            }

            logger.info("Cancelling build execution for configuration.id: {}.", buildExecutionConfigurationId);
            buildExecutorTriggerer.cancelBuild(buildExecutionConfigurationId);

            return Response.ok().build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            MDCUtils.removeBuildContext();
        }
    }
}
