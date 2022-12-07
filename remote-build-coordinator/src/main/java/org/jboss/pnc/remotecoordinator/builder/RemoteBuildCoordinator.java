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

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.common.Date.ExpiresDate;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.monitor.PollingMonitor;
import org.jboss.pnc.common.util.ProcessStageUtils;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.remotecoordinator.BuildCoordinationException;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.coordinator.Remote;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.repour.RepourResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.jboss.pnc.common.util.CollectionUtils.hasCycle;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@Remote
@ApplicationScoped
public class RemoteBuildCoordinator implements BuildCoordinator {

    private static final EnumSet<BuildStatus> REJECTED_STATES = EnumSet
            .of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

    private final Logger log = LoggerFactory.getLogger(RemoteBuildCoordinator.class);
    private static final Logger userLog = LoggerFactory
            .getLogger("org.jboss.pnc._userlog_.build-process-status-update");

    private SystemConfig systemConfig;
    private DatastoreAdapter datastoreAdapter;
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    private RexBuildScheduler buildScheduler;

    private BuildTaskRepository taskRepository;

    private BuildTasksInitializer buildTasksInitializer;

    // Lock so that only one build method is active at any time
    private final Object buildMethodLock = new Object();
    private GroupBuildMapper groupBuildMapper;
    private BuildMapper buildMapper;

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
            SystemConfig systemConfig,
            GroupBuildMapper groupBuildMapper,
            BuildMapper buildMapper) {
        this.datastoreAdapter = datastoreAdapter;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
        this.buildScheduler = buildScheduler;
        this.systemConfig = systemConfig;
        this.taskRepository = taskRepository;
        this.buildTasksInitializer = new BuildTasksInitializer(
                datastoreAdapter,
                systemConfig.getTemporaryBuildsLifeSpan());
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
    @Override
    public BuildSetTask buildConfig(BuildConfiguration buildConfiguration, User user, BuildOptions buildOptions)
            throws BuildConflictException, CoreException {
        BuildConfigurationAudited buildConfigurationAudited = datastoreAdapter
                .getLatestBuildConfigurationAuditedInitializeBCDependencies(buildConfiguration.getId());
        return build0(user, buildOptions, buildConfigurationAudited);
    }

    private BuildSetTask build0(
            User user,
            BuildOptions buildOptions,
            BuildConfigurationAudited buildConfigurationAudited) throws BuildConflictException, CoreException {
        synchronized (buildMethodLock) {
            checkNotRunning(buildConfigurationAudited);

            BuildSetTask buildSetTask = buildTasksInitializer.createBuildSetTask(
                    buildConfigurationAudited,
                    user,
                    buildOptions,
                    this::buildRecordIdSupplier,
                    taskRepository.getUnfinishedTasks());

            build(buildSetTask);

            return buildSetTask;
        }
    }

    private void checkNotRunning(BuildConfigurationAudited buildConfigurationAudited) throws BuildConflictException {
        // TODO implement for REX
        // if (taskRepository.getUnfinishedTask(buildConfigurationAudited).isPresent()) {
        // throw new BuildConflictException(
        // "Active build task found using the same configuration BC [id=" + buildConfigurationAudited.getId()
        // + ", rev=" + buildConfigurationAudited.getRev() + "]");
        // }
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
    @Override
    public BuildSetTask buildConfigurationAudited(
            BuildConfigurationAudited buildConfigurationAudited,
            User user,
            BuildOptions buildOptions) throws BuildConflictException, CoreException {
        return build0(user, buildOptions, buildConfigurationAudited);
    }

    private String buildRecordIdSupplier() {
        return Sequence.nextBase32Id();
    }

    /**
     * Run a set of builds. Only the current/latest version of builds in the given set will be executed. The order of
     * execution is determined by the dependency relations between the build configurations.
     *
     * @param buildConfigurationSet The set of builds to be executed.
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build set task
     * @throws CoreException Thrown if there is a problem initializing the build
     */
    @Override
    @Deprecated
    public BuildSetTask buildSet(BuildConfigurationSet buildConfigurationSet, User user, BuildOptions buildOptions)
            throws CoreException {
        synchronized (buildMethodLock) {
            BuildSetTask buildSetTask = buildTasksInitializer.createBuildSetTask(
                    buildConfigurationSet,
                    user,
                    buildOptions,
                    this::buildRecordIdSupplier,
                    taskRepository.getUnfinishedTasks());
            updateBuildSetTaskStatus(buildSetTask, BuildStatus.BUILDING);

            validateBuildConfigurationSetTask(buildConfigurationSet, buildOptions, buildSetTask);

            build(buildSetTask);

            return buildSetTask;
        }
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
    @Override
    public BuildSetTask buildSet(
            BuildConfigurationSet buildConfigurationSet,
            Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap,
            User user,
            BuildOptions buildOptions) throws CoreException {

        synchronized (buildMethodLock) {
            BuildSetTask buildSetTask = buildTasksInitializer.createBuildSetTask(
                    buildConfigurationSet,
                    buildConfigurationAuditedsMap,
                    user,
                    buildOptions,
                    this::buildRecordIdSupplier,
                    taskRepository.getUnfinishedTasks());
            updateBuildSetTaskStatus(buildSetTask, BuildStatus.BUILDING);

            validateBuildConfigurationSetTask(buildConfigurationSet, buildOptions, buildSetTask);

            build(buildSetTask);

            return buildSetTask;
        }
    }

    private void validateBuildConfigurationSetTask(
            BuildConfigurationSet buildConfigurationSet,
            BuildOptions buildOptions,
            BuildSetTask buildSetTask) throws CoreException {
        checkForEmptyBuildSetTask(buildSetTask);
        if (!buildOptions.isForceRebuild()) {
            checkIfAnyBuildConfigurationNeedsARebuild(
                    buildSetTask,
                    buildConfigurationSet,
                    buildOptions.isImplicitDependenciesCheck(),
                    buildOptions.isTemporaryBuild(),
                    buildOptions.getAlignmentPreference());
        }

        checkForCyclicDependencies(buildSetTask);
    }

    private void checkIfAnyBuildConfigurationNeedsARebuild(
            BuildSetTask buildSetTask,
            BuildConfigurationSet buildConfigurationSet,
            boolean checkImplicitDependencies,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference) throws CoreException {
        Set<BuildConfiguration> buildConfigurations = buildConfigurationSet.getBuildConfigurations();
        int requiresRebuild = buildConfigurations.size();
        log.debug("There are {} configurations in a set {}.", requiresRebuild, buildConfigurationSet.getId());

        Set<Integer> processedDependenciesCache = new HashSet<>();
        for (BuildConfiguration buildConfiguration : buildConfigurations) {
            BuildConfigurationAudited buildConfigurationAudited = datastoreAdapter
                    .getLatestBuildConfigurationAuditedInitializeBCDependencies(buildConfiguration.getId());
            if (!datastoreAdapter.requiresRebuild(
                    buildConfigurationAudited,
                    checkImplicitDependencies,
                    temporaryBuild,
                    alignmentPreference,
                    processedDependenciesCache)) {
                requiresRebuild--;
            }
        }
        if (!buildConfigurations.isEmpty() && requiresRebuild == 0) {
            updateBuildSetTaskStatus(
                    buildSetTask,
                    BuildStatus.NO_REBUILD_REQUIRED,
                    "All build configs were previously built");
        }
    }

    private void build(BuildSetTask buildSetTask) throws CoreException {
        synchronized (buildMethodLock) {
            if (BuildStatus.REJECTED.equals(buildSetTask.getTaskStatus())) {
                return;
            }

            // save NRR records before doing a build
            if (!buildSetTask.getBuildOptions().isForceRebuild()) {
                createNRRRecords(buildSetTask);
            }

            if (BuildStatus.NO_REBUILD_REQUIRED.equals(buildSetTask.getTaskStatus())) {
                return;
            }

            buildScheduler.startBuilding(buildSetTask);
        }
    }

    private void createNRRRecords(BuildSetTask buildSetTask) {
        Set<BuildTask> toBuild = new HashSet<>();
        Set<BuildTask> notToBuild = new HashSet<>();

        for (BuildTask task : buildSetTask.getBuildTasks()) {
            if (!toBuild.contains(task) && !datastoreAdapter.requiresRebuild(task, new HashSet<>())) {
                notToBuild.add(task);
            } else {
                markToBuild(task, toBuild, notToBuild);
            }
        }

        notToBuild.forEach(task -> {
            completeNoBuild(task, CompletionStatus.NO_REBUILD_REQUIRED);

            // NOTE: after removal NRR task can still be referenced as a dependency of other tasks
            buildSetTask.getBuildTasks().remove(task);
        });
    }

    private void markToBuild(BuildTask task, Set<BuildTask> toBuild, Set<BuildTask> notToBuild) {
        toBuild.add(task);
        notToBuild.remove(task);
        markDependantsToBuild(task, toBuild, notToBuild);
    }

    private void markDependantsToBuild(BuildTask task, Set<BuildTask> toBuild, Set<BuildTask> notToBuild) {
        for (BuildTask dependant : task.getDependants()) {
            if (!toBuild.contains(dependant)) {
                markToBuild(dependant, toBuild, notToBuild);
            }
        }
    }

    @Override
    public boolean cancel(String buildTaskId) throws CoreException {
        // Logging MDC must be set before calling
        Optional<BuildTask> taskOptional = getSubmittedBuildTasks().stream()
                .filter(buildTask -> buildTask.getId().equals(buildTaskId))
                .findAny();
        if (taskOptional.isPresent()) {
            log.debug("Cancelling task {}.", taskOptional.get());
            try {
                boolean cancelSubmitted = buildScheduler.cancel(taskOptional.get());
                if (cancelSubmitted) {
                    monitorCancellation(taskOptional.get());
                } else {
                    cancelInternal(taskOptional.get());
                }
            } catch (CoreException e) {
                cancelInternal(taskOptional.get());
            }
            return true;
        } else {
            log.warn("Cannot find task {} to cancel.", buildTaskId);
            return false;
        }
    }

    @Override
    public Optional<BuildTaskContext> getMDCMeta(String buildTaskId) {
        return getSubmittedBuildTasks().stream()
                .filter(buildTask -> buildTaskId.equals(buildTask.getId()))
                .map(this::getMDCMeta)
                .findAny();
    }

    private BuildTaskContext getMDCMeta(BuildTask buildTask) {
        boolean temporaryBuild = buildTask.getBuildOptions().isTemporaryBuild();
        return new BuildTaskContext(
                buildTask.getContentId(),
                buildTask.getUser().getId().toString(),
                temporaryBuild,
                ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), temporaryBuild));
    }

    @Override
    public boolean cancelSet(int buildConfigSetRecordId) {
        BuildConfigSetRecord record = datastoreAdapter.getBuildCongigSetRecordById(buildConfigSetRecordId);
        if (record == null) {
            log.error("Could not find buildConfigSetRecord with id : {}", buildConfigSetRecordId);
            return false;
        }
        log.debug("Cancelling Build Configuration Set: {}", buildConfigSetRecordId);
        Collection<BuildTask> buildTasks = getSubmittedBuildTasksBySetId(buildConfigSetRecordId);
        buildTasks.forEach(buildTask -> {
            try {
                MDCUtils.addBuildContext(getMDCMeta(buildTask));
                log.debug("Received cancel request for buildTaskId: {}.", buildTask.getId());
                cancel(buildTask.getId());
            } catch (CoreException e) {
                log.error("Unable to cancel the build [" + buildTask.getId() + "].", e);
            } finally {
                MDCUtils.removeBuildContext();
            }
        });

        // modifying of the record to Cancelled state is done in SetRecordUpdateJob
        return true;
    }

    private void monitorCancellation(BuildTask buildTask) {
        int cancellationTimeout = 30;
        PollingMonitor monitor = new PollingMonitor();

        Runnable invokeCancelInternal = () -> {
            if (!getSubmittedBuildTasks().contains(buildTask)) {
                log.debug("Task {} cancellation already completed.", buildTask.getId());
                return;
            }
            log.warn("Cancellation did not complete in {} seconds.", cancellationTimeout);
            cancelInternal(buildTask);
        };
        ScheduledFuture<?> timer = monitor.timer(invokeCancelInternal, cancellationTimeout, TimeUnit.SECONDS);
        // TODO optimization: cancel the timer when the task is canceled
        // timer.cancel(false);
    }

    private void cancelInternal(BuildTask buildTask) {

        BuildResult result = new BuildResult(
                CompletionStatus.CANCELLED,
                Optional.empty(),
                "",
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        completeBuild(buildTask, result);
        // TODO 2.0 completeNoBuild(buildTask, CompletionStatus.CANCELLED); NCL-4242

        log.info("Task {} canceled internally.", buildTask.getId());
    }

    private void checkForCyclicDependencies(BuildSetTask buildSetTask) throws CoreException {
        Set<BuildTask> buildTasks = buildSetTask.getBuildTasks();
        if (hasCycle(buildTasks, BuildTask::getDependencies)) {
            updateBuildSetTaskStatus(buildSetTask, BuildStatus.REJECTED, "Build config set has a cycle");
        }
    }

    /**
     * Check if the given build set task is empty and update the status message appropriately
     */
    private void checkForEmptyBuildSetTask(BuildSetTask buildSetTask) throws CoreException {
        if (buildSetTask.getBuildTasks() == null || buildSetTask.getBuildTasks().isEmpty()) {
            updateBuildSetTaskStatus(buildSetTask, BuildStatus.REJECTED, "Build config set is empty");
        }
    }

    public void updateBuildTaskStatus(BuildTask task, BuildCoordinationStatus status) {
        updateBuildTaskStatus(task, task.getStatus(), status, null);
    }

    private void updateBuildTaskStatus(
            BuildTask task,
            BuildCoordinationStatus oldStatus,
            BuildCoordinationStatus status,
            String statusDescription) {

        task.setStatus(status);
        task.setStatusDescription(statusDescription);

        Build build = buildMapper.fromBuildTask(task);
        BuildStatusChangedEvent buildStatusChanged = new DefaultBuildStatusChangedEvent(
                build,
                BuildStatus.fromBuildCoordinationStatus(oldStatus),
                BuildStatus.fromBuildCoordinationStatus(status));
        log.debug(
                "Updated build task {} status to {}; old coord status: {}, new coord status: {}",
                task.getId(),
                buildStatusChanged,
                oldStatus,
                status);

        userLog.info("Build status updated to {}; previous: {}", status, oldStatus);

        BuildStatus oldBuildStatus = BuildStatus.fromBuildCoordinationStatus(oldStatus);
        BuildStatus newBuildStatus = BuildStatus.fromBuildCoordinationStatus(status);
        if ((oldBuildStatus != newBuildStatus) && !(oldBuildStatus.isFinal() && newBuildStatus.isFinal())) {
            // only fire notification when BuildStatus changes
            // and avoid firing the notification when old and new statuses are final (NCL-5885)
            buildStatusChangedEventNotifier.fire(buildStatusChanged);
            log.debug("Fired buildStatusChangedEventNotifier after task {} status update to {}.", task.getId(), status);
        }
    }

    private void updateBuildSetTaskStatus(BuildSetTask buildSetTask, BuildStatus status) throws CoreException {
        updateBuildSetTaskStatus(buildSetTask, status, null);
    }

    private void updateBuildSetTaskStatus(BuildSetTask buildSetTask, BuildStatus status, String description)
            throws CoreException {
        Optional<BuildConfigSetRecord> buildConfigSetRecord = buildSetTask.getBuildConfigSetRecord();

        if (buildConfigSetRecord.isPresent()) {
            updateBuildConfigSetRecordStatus(buildConfigSetRecord.get(), status, description);
        }

        buildSetTask.setTaskStatus(status);
        buildSetTask.setStatusDescription(description);
    }

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
        } catch (DatastoreException de) {
            log.error("Failed to update build config set record status: ", de);
            throw new CoreException(de);
        }

        sendSetStatusChangeEvent(status, oldStatus, setRecord, description);
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

    public void completeNoBuild(BuildTask buildTask, CompletionStatus completionStatus) {
        String buildTaskId = buildTask.getId();
        BuildCoordinationStatus coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        try {
            if (CompletionStatus.NO_REBUILD_REQUIRED.equals(completionStatus)) {
                updateBuildTaskStatus(buildTask, BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
                // TODO cancel should be here enable in 2.0 as CANCELed is not failed build
                // } else if (CompletionStatus.CANCELLED.equals(completionStatus)) {
                // updateBuildTaskStatus(buildTask, BuildCoordinationStatus.CANCELLED);
            } else {
                throw new BuildCoordinationException(String.format("Invalid status %s.", completionStatus));
            }

            log.debug("Storing no build required result. Id: {}", buildTaskId);
            BuildRecord buildRecord = datastoreAdapter.storeRecordForNoRebuild(buildTask);
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
            updateBuildTaskStatus(buildTask, coordinationStatus);
        }
    }

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
                        datastoreAdapter.storeResult(buildTask, Optional.of(buildResult), exception);
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

    @Override
    public Optional<BuildTask> getSubmittedBuildTask(String buildId) {
        // TODO Implement with Rex
        throw new UnsupportedOperationException("Not implemented YET");
    }

    public List<BuildTask> getSubmittedBuildTasks() {
        // TODO Implement with Rex
        throw new UnsupportedOperationException("Not implemented YET");
    }

    @Override
    public List<BuildTask> getSubmittedBuildTasksBySetId(int buildConfigSetRecordId) {
        // TODO Implement with Rex
        throw new UnsupportedOperationException("Not implemented YET");
    }

    @PostConstruct
    public void start() {
        log.info("The application is starting ...");
    }

    @PreDestroy
    public void destroy() {
        log.info("The application is shutting down ...");
    }
}
