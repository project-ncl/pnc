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

import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.common.Date.ExpiresDate;
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
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.remotecoordinator.BuildCoordinationException;
import org.jboss.pnc.remotecoordinator.BuildGraph;
import org.jboss.pnc.remotecoordinator.RemoteBuildTask;
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
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Individual build submitted:
 * - collect all the tasks based on the dependencies
 *
 * Build set submitted:
 * - build all the submitted BC and do not create tasks for non submitted dependencies.
 *   Dependencies are used only to determine the order.
 *
 * No rebuild required:
 * - determine base on the DB status
 * - there must be no dependency scheduled for a rebuild
 * //TODO Jan: where do we order the builds (dependencies first) as we need that for NNR
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

        Collection<BuildTaskRef> unfinishedTasks = taskRepository.getUnfinishedTasks();
        checkAllRunning(Collections.singleton(buildConfigurationAudited), unfinishedTasks);

        BuildGraph buildGraph = buildTasksInitializer.createBuildGraph(
                buildConfigurationAudited,
                user,
                buildOptions,
                unfinishedTasks);

        checkIfAnyDependencyOfAlreadyRunningIsSubmitted(buildGraph);

        Collection<RemoteBuildTask> noRebuildTasks;
        if (buildOptions.isForceRebuild()) {
            noRebuildTasks = Collections.emptySet();
        } else {
            noRebuildTasks = removeNRRTasks(buildGraph, buildOptions);
        }
        if (!buildGraph.getBuildTasks().isEmpty()) {
            buildScheduler.startBuilding(buildGraph);
        }
        // save NRR records
        noRebuildTasks.forEach(task -> completeNoBuild(task, CompletionStatus.NO_REBUILD_REQUIRED));

        String id = buildGraph.getBuildTasks()
                .stream()
                .filter(t -> t.getBuildConfigurationAudited().getIdRev().equals(buildConfigurationAudited.getIdRev()))
                .findAny()
                .orElseThrow(() -> new CoreException(
                        "Missing task with IdRev: " + buildConfigurationAudited.getIdRev()))
                .getId();

        // return ID only
        BuildSetTask buildSetTask = BuildSetTask.Builder.newBuilder().build();
        BuildTask buildTask = BuildTask.build(
                buildConfigurationAudited,
                null,
                null,
                id,
                null,
                new Date(),
                null,
                null,
                null);

        buildSetTask.addBuildTask(buildTask);
        return buildSetTask;
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
            BuildOptions buildOptions) throws CoreException, BuildConflictException {

        Collection<BuildTaskRef> unfinishedTasks = taskRepository.getUnfinishedTasks();
        checkAllRunning(buildConfigurationAuditedsMap.values(), unfinishedTasks);

        BuildGraph buildGraph = buildTasksInitializer.createBuildGraph(buildConfigurationSet,
                buildConfigurationAuditedsMap,
                user,
                buildOptions,
                unfinishedTasks);
        checkIfAnyDependencyOfAlreadyRunningIsSubmitted(buildGraph);

        Optional<ValidationStatus> validationFailedStatus = validateBuildConfigurationSetTask(buildGraph, buildOptions);

        BuildConfigSetRecord buildConfigSetRecord = null;
        if (validationFailedStatus.isPresent()) {
            ValidationStatus validationStatus = validationFailedStatus.get();
            storeBuildBuildConfingSetRecord(buildConfigurationSet,
                    validationStatus.buildStatus,
                    validationStatus.description,
                    user,
                    buildOptions);
        } else {
            Collection<RemoteBuildTask> noRebuildTasks;
            if (!buildOptions.isForceRebuild()) {
                noRebuildTasks = removeNRRTasks(buildGraph, buildOptions);
            } else {
                noRebuildTasks = Collections.emptySet();
            }
            buildScheduler.startBuilding(buildGraph);

            // save NRR records
            noRebuildTasks.forEach(task -> completeNoBuild(task, CompletionStatus.NO_REBUILD_REQUIRED));

            buildConfigSetRecord = storeBuildBuildConfingSetRecord(buildConfigurationSet,
                    BuildStatus.BUILDING,
                    null,
                    user,
                    buildOptions);
        }
        // return BCSR.ID only
        return BuildSetTask.Builder.newBuilder()
                .buildConfigSetRecord(buildConfigSetRecord)
                .build();
    }

    @Override
    public Optional<BuildTask> getSubmittedBuildTask(String buildId) {
        //TODO used only in completion callback, where there required data should already come from the Rex
        return Optional.empty();
    }

    /**
     * Returns a BuildStatus if no build is required or not possible, otherwise Optional.empty is returned.
     */
    private Optional<ValidationStatus> validateBuildConfigurationSetTask(
            BuildGraph buildGraph,
            BuildOptions buildOptions) throws CoreException {

        // Check if the given build set task is empty and update the status message appropriately
        if (buildGraph.getBuildTasks().isEmpty()) {
            return Optional.of(new ValidationStatus(BuildStatus.REJECTED, "Build config set is empty"));
        }

        // check if no rebuild is required
        if (!buildOptions.isForceRebuild()) {
            boolean noRebuildsRequired = checkIfNoRebuildIsRequired(buildGraph,
                    buildOptions.isImplicitDependenciesCheck(),
                    buildOptions.isTemporaryBuild(),
                    buildOptions.getAlignmentPreference());
            if (noRebuildsRequired) {
                return Optional.of(new ValidationStatus(BuildStatus.NO_REBUILD_REQUIRED,
                        "All build configs were previously built"));
            }
        }

        // check if there are cycles
        if (!buildGraph.isDAG()) {
            return Optional.of(new ValidationStatus(BuildStatus.REJECTED, "Build config set has a cycle"));
        }
        return Optional.empty();
    }

    @AllArgsConstructor
    private class ValidationStatus {

        BuildStatus buildStatus;
        String description;
    }

    /**
     * Returns true if no build configurations needs a rebuild
     */
    private boolean checkIfNoRebuildIsRequired (BuildGraph buildGraph, boolean checkImplicitDependencies, boolean temporaryBuild, AlignmentPreference alignmentPreference)
            throws CoreException {
        Collection<RemoteBuildTask> buildTasks = buildGraph.getBuildTasks();
        AtomicLong requiresRebuild = new AtomicLong(0);

        Set<Integer> processedDependenciesCache = new HashSet<>();

        buildTasks.stream().filter(bt -> !bt.isAlreadyRunning()).forEach(buildTask -> {
            BuildConfiguration buildConfiguration = buildTask.getBuildConfigurationAudited().getBuildConfiguration();
            BuildConfigurationAudited buildConfigurationAudited = datastoreAdapter.getLatestBuildConfigurationAuditedInitializeBCDependencies(
                    buildConfiguration.getId());
            if (!datastoreAdapter.requiresRebuild(buildConfigurationAudited,
                    checkImplicitDependencies,
                    temporaryBuild,
                    alignmentPreference,
                    processedDependenciesCache)) {
                requiresRebuild.incrementAndGet();
            }
        });
        log.debug("{} configurations require a rebuild.", requiresRebuild);
        return requiresRebuild.get() == 0;
    }

    /**
     * Remove NO_REBUILD_REQUIRED them from the buildSetTask and return them.
     *
     * @return NO_REBUILD_REQUIRED tasks
     */
    private Collection<RemoteBuildTask> removeNRRTasks(BuildGraph buildGraph, BuildOptions buildOptions){
        Set<RemoteBuildTask> toBuild = new HashSet<>();
        Set<RemoteBuildTask> notToBuild = new HashSet<>();

        List<RemoteBuildTask> buildTasks = buildGraph.getBuildTasksChildrenFirst();
        for (RemoteBuildTask task : buildTasks) {
            if (CollectionUtils.containsAny(buildGraph.getChildren(task), toBuild) || datastoreAdapter.requiresRebuild(
                    task.getBuildConfigurationAudited(),
                    buildOptions.isImplicitDependenciesCheck(),
                    buildOptions.isTemporaryBuild(),
                    buildOptions.getAlignmentPreference(),
                    new HashSet<>()
            )) {
                toBuild.add(task);
            } else {
                notToBuild.add(task);
            }
        }

        notToBuild.forEach(task -> {
            // NOTE: after removal NRR task can still be referenced as a dependency of other tasks
            buildGraph.remove(task);
        });
        return notToBuild;
    }

    @Override
    public boolean cancel (String buildTaskId) throws CoreException { //TODO
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

    @Override public Optional<BuildTaskContext> getMDCMeta (String buildTaskId){
        return getSubmittedBuildTasks().stream()
                .filter(buildTask -> buildTaskId.equals(buildTask.getId()))
                .map(this::getMDCMeta)
                .findAny();
    }

    private BuildTaskContext getMDCMeta (BuildTask buildTask){
        boolean temporaryBuild = buildTask.getBuildOptions().isTemporaryBuild();
        return new BuildTaskContext(buildTask.getContentId(),
                buildTask.getUser().getId().toString(),
                temporaryBuild,
                ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), temporaryBuild));
    }

    @Override
    public boolean cancelSet ( int buildConfigSetRecordId){ //TODO
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

    private void monitorCancellation (BuildTask buildTask){
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

    private void cancelInternal (BuildTask buildTask){

        BuildResult result = new BuildResult(CompletionStatus.CANCELLED,
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

    public void updateBuildTaskStatus (BuildTask task, BuildCoordinationStatus status){
        updateBuildTaskStatus(task, task.getStatus(), status, null);
    }

    private void updateBuildTaskStatus (
            RemoteBuildTask task,
            BuildCoordinationStatus oldStatus,
            BuildCoordinationStatus status,
            String statusDescription) {

        task.setStatus(status); //TODO what is needed in the Build Task notifications
        task.setStatusDescription(statusDescription);

        Build build = buildMapper.fromBuildTask(task);
        BuildStatusChangedEvent buildStatusChanged = new DefaultBuildStatusChangedEvent(build,
                BuildStatus.fromBuildCoordinationStatus(oldStatus),
                BuildStatus.fromBuildCoordinationStatus(status));
        log.debug("Updated build task {} status to {}; old coord status: {}, new coord status: {}",
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
            log.debug("Fired buildStatusChangedEventNotifier after task {} status update to {}.",
                    task.getId(),
                    status);
        }
    }

    /**
     * update status,
     * store BuildConfigSetRecord,
     * sendSetStatusChangeEvent
     *
     * @return
     */
    private BuildConfigSetRecord storeBuildBuildConfingSetRecord (BuildConfigurationSet buildConfigurationSet, BuildStatus
    status, String description, User user, BuildOptions buildOptions)
        throws CoreException {

        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .startTime(new Date())
                .status(org.jboss.pnc.enums.BuildStatus.BUILDING)
                .temporaryBuild(buildOptions.isTemporaryBuild())
                .alignmentPreference(buildOptions.getAlignmentPreference())
                .build();

        updateBuildConfigSetRecordStatus(buildConfigSetRecord, status, description);
        return buildConfigSetRecord;
    }

    public void updateBuildConfigSetRecordStatus (BuildConfigSetRecord setRecord, BuildStatus status, String
    description)
        throws CoreException {
        log.info("Setting new status {} on buildConfigSetRecord.id {}. Description: {}.",
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

        sendSetStatusChangeEvent(status, oldStatus, setRecord, description); //TODO try to move UP
    }

    private void sendSetStatusChangeEvent (BuildStatus status, BuildStatus oldStatus, BuildConfigSetRecord
    record, String description){
        BuildSetStatusChangedEvent event = new DefaultBuildSetStatusChangedEvent(oldStatus,
                status,
                groupBuildMapper.toDTO(record),
                description);
        log.debug("Notifying build set status update {}.", event);
        buildSetStatusChangedEventNotifier.fire(event);
    }

    public void completeNoBuild (RemoteBuildTask buildTask, CompletionStatus completionStatus){
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

    public void completeBuild(BuildTask buildTask, BuildResult buildResult){
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
                            log.debug("[buildTaskId: {}] Storing build result with exception {}.",
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
                                exception = new ProcessException(
                                        "Build completed with system error but no exception.");
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
                        log.debug("[buildTaskId: {}] Storing failed build result. FailedReasonStatus: {}",
                                buildTaskId,
                                operationCompletionStatus);
                        datastoreAdapter.storeResult(buildTask, buildResult);
                        coordinationStatus = BuildCoordinationStatus.CANCELLED;
                        break;

                    case FAILED:
                        log.debug("[buildTaskId: {}] Storing failed build result. FailedReasonStatus: {}",
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

    public List<BuildTask> getSubmittedBuildTasks () {
        return null; //TODO
        //        return new ArrayList<>(taskRepository.getUnfinishedTasks());
    }

    @Override public List<BuildTask> getSubmittedBuildTasksBySetId ( int buildConfigSetRecordId){
        return taskRepository.getBuildTasksByBCSRId(buildConfigSetRecordId);
    }

    @PostConstruct
    public void start () {
        log.info("The application is starting ...");
    }

    @PreDestroy
    public void destroy () {
        log.info("The application is shutting down ...");
    }

    /**
     * @throws BuildConflictException if all BCAs are in the unfinishedTasks.
     */
    private void checkAllRunning(Collection < BuildConfigurationAudited > BCAs, Collection < BuildTaskRef > unfinishedTasks)
            throws BuildConflictException {

        Set<IdRev> unfinished = unfinishedTasks.stream().map(t -> t.getIdRev()).collect(Collectors.toUnmodifiableSet());

        Set<IdRev> running = BCAs.stream().map(bca -> bca.getIdRev()).filter(unfinished::contains).collect(Collectors.toSet());

        if (running.size() == BCAs.size()) {
            String runningMessage = running.stream().map(IdRev::toString).collect(Collectors.joining(", "));
            throw new BuildConflictException("Active build task found using the same configuration BC(s): " + runningMessage);
        }
    }

    /**
     * @throws BuildConflictException
     */
    private void checkIfAnyDependencyOfAlreadyRunningIsSubmitted(BuildGraph buildGraph) throws BuildConflictException {
        for (RemoteBuildTask parent : buildGraph.getBuildTasks()) {
            if (parent.isAlreadyRunning()) {
                Collection<RemoteBuildTask> children = buildGraph.getChildren(parent);
                for (RemoteBuildTask child : children) {
                    if (!child.isAlreadyRunning()) {
                        throw new BuildConflictException(
                                "Submitted build " + parent.getBuildConfigurationAudited().getName()
                                        + " is a dependency of already running build: "
                                        + child.getBuildConfigurationAudited().getName());
                    }
                }
            }
        }
    }
}
