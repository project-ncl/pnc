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
package org.jboss.pnc.remotecoordinator.builder;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.common.util.ProcessStageUtils;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.remotecoordinator.BuildCoordinationException;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.coordinator.Remote;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.BuildRequestException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.jboss.pnc.spi.exception.ScheduleConflictException;
import org.jboss.pnc.spi.repour.RepourResult;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Individual build submitted: - collect all the tasks based on the dependencies
 *
 * Build set submitted: - build all the submitted BC and do not create tasks for non submitted dependencies.
 * Dependencies are used only to determine the order.
 *
 * No rebuild required: - determine base on the DB status - there must be no dependency scheduled for a rebuild
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@Remote
@ApplicationScoped
public class RemoteBuildCoordinator implements BuildCoordinator {

    private static final Logger log = LoggerFactory.getLogger(RemoteBuildCoordinator.class);

    private static final Logger userLog = LoggerFactory
            .getLogger("org.jboss.pnc._userlog_.build-process-status-update");

    private SystemConfig systemConfig;
    private DatastoreAdapter datastoreAdapter;
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    private RexBuildScheduler buildScheduler;

    private BuildTaskRepository taskRepository;

    private BuildTasksInitializer buildTasksInitializer;

    private GroupBuildMapper groupBuildMapper;
    private BuildMapper buildMapper;

    private BuildConfigurationAuditedRepository bcaRepository;

    @Deprecated
    public RemoteBuildCoordinator() {
    } // workaround for CDI constructor parameter injection

    @Inject
    public RemoteBuildCoordinator(
            DatastoreAdapter datastoreAdapter,
            Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier,
            Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier,
            RexBuildScheduler buildScheduler,
            BuildTaskRepository taskRepository,
            BuildConfigurationAuditedRepository bcaRepository,
            SystemConfig systemConfig,
            GroupBuildMapper groupBuildMapper,
            BuildMapper buildMapper,
            BuildTasksInitializer buildTasksInitializer) {
        this.datastoreAdapter = datastoreAdapter;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
        this.buildScheduler = buildScheduler;
        this.systemConfig = systemConfig;
        this.taskRepository = taskRepository;
        this.bcaRepository = bcaRepository;
        this.buildTasksInitializer = buildTasksInitializer;
        this.groupBuildMapper = groupBuildMapper;
        this.buildMapper = buildMapper;
    }

    /**
     * Run a single build. Uses the settings from the latest saved/audited build configuration.
     *
     * @param buildConfiguration The build configuration which will be used. The latest version of this build config
     *        will be built.
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build task
     * @throws BuildConflictException If there is already a build running with the same build configuration Id and
     *         version
     */
    @Transactional
    @Retry(retryOn = ScheduleConflictException.class)
    @Override
    public BuildSetTask buildConfig(BuildConfiguration buildConfiguration, User user, BuildOptions buildOptions)
            throws CoreException, BuildRequestException, BuildConflictException {
        BuildConfigurationAudited buildConfigurationAudited = datastoreAdapter
                .getLatestBuildConfigurationAuditedInitializeBCDependencies(buildConfiguration.getId());
        return buildConfigurationAudited(buildConfigurationAudited, user, buildOptions);
    }

    /**
     * Run a single build. Uses the settings from the specific revision of a BuildConfiguration. The dependencies are
     * resolved by the BuildConfiguration relations and are used in the latest revisions
     *
     * @param buildConfigurationAudited A revision of a BuildConfiguration which will be used.
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build task
     * @throws BuildConflictException If there is already a build running with the same build configuration Id and
     *         revision
     */
    @Transactional // TODO test in integration tests
    @Retry(retryOn = ScheduleConflictException.class)
    @Override
    public BuildSetTask buildConfigurationAudited(
            BuildConfigurationAudited buildConfigurationAudited,
            User user,
            BuildOptions buildOptions) throws BuildRequestException, BuildConflictException, CoreException {

        try {
            Collection<BuildTaskRef> unfinishedTasks = taskRepository.getUnfinishedTasks();
            verifyAllBCAsAreNotRunning(Collections.singleton(buildConfigurationAudited), unfinishedTasks);

            Graph<RemoteBuildTask> buildGraph = buildTasksInitializer
                    .createBuildGraph(buildConfigurationAudited, user, buildOptions, unfinishedTasks);

            ScheduleResult scheduleResult = validateAndRunBuilds(user, buildOptions, buildGraph, null);

            // save and notify NRR records
            scheduleResult.noRebuildTasks.forEach(
                    task -> completeNoBuild(
                            task,
                            scheduleResult.buildGraph,
                            CompletionStatus.NO_REBUILD_REQUIRED,
                            null,
                            user));
            // Notification of the scheduled builds are triggered from the Rex.
            BuildSetTask buildSetTask = BuildSetTask.Builder.newBuilder().build();
            BuildTask buildTask = BuildTask.build(
                    buildConfigurationAudited,
                    null,
                    null,
                    findTaskIdForCongifId(scheduleResult.buildGraph, buildConfigurationAudited.getId()),
                    null,
                    null,
                    null,
                    null,
                    null);
            buildSetTask.addBuildTask(buildTask);
            return buildSetTask; // TODO once fully migrated to Rex return id only
        } catch (ScheduleConflictException | BuildConflictException | BuildRequestException e) {
            log.warn("Cannot prepare build.", e);
            throw e;
        } catch (Throwable e) {
            String errorMessage = "Unexpected error while trying to schedule build.";
            log.error(errorMessage, e);
            throw new CoreException(errorMessage + " " + e.getMessage());
        }
    }

    @Override
    @Deprecated
    public BuildSetTask buildSet(BuildConfigurationSet buildConfigurationSet, User user, BuildOptions buildOptions)
            throws CoreException {
        throw new UnsupportedOperationException("Should not be used.");
    }

    /**
     * Run a build of BuildConfigurationSet with BuildConfigurations in specific versions.
     *
     * The set of the BuildConfigurations to be executed is determined by the buildConfigurationSet entity. If there is
     * a revision of a BuildConfiguration available in the buildConfigurationAuditedsMap parameter, then this revision
     * is executed. If it's not available, latest revision of the BuildConfiguration will be executed.
     *
     * @param buildConfigurationSet The set of the configurations to be executed
     * @param buildConfigurationAuditedsMap A map BuildConfiguration::id:BuildConfigurationAudited of specific revisions
     *        of BuildConfigurations contained in the buildConfigurationSet
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build set task
     * @throws CoreException Thrown if there is a problem initializing the build
     */
    @Transactional // TODO test in the integration tests
    @Retry(retryOn = ScheduleConflictException.class)
    @Override
    public BuildSetTask buildSet(
            BuildConfigurationSet buildConfigurationSet,
            Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap,
            User user,
            BuildOptions buildOptions) throws CoreException, BuildRequestException, BuildConflictException {

        try {
            Collection<BuildTaskRef> unfinishedTasks = taskRepository.getUnfinishedTasks();
            verifyAllBCAsAreNotRunning(buildConfigurationAuditedsMap.values(), unfinishedTasks);

            Graph<RemoteBuildTask> buildGraph = buildTasksInitializer.createBuildGraph(
                    buildConfigurationSet,
                    buildConfigurationAuditedsMap,
                    user,
                    buildOptions,
                    unfinishedTasks);
            Long buildConfigSetRecordId = Sequence.nextId();
            ScheduleResult scheduleResult = validateAndRunBuilds(
                    user,
                    buildOptions,
                    buildGraph,
                    buildConfigSetRecordId);
            // TODO end build timing
            return storeAndNotifyBuildSet(buildConfigurationSet, user, buildOptions, scheduleResult);
        } catch (ScheduleConflictException | BuildConflictException | BuildRequestException e) {
            log.warn("Cannot prepare builds.", e);
            throw e;
        } catch (Throwable e) {
            log.error("Unexpected error while trying to schedule build set.", e);
            throw e;
        }
    }

    /**
     *
     * @param user
     * @param buildOptions
     * @param buildGraph
     * @param buildConfigSetRecordId group recordId or null if it is not a group build
     */
    private ScheduleResult validateAndRunBuilds(
            User user,
            BuildOptions buildOptions,
            Graph<RemoteBuildTask> buildGraph,
            Long buildConfigSetRecordId) throws CoreException, BuildConflictException, BuildRequestException {

        GraphValidation.checkIfAnyDependencyOfAlreadyRunningIsSubmitted(buildGraph);

        Optional<BuildStatusWithDescription> validationFailedStatus = GraphValidation
                .validateBuildConfigurationSetTask(buildGraph, buildOptions);

        BuildCoordinationStatus coordinationStatus;
        BuildStatusWithDescription buildStatusWithDescription;
        Collection<RemoteBuildTask> noRebuildTasks;

        // clone before removing NRRTasks because we need the NRR vertices for storing NNRRecords
        Graph<RemoteBuildTask> buildGraphCopy = GraphUtils.clone(buildGraph);
        if (validationFailedStatus.isPresent()) {
            buildStatusWithDescription = validationFailedStatus.get();
            coordinationStatus = BuildCoordinationStatus.REJECTED;
            if (buildStatusWithDescription.getBuildStatus().equals(BuildStatus.NO_REBUILD_REQUIRED)) {
                noRebuildTasks = BuildTasksInitializer.removeNRRTasks(buildGraphCopy);
            } else if (buildStatusWithDescription.getBuildStatus().equals(BuildStatus.REJECTED)) {
                throw new BuildRequestException(buildStatusWithDescription.getDescription());
            } else {
                throw new CoreException("Unexpected validation status.");
            }
        } else {
            if (!buildOptions.isForceRebuild()) {
                noRebuildTasks = BuildTasksInitializer.removeNRRTasks(buildGraphCopy);
            } else {
                noRebuildTasks = Collections.emptySet();
            }

            log.info(
                    "Scheduling builds {}.",
                    GraphUtils.unwrap(buildGraphCopy.getVerticies())
                            .stream()
                            .map(RemoteBuildTask::getId)
                            .collect(Collectors.toList()));
            buildScheduler.startBuilding(buildGraphCopy, user, buildConfigSetRecordId);
            buildStatusWithDescription = new BuildStatusWithDescription(BuildStatus.BUILDING, null);
            coordinationStatus = BuildCoordinationStatus.ENQUEUED;
        }

        return new ScheduleResult(buildGraph, coordinationStatus, buildStatusWithDescription, noRebuildTasks);
    }

    private BuildSetTask storeAndNotifyBuildSet(
            BuildConfigurationSet buildConfigurationSet,
            User user,
            BuildOptions buildOptions,
            ScheduleResult scheduleResult) throws CoreException {
        BuildConfigSetRecord buildConfigSetRecord = storeAndNotifyBuildConfigSetRecord(
                buildConfigurationSet,
                scheduleResult.buildStatusWithDescription.getBuildStatus(),
                scheduleResult.buildStatusWithDescription.getDescription(),
                user,
                buildOptions);

        // save and notify NRR records
        scheduleResult.noRebuildTasks.forEach(
                task -> completeNoBuild(
                        task,
                        scheduleResult.buildGraph,
                        CompletionStatus.NO_REBUILD_REQUIRED,
                        buildConfigSetRecord,
                        user));
        // Notification of the scheduled builds are triggered from the Rex.

        // return BCSR.ID only
        return BuildSetTask.Builder.newBuilder().buildConfigSetRecord(buildConfigSetRecord).build();
    }

    @Override
    public Optional<BuildTask> getSubmittedBuildTask(String buildId) {
        // TODO used only in completion callback, where there required data should already come from the Rex
        return Optional.empty();
    }

    @Override
    public boolean cancel(String buildTaskId) throws CoreException {
        // Logging MDC must be set before calling
        Optional<BuildTaskRef> taskOptional = taskRepository.getUnfinishedTasks()
                .stream() // TODO use the endpoint to get a specific task
                .filter(buildTask -> buildTask.getId().equals(buildTaskId))
                .findAny();
        if (taskOptional.isPresent()) {
            log.debug("Cancelling task {}.", taskOptional.get());
            try {
                buildScheduler.cancel(taskOptional.get().getId());
                return true;
            } catch (CoreException e) {
                log.error("Failed to cancel task {}.", buildTaskId);
                return false;
            }
        } else {
            log.warn("Cannot find task {} to cancel.", buildTaskId);
            return false;
        }
    }

    @Override
    public Optional<BuildTaskContext> getMDCMeta(String buildTaskId) {
        throw new UnsupportedOperationException(
                "Remote build coordinator cannot provide more MDC details than the endpoint has.");
    }

    @Override
    public boolean cancelSet(long buildConfigSetRecordId) throws CoreException {
        BuildConfigSetRecord record = datastoreAdapter.getBuildCongigSetRecordById(buildConfigSetRecordId);
        if (record == null) {
            log.error("Could not find buildConfigSetRecord with id : {}", buildConfigSetRecordId);
            throw new CoreException("Cannot cancel the build: buildConfigSetRecord not found.");
        }
        log.debug("Cancelling Build Configuration Set: {}", buildConfigSetRecordId);
        Collection<BuildTaskRef> buildTasks = getSubmittedBuildTaskRefsBySetId(buildConfigSetRecordId);
        for (BuildTaskRef buildTask : buildTasks) {
            try {
                MDC.put(MDCKeys.PROCESS_CONTEXT_KEY, ContentIdentityManager.getBuildContentId(buildTask.getId()));
                log.debug("Received cancel request for buildTaskId: {}.", buildTask.getId());
                cancel(buildTask.getId());
            } finally {
                MDC.remove(MDCKeys.PROCESS_CONTEXT_KEY);
            }
        }

        // modifying of the record to Cancelled state is done in SetRecordUpdateJob
        return true; // TODO once fully migrated, return void as the exception is thrown;
    }

    public void updateBuildTaskStatus(BuildTask task, BuildCoordinationStatus status) {
        throw new UnsupportedOperationException("This method should not be used.");
    }

    private void updateBuildTaskStatus(
            RemoteBuildTask task,
            BuildCoordinationStatus status,
            BuildConfigSetRecord buildConfigSetRecord,
            User user) {

        Build build = buildMapper.fromBuildTask(toBuildTask(task, buildConfigSetRecord, status, user));
        BuildStatusChangedEvent buildStatusChanged = new DefaultBuildStatusChangedEvent(
                build,
                null,
                BuildStatus.fromBuildCoordinationStatus(status));
        log.debug("Updated build task {} status to {}; new coord status: {}", task.getId(), buildStatusChanged, status);

        userLog.info("Build status updated to {}.", status);

        // TODO make sure it satisfies NCL-5885
        buildStatusChangedEventNotifier.fire(buildStatusChanged);
        log.debug("Fired buildStatusChangedEventNotifier after task {} status update to {}.", task.getId(), status);
    }

    /**
     * update status, store BuildConfigSetRecord, sendSetStatusChangeEvent
     *
     * @return
     */
    private BuildConfigSetRecord storeAndNotifyBuildConfigSetRecord(
            BuildConfigurationSet buildConfigurationSet,
            BuildStatus status,
            String description,
            User user,
            BuildOptions buildOptions) throws CoreException {

        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .startTime(new Date())
                .status(status)
                .temporaryBuild(buildOptions.isTemporaryBuild())
                .alignmentPreference(buildOptions.getAlignmentPreference())
                .build();

        updateBuildConfigSetRecordStatus(buildConfigSetRecord, status, description);
        return buildConfigSetRecord;
    }

    // TODO Either rename the method to storeBuildConfigSetRecordAndNotify or move the status update to upper layer
    // because yhe method name doesn't suggest that it updates the status.
    public void updateBuildConfigSetRecordStatus(BuildConfigSetRecord setRecord, BuildStatus status, String description)
            throws CoreException {
        log.info(
                "Setting new status {} on buildConfigSetRecord.id {}. Description: {}.",
                status,
                setRecord.getId(),
                description);
        BuildStatus oldStatus = setRecord.getStatus();

        if (status.isFinal()) {
            setRecord.setEndTime(new Date());
        }
        setRecord.setStatus(status);

        // TODO: don't send the event if we haven't updated the row
        try {
            datastoreAdapter.saveBuildConfigSetRecord(setRecord);
            sendSetStatusChangeEvent(status, oldStatus, setRecord, description);
        } catch (DatastoreException de) {
            log.error("Failed to update build config set record status: ", de);
            throw new CoreException(de);
        }
    }

    private void sendSetStatusChangeEvent(
            BuildStatus status,
            BuildStatus oldStatus,
            BuildConfigSetRecord record,
            String description) {
        BuildSetStatusChangedEvent event = new DefaultBuildSetStatusChangedEvent(
                oldStatus,
                status,
                groupBuildMapper.toDTO(record),
                description);
        log.debug("Notifying build set status update {}.", event);
        buildSetStatusChangedEventNotifier.fire(event);
    }

    public void completeNoBuild(
            RemoteBuildTask buildTask,
            Graph<RemoteBuildTask> buildGraph,
            CompletionStatus completionStatus,
            BuildConfigSetRecord buildConfigSetRecord,
            User user) {
        String buildTaskId = buildTask.getId();

        BuildCoordinationStatus coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        try {
            if (CompletionStatus.NO_REBUILD_REQUIRED.equals(completionStatus)) {
                updateBuildTaskStatus(
                        buildTask,
                        BuildCoordinationStatus.REJECTED_ALREADY_BUILT,
                        buildConfigSetRecord,
                        user);
                // TODO cancel should be here enable in 2.0 as CANCELed is not failed build
                // } else if (CompletionStatus.CANCELLED.equals(completionStatus)) {
                // updateBuildTaskStatus(buildTask, BuildCoordinationStatus.CANCELLED);
            } else {
                throw new BuildCoordinationException(String.format("Invalid status %s.", completionStatus));
            }

            log.debug("Storing no build required result. Id: {}", buildTaskId);

            Optional<Vertex<RemoteBuildTask>> taskVertex = GraphUtils.getVertex(buildGraph, buildTaskId);
            List<Vertex<RemoteBuildTask>> dependants = GraphUtils.getFromVerticies(taskVertex.get().getIncomingEdges());
            List<Base32LongID> dependantIds = GraphUtils.unwrap(dependants)
                    .stream()
                    .map(RemoteBuildTask::getId)
                    .map(Base32LongID::new)
                    .collect(Collectors.toList());
            List<Vertex<RemoteBuildTask>> dependencies = GraphUtils.getToVerticies(taskVertex.get().getOutgoingEdges());
            List<Base32LongID> dependencyIds = GraphUtils.unwrap(dependencies)
                    .stream()
                    .map(RemoteBuildTask::getId)
                    .map(Base32LongID::new)
                    .collect(Collectors.toList());

            Long buildConfigSetRecordId;
            if (buildConfigSetRecord != null) {
                buildConfigSetRecordId = buildConfigSetRecord.getId();
            } else {
                buildConfigSetRecordId = null;
            }
            BuildRecord buildRecord = datastoreAdapter
                    .storeRecordForNoRebuild(buildTask, user, dependencyIds, dependantIds, buildConfigSetRecordId);
            if (buildRecord.getStatus().completedSuccessfully()) {
                coordinationStatus = BuildCoordinationStatus.DONE;
            } else {
                log.warn(
                        "[buildTaskId: {}] Something went wrong while storing the success result. The status has changed to {}.",
                        buildTaskId,
                        buildRecord.getStatus());
                coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
            }

        } catch (Throwable e) {
            log.error("[buildTaskId: " + buildTaskId + "] Cannot store results to datastore.", e);
            coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        } finally {
            updateBuildTaskStatus(buildTask, coordinationStatus, buildConfigSetRecord, user);
        }
    }

    // TODO only used from the endpoint (triggered from Rex)
    public void completeBuild(BuildTask buildTask, BuildResult buildResult) {
        String buildTaskId = buildTask.getId();

        BuildCoordinationStatus coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        try {
            if (buildResult.hasFailed()) {
                CompletionStatus operationCompletionStatus = buildResult.getCompletionStatus();

                switch (operationCompletionStatus) {
                    case SYSTEM_ERROR:
                        ProcessException exception;
                        if (buildResult.getProcessException().isPresent()) {
                            exception = buildResult.getProcessException().get();
                            log.debug(
                                    "[buildTaskId: {}] Storing build result with exception {}.",
                                    buildTaskId,
                                    exception.getMessage());
                        } else if (buildResult.getRepourResult().isPresent()) {
                            RepourResult repourResult = buildResult.getRepourResult().get();
                            if (repourResult.getCompletionStatus().isFailed()) {
                                exception = new ProcessException("Repour completed with system error.");
                                log.debug(
                                        "[buildTaskId: {}] Storing build result with system error from repour: {}.",
                                        buildTaskId,
                                        repourResult.getLog());
                            } else {
                                exception = new ProcessException("Build completed with system error but no exception.");
                                log.error(
                                        "[buildTaskId: {}] Storing build result with system_error and missing exception.",
                                        buildTaskId);
                            }
                        } else {
                            exception = new ProcessException(
                                    "Build completed with system error but no exception and no Repour result.");
                            log.error(
                                    "[buildTaskId: {}] Storing build result with system_error no exception and no Repour result.",
                                    buildTaskId);
                        }
                        // TODO datastoreAdapter.storeResult(buildTask, Optional.of(buildResult), exception);

                        coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
                        break;

                    case CANCELLED:
                    case TIMED_OUT:
                        log.debug(
                                "[buildTaskId: {}] Storing failed build result. FailedReasonStatus: {}",
                                buildTaskId,
                                operationCompletionStatus);
                        datastoreAdapter.storeResult(buildTask, buildResult);
                        coordinationStatus = BuildCoordinationStatus.CANCELLED;
                        break;

                    case FAILED:
                        log.debug(
                                "[buildTaskId: {}] Storing failed build result. FailedReasonStatus: {}",
                                buildTaskId,
                                operationCompletionStatus);
                        datastoreAdapter.storeResult(buildTask, buildResult);
                        coordinationStatus = BuildCoordinationStatus.DONE_WITH_ERRORS;
                        break;

                    case SUCCESS:
                        throw new BuildCoordinationException("Failed task with SUCCESS completion status ?!.");
                }

            } else {
                log.debug("[buildTaskId: {}] Storing success build result.", buildTaskId);
                BuildRecord buildRecord = datastoreAdapter.storeResult(buildTask, buildResult);
                if (buildRecord.getStatus().completedSuccessfully()) {
                    coordinationStatus = BuildCoordinationStatus.DONE;
                } else {
                    log.warn(
                            "[buildTaskId: {}] Something went wrong while storing the success result. The status has changed to {}.",
                            buildTaskId,
                            buildRecord.getStatus());
                    coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
                }
            }

            updateBuildTaskStatus(buildTask, coordinationStatus);

        } catch (Throwable e) {
            log.error("[buildTaskId: " + buildTaskId + "] Cannot store results to datastore.", e);
            updateBuildTaskStatus(buildTask, BuildCoordinationStatus.SYSTEM_ERROR);
        } finally {
            // Starts when the build execution completes
            ProcessStageUtils.logProcessStageEnd("FINALIZING_BUILD", "Finalizing completed.");
        }
    }

    public List<BuildTask> getSubmittedBuildTasks() throws RemoteRequestException {
        Collection<BuildTaskRef> unfinishedTasks = taskRepository.getUnfinishedTasks();
        return unfinishedTasks.stream().map(this::toBuildTask).collect(Collectors.toList());
    }

    @Override
    public List<BuildTask> getSubmittedBuildTasksBySetId(long buildConfigSetRecordId) throws RemoteRequestException {
        // in the returned tasks only ids and BuildConfigurationAudited names are used
        List<BuildTaskRef> tasks = taskRepository.getBuildTasksByBCSRId(buildConfigSetRecordId);
        return tasks.stream().map(t -> {
            BuildConfigurationAudited bca = bcaRepository.queryById(t.getIdRev());
            return BuildTask.build(bca, null, null, t.getId(), null, null, null, null, null);
        }).collect(Collectors.toList());
    }

    private BuildTask toBuildTask(BuildTaskRef buildTask) {
        String contentId = ContentIdentityManager.getBuildContentId(buildTask.getId());
        IdRev idRev = buildTask.getIdRev();
        BuildConfigurationAudited buildConfigurationAudited = bcaRepository.queryById(idRev);

        User user = new User();
        user.setUsername(buildTask.getUsername());
        return BuildTask.build(
                buildConfigurationAudited,
                null,
                user, // used by BuildConfigurationProviderImpl.getBuildConfigurationIncludeLatestBuild ->
                      // populateBuildConfigWithLatestBuild
                buildTask.getId(),
                null,
                Date.from(buildTask.getSubmitTime()), // used by
                                                      // BuildConfigurationProviderImpl.getBuildConfigurationIncludeLatestBuild
                                                      // -> populateBuildConfigWithLatestBuild
                null,
                contentId,
                null);
    }

    private BuildTask toBuildTask(
            RemoteBuildTask buildTask,
            BuildConfigSetRecord buildConfigSetRecord,
            BuildCoordinationStatus status,
            User user) {
        String contentId = ContentIdentityManager.getBuildContentId(buildTask.getId());

        BuildSetTask buildSetTask;
        if (buildConfigSetRecord != null) {
            buildSetTask = BuildSetTask.Builder.newBuilder().buildConfigSetRecord(buildConfigSetRecord).build();
        } else {
            buildSetTask = null;
        }
        BuildTask build = BuildTask.build(
                buildTask.getBuildConfigurationAudited(),
                null,
                user,
                buildTask.getId(),
                buildSetTask,
                Date.from(buildTask.getSubmitTime()),
                null,
                contentId,
                null);
        build.setStatus(status);
        return build;
    }

    private List<BuildTaskRef> getSubmittedBuildTaskRefsBySetId(long buildConfigSetRecordId)
            throws RemoteRequestException {
        return taskRepository.getBuildTasksByBCSRId(buildConfigSetRecordId);
    }

    @PostConstruct
    public void start() {
        log.info("The application is starting ...");
    }

    @PreDestroy
    public void destroy() {
        log.info("The application is shutting down ...");
    }

    /**
     * @throws BuildConflictException if every BCA is already scheduled.
     */
    private void verifyAllBCAsAreNotRunning(
            Collection<BuildConfigurationAudited> BCAs,
            Collection<BuildTaskRef> unfinishedTasks) throws BuildConflictException {

        Set<IdRev> unfinished = unfinishedTasks.stream().map(t -> t.getIdRev()).collect(Collectors.toUnmodifiableSet());

        Set<IdRev> running = BCAs.stream()
                .map(bca -> bca.getIdRev())
                .filter(unfinished::contains)
                .collect(Collectors.toSet());

        if (running.size() == BCAs.size()) {
            String runningMessage = running.stream().map(IdRev::toString).collect(Collectors.joining(", "));
            throw new BuildConflictException("All the build configurations are already running. " + runningMessage);
        }
    }

    private String findTaskIdForCongifId(Graph<RemoteBuildTask> buildGraph, Integer id) {
        return GraphUtils.unwrap(buildGraph.getVerticies())
                .stream()
                .filter(rbt -> rbt.getBuildConfigurationAudited().getId().equals(id))
                .findFirst()
                .get()
                .getId();
    }

}
