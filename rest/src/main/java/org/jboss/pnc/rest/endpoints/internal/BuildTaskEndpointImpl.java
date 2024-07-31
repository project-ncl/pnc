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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.api.dto.ErrorResponse;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.bpm.model.mapper.BuildResultMapper;
import org.jboss.pnc.common.Date.ExpiresDate;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.GroupBuildProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.BuildTaskMappers;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.remotecoordinator.builder.SetRecordTasks;
import org.jboss.pnc.rest.endpoints.internal.api.BuildTaskEndpoint;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.rex.common.enums.State;
import org.jboss.pnc.rex.common.enums.StopFlag;
import org.jboss.pnc.rex.model.ServerResponse;
import org.jboss.pnc.rex.model.requests.MinimizedTask;
import org.jboss.pnc.rex.model.requests.NotificationRequest;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildMeta;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.EnumSet;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.text.MessageFormat.format;
import static org.jboss.pnc.mapper.api.BuildTaskMappers.toBuildStatus;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.*;

@Dependent
public class BuildTaskEndpointImpl implements BuildTaskEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BuildTaskEndpointImpl.class);

    private final ObjectMapper jsonMapper = new JacksonProvider().getMapper();

    @Inject
    private BuildCoordinator buildCoordinator;

    @Inject
    private BuildResultMapper mapper;

    @Inject
    private BuildTaskMappers taskMapper;

    @Inject
    private SystemConfig systemConfig;

    @Inject
    private UserService userService;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private GroupBuildProvider groupBuildProvider;

    @Inject
    private SetRecordTasks setRecordTasks;

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
        Optional<BuildTask> maybeBuildTask;
        try {
            maybeBuildTask = buildCoordinator.getSubmittedBuildTask(buildId);
        } catch (RemoteRequestException | MissingDataException e) {
            // these are never thrown by In-memory BuildCoordinator
            throw new RuntimeException(e);
        }

        if (maybeBuildTask.isPresent()) {
            BuildTask buildTask = maybeBuildTask.get();
            boolean temporaryBuild = buildTask.getBuildOptions().isTemporaryBuild();
            // user MDC must be set by the request filter
            MDCUtils.addBuildContext(
                    buildTask.getContentId(),
                    temporaryBuild,
                    ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), temporaryBuild),
                    null);
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

                buildCoordinator.completeBuild(buildTask, mapper.toEntity(buildResult));

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
    @Deprecated // TODO remove once fully migrated to Rex
    public Response buildTaskCompletedJson(String buildId, BuildResultRest buildResult)
            throws org.jboss.pnc.facade.validation.InvalidEntityException {
        return buildTaskCompleted(buildId, buildResult);
    }

    @Override
    @Transactional
    public Response buildTaskNotification(@NotBlank String buildId, @NotNull NotificationRequest notification)
            throws InvalidEntityException {
        logger.debug("Received transition notification for Build '{}'", buildId);

        // BuildID of endpoint must match the rex task ID
        if (!buildId.equals(notification.getTask().getName())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Build ID does not correspond to associated Rex Task")
                    .build();
        }

        BuildMeta buildMeta = parseBuildMeta(buildId, notification);

        // Task has to be present in Rex, otherwise it is unknown
        validateBuildIsActive(buildId, notification);

        // Avoid notifications if build is already persisted
        validateBuildHasNotBeenSaved(buildId, notification);

        State newState = notification.getAfter();
        State previousState = notification.getBefore();
        StopFlag stopFlag = notification.getTask().getStopFlag();
        logger.debug("Handling transition for build '{}' from {} to {}", buildId, previousState, newState);

        if (newState.isFinal()) {
            // handle edge case when GroupBuild didn't manage to be saved yet but Build finished faster
            validateSetRecordIsPresent(buildId, notification);

            handleFinalTransition(notification.getTask(), buildMeta, previousState);
        } else if (!shouldSkip(previousState, newState, stopFlag)) {
            handleRegularTransition(notification.getTask(), buildMeta, newState, previousState, stopFlag);
        }

        logger.debug("Completed notification for build '{}'.", buildId);
        return Response.ok().build();
    }

    private void validateSetRecordIsPresent(String buildId, NotificationRequest notification)
            throws WebApplicationException {
        String groupBuildId = notification.getTask().getCorrelationID();
        if (groupBuildId != null && groupBuildProvider.getSpecific(groupBuildId) == null) {
            throw new WebApplicationException(
                    Response.status(parseInt(TOO_EARLY_CODE))
                            .entity(format("GroupBuild is not yet persisted."))
                            .build());
        }
    }

    private void validateBuildHasNotBeenSaved(String buildId, NotificationRequest request)
            throws WebApplicationException {
        // fetch directly from DB
        BuildRecord build = buildRecordRepository.queryById(new Base32LongID(buildId));
        if (build != null && build.getStatus().isFinal()) {
            logger.error("Notification arrived while Build '{}' is already saved. Request: {}", buildId, request);
            throw new WebApplicationException(
                    Response.status(Response.Status.GONE)
                            .entity(format("Build {0} has already been persisted.", buildId))
                            .build());
        }
    }

    private void validateBuildIsActive(String buildId, NotificationRequest request)
            throws InternalServerErrorException, NotFoundException {
        Optional<BuildTask> task;
        try {
            task = buildCoordinator.getSubmittedBuildTask(buildId);
        } catch (RemoteRequestException | MissingDataException e) {
            logger.error("Failed to retrieve task '" + buildId + "' from Rex. Request: " + request.toString(), e);
            throw new InternalServerErrorException("Failed to retrieve task '" + buildId + "' from Rex.", e);
        }

        if (task.isEmpty()) {
            logger.warn("Missing or inactive Build with id '{}' received a notification.", buildId);
            throw new NotFoundException(
                    Response.status(Response.Status.NOT_FOUND).entity("No active build with id: " + buildId).build());
        }
    }

    private BuildMeta parseBuildMeta(String buildId, NotificationRequest notification) throws BadRequestException {
        try {
            return jsonMapper.convertValue(notification.getAttachment(), BuildMeta.class);
        } catch (IllegalArgumentException e) {
            logger.error("Notification for Build {} is missing metadata. Request: {}", buildId, notification);
            throw new BadRequestException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("The attachment doesn't contain Build metadata")
                            .build());
        }
    }

    private boolean shouldSkip(State previousState, State newState, StopFlag stopFlag) {
        // For ongoing cancellations, we don't have BuildStatuses
        EnumSet<State> ignoredStates = EnumSet.of(State.STOP_REQUESTED, State.STOPPING);
        if (ignoredStates.contains(newState)) {
            logger.debug("Skipping transition for state {}.", newState);
            return true;
        }

        BuildCoordinationStatus before = toBuildStatus(previousState, null);
        BuildCoordinationStatus after = toBuildStatus(newState, stopFlag);

        // no change between internal statuses
        if (before == after) {
            logger.debug(
                    "Skipping transition from {} to {}, no change in current BuildStatus {}.",
                    previousState,
                    newState,
                    after);
            return true;
        }

        return false;
    }

    private void handleRegularTransition(
            MinimizedTask rexTask,
            BuildMeta buildMeta,
            State newState,
            State previousState,
            StopFlag stopFlag) {
        buildCoordinator.updateBuildTaskStatus(
                taskMapper.toBuildTaskRef(rexTask, buildMeta),
                toBuildStatus(previousState, stopFlag),
                toBuildStatus(newState, stopFlag));
    }

    private void handleFinalTransition(MinimizedTask rexTask, BuildMeta meta, State previousState) {
        Optional<BuildResult> buildResultRest = getBuildResult(rexTask, previousState);

        // store BuildRecord
        buildCoordinator.completeBuild(
                taskMapper.toBuildTaskRef(rexTask, meta),
                buildResultRest,
                toBuildStatus(previousState, rexTask.getStopFlag()));

        // poke BCSR update job
        if (rexTask.getCorrelationID() != null) {
            try {
                setRecordTasks.updateConfigSetRecordsStatuses();
            } catch (CoreException e) {
                throw new InternalServerErrorException(
                        format("Error while trying to update record jobs for finished task {0}", rexTask.getName()),
                        e);
            }
        }
    }

    private Optional<BuildResult> getBuildResult(MinimizedTask rexTask, State previousState)
            throws InvalidEntityException {
        var responseObject = rexTask.getServerResponses()
                .stream()
                .filter(sr -> sr.getState() == previousState) // get bpm response out of Rex's dto
                .findFirst();

        var bpmResponse = responseObject.map(ServerResponse::getBody);

        if (bpmResponse.isEmpty()) {
            // BPM returned null message
            return handleEmptyResponse(rexTask);
        }

        return handleResponse(rexTask, responseObject.get(), bpmResponse);
    }

    private Optional<BuildResult> handleEmptyResponse(MinimizedTask rexTask) {
        switch (rexTask.getStopFlag()) {
            case NONE:
            case UNSUCCESSFUL: {
                // SYSTEM_ERRORS
                if (rexTask.getState() == State.START_FAILED) {
                    return Optional.of(
                            createEmptyExceptionalResult(
                                    new ProcessException(
                                            "Failed to start BPM process for build " + rexTask.getName())));
                } else if (rexTask.getState() == State.FAILED) {
                    return Optional.of(
                            createEmptyExceptionalResult(
                                    new ProcessException("BPM response is missing for build " + rexTask.getName())));
                }

                return Optional.empty();
            }
            // dependency was cancelled, failed or the build had timeout on cancel
            case CANCELLED:
            case DEPENDENCY_FAILED:
                return Optional.empty();
        }

        return Optional.empty();
    }

    private Optional<BuildResult> handleResponse(
            MinimizedTask rexTask,
            ServerResponse responseObject,
            Optional<Object> bpmResponse) {
        switch (responseObject.getOrigin()) {
            case REMOTE_ENTITY:
                return parseBPMResult(rexTask, bpmResponse);
            case REX_INTERNAL_ERROR:
                return parseInternalError(rexTask, bpmResponse);
            default:
                throw new InternalServerErrorException("Unknown origin.");
        }
    }

    private Optional<BuildResult> parseInternalError(MinimizedTask rexTask, Optional<Object> bpmResponse) {
        Optional<ErrorResponse> errorOpt = Optional
                .ofNullable(jsonMapper.convertValue(bpmResponse, ErrorResponse.class));
        if (errorOpt.isPresent()) {
            ErrorResponse error = errorOpt.get();
            return Optional.of(
                    createEmptyExceptionalResult(
                            new ProcessException(
                                    "REX Internal Error for build " + rexTask.getName() + ". Exception: "
                                            + error.getErrorType() + "\nException Message: " + error.getErrorMessage()
                                            + "\nREX Details: " + error.getDetails().toString())));
        }
        return Optional.of(
                createEmptyExceptionalResult(
                        new ProcessException(
                                "Rex Internal Error for build " + rexTask.getName() + ". Can't parse the error.")));
    }

    private Optional<BuildResult> parseBPMResult(MinimizedTask rexTask, Optional<Object> bpmResponse) {
        Optional<BuildResult> buildResultRest;
        try {
            // valid response from BPM
            buildResultRest = Optional
                    .ofNullable(mapper.toEntity(jsonMapper.convertValue(bpmResponse, BuildResultRest.class)));
            ValidationBuilder.validateObject(buildResultRest, WhenCreatingNew.class).validateAnnotations();
        } catch (IllegalArgumentException e) {
            // Can't parse BPM message or Rex finished Task with Server error
            buildResultRest = Optional.of(
                    createEmptyExceptionalResult(
                            new ProcessException(
                                    "Can't parse result for build " + rexTask.getName() + ". " + e.getMessage())));
        }
        return buildResultRest;
    }

    private BuildResult createEmptyExceptionalResult(ProcessException exception) {
        return new BuildResult(
                CompletionStatus.SYSTEM_ERROR,
                Optional.of(exception),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }
}
