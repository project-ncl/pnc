/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.bpm.model.mapper.BuildResultMapper;
import org.jboss.pnc.common.Date.ExpiresDate;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.facade.BuildCoordinatorProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.rest.endpoints.internal.api.BuildTaskEndpoint;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Dependent
public class BuildTaskEndpointImpl implements BuildTaskEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BuildTaskEndpointImpl.class);

    @Inject
    private BuildCoordinatorProvider buildCoordinatorProvider;

    @Inject
    private BuildResultMapper mapper;

    @Inject
    SystemConfig systemConfig;

    @Inject
    UserService userService;

    @Override
    public Response buildTaskCompleted(String buildId, BuildResultRest buildResult) throws InvalidEntityException {

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

        // check if task is already completed
        // required workaround as we don't remove the BpmTasks immediately after the completion
        Optional<BuildTask> maybeBuildTask = buildCoordinatorProvider.getCoordinator().getSubmittedBuildTask(buildId);
        if (maybeBuildTask.isPresent()) {
            BuildTask buildTask = maybeBuildTask.get();
            boolean temporaryBuild = buildTask.getBuildOptions().isTemporaryBuild();
            MDCUtils.addBuildContext(
                    buildTask.getContentId(),
                    temporaryBuild,
                    ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), temporaryBuild),
                    userService.currentUser().getId().toString());
            try {
                if (buildTask.getStatus().isCompleted()) {
                    logger.warn(
                            "BuildTask with id: {} is already completed with status: {}",
                            buildTask.getId(),
                            buildTask.getStatus());
                    return Response.status(Response.Status.GONE)
                            .entity(
                                    "BuildTask with id: " + buildTask.getId() + " is already completed with status: "
                                            + buildTask.getStatus() + ".")
                            .build();
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Received build result wit full log: {}.", buildResult.toFullLogString());
                }
                logger.debug("Completing buildTask [{}] ...", buildId);

                buildCoordinatorProvider.getCoordinator().completeBuild(buildTask, mapper.toEntity(buildResult));

                logger.debug("Completed buildTask [{}].", buildId);
                return Response.ok().build();
            } finally {
                MDCUtils.removeBuildContext();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("No active build with id: " + buildId).build();
        }
    }

    @Override
    public Response buildTaskCompletedJson(String buildId, BuildResultRest buildResult)
            throws org.jboss.pnc.facade.validation.InvalidEntityException {
        return buildTaskCompleted(buildId, buildResult);
    }
}
