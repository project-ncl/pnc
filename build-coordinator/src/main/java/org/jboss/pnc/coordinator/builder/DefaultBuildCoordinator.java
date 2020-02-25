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
package org.jboss.pnc.coordinator.builder;

import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.common.concurrent.NamedThreadFactory;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.mdc.BuildTaskContext;
import org.jboss.pnc.common.mdc.MDCUtils;
import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.common.util.ProcessStageUtils;
import org.jboss.pnc.coordinator.BuildCoordinationException;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.dto.Build;
import org.jboss.pnc.spi.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.spi.dto.BuildEnvironment;
import org.jboss.pnc.spi.dto.ProjectRef;
import org.jboss.pnc.spi.dto.RepositoryConfiguration;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repour.RepourResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.CollectionUtils.hasCycle;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@ApplicationScoped
public class DefaultBuildCoordinator implements BuildCoordinator {

    private final Logger log = LoggerFactory.getLogger(DefaultBuildCoordinator.class);
    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.build-process-status-update");

    private SystemConfig systemConfig;
    private DatastoreAdapter datastoreAdapter;
    private Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier;
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    private BuildScheduler buildScheduler;

    private BuildQueue buildQueue;

    private BuildTasksInitializer buildTasksInitializer;

    // Lock so that only one build method is active at any time
    private final Object buildMethodLock = new Object();


    @Deprecated
    public DefaultBuildCoordinator(){} //workaround for CDI constructor parameter injection

    @Inject
    public DefaultBuildCoordinator(
            DatastoreAdapter datastoreAdapter,
            Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier,
            Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier,
            BuildSchedulerFactory buildSchedulerFactory,
            BuildQueue buildQueue,
            SystemConfig systemConfig) {
        this.datastoreAdapter = datastoreAdapter;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
        this.buildScheduler = buildSchedulerFactory.getBuildScheduler();
        this.systemConfig = systemConfig;
        this.buildQueue = buildQueue;
        this.buildTasksInitializer = new BuildTasksInitializer(datastoreAdapter, systemConfig.getTemporalBuildExpireDate());
    }

    /**
     * Run a single build.  Uses the settings from the latest saved/audited build configuration.
     *
     * @param buildConfiguration The build configuration which will be used.  The latest version of this
     * build config will be built.
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build task
     * @throws BuildConflictException If there is already a build running with the same build configuration Id and version
     */
    @Override
    public BuildSetTask build(BuildConfiguration buildConfiguration,
                           User user, BuildOptions buildOptions) throws BuildConflictException {
        BuildConfigurationAudited buildConfigurationAudited = datastoreAdapter.getLatestBuildConfigurationAuditedInitializeBCDependencies(buildConfiguration.getId());
        return build0(user, buildOptions, buildConfigurationAudited);
    }

    private BuildSetTask build0(User user, BuildOptions buildOptions, BuildConfigurationAudited buildConfigurationAudited) throws BuildConflictException {
        synchronized (buildMethodLock) {
            checkNotRunning(buildConfigurationAudited);

            BuildSetTask buildSetTask =
                    buildTasksInitializer.createBuildSetTask(
                            buildConfigurationAudited,
                            user,
                            buildOptions,
                            this::buildRecordIdSupplier,
                            buildQueue.getUnfinishedTasks());

            buildQueue.enqueueTaskSet(buildSetTask);
            buildSetTask.getBuildTasks().forEach(this::addTaskToBuildQueue);

            return buildSetTask;
        }
    }

    private void checkNotRunning(BuildConfigurationAudited buildConfigurationAudited) throws BuildConflictException {
        if (buildQueue.getUnfinishedTask(buildConfigurationAudited).isPresent()) {
            throw new BuildConflictException("Active build task found using the same configuration BC [id=" + buildConfigurationAudited.getId() +", rev=]" + buildConfigurationAudited.getRev());
        }
    }

    /**
     * Run a single build.  Uses the settings from the specific revision of a BuildConfiguration.
     * The dependencies are resolved by the BuildConfiguration relations and are used in the latest revisions
     *
     * @param buildConfigurationAudited A revision of a BuildConfiguration which will be used.
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build task
     * @throws BuildConflictException If there is already a build running with the same build configuration Id and revision
     */
    @Override
    public BuildSetTask build(BuildConfigurationAudited buildConfigurationAudited,
                              User user,
                              BuildOptions buildOptions) throws BuildConflictException {
        return build0(user, buildOptions, buildConfigurationAudited);
    }

    private Integer buildRecordIdSupplier() {
        return datastoreAdapter.getNextBuildRecordId();
    }

    /**
     * Run a set of builds.  Only the current/latest version of builds in the given set will be executed.  The
     * order of execution is determined by the dependency relations between the build configurations.
     *
     * @param buildConfigurationSet The set of builds to be executed.
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build set task
     * @throws CoreException Thrown if there is a problem initializing the build
     */
    @Override
    public BuildSetTask build(
            BuildConfigurationSet buildConfigurationSet,
            User user, BuildOptions buildOptions) throws CoreException {
        synchronized (buildMethodLock) {
            BuildSetTask buildSetTask = buildTasksInitializer.createBuildSetTask(
                    buildConfigurationSet,
                    user,
                    buildOptions,
                    this::buildRecordIdSupplier,
                    buildQueue.getUnfinishedTasks());
            updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.NEW);

            validateAndEnqueueBuildConfigurationSetTasks(buildConfigurationSet, buildOptions, buildSetTask);
            return buildSetTask;
        }
    }

    /**
     * Run a build of BuildConfigurationSet with BuildConfigurations in specific versions.
     *
     * The set of the BuildConfigurations to be executed is determined by the buildConfigurationSet entity.
     * If there is a revision of a BuildConfiguration available in the buildConfigurationAuditedsMap parameter, then
     * this revision is executed. If it's not available, latest revision of the BuildConfiguration will be executed.
     *
     * @param buildConfigurationSet The set of the configurations to be executed
     * @param buildConfigurationAuditedsMap A map BuildConfiguration::id:BuildConfigurationAudited of specific revisions of BuildConfigurations contained in the buildConfigurationSet
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build set task
     * @throws CoreException Thrown if there is a problem initializing the build
     */
    @Override
    public BuildSetTask build(BuildConfigurationSet buildConfigurationSet,
                              Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap,
                              User user, BuildOptions buildOptions) throws CoreException {

        synchronized (buildMethodLock) {
            BuildSetTask buildSetTask = buildTasksInitializer.createBuildSetTask(
                    buildConfigurationSet,
                    buildConfigurationAuditedsMap,
                    user,
                    buildOptions,
                    this::buildRecordIdSupplier,
                    buildQueue.getUnfinishedTasks());
            updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.NEW);

            validateAndEnqueueBuildConfigurationSetTasks(buildConfigurationSet, buildOptions, buildSetTask);
            return buildSetTask;
        }
    }

    private void validateAndEnqueueBuildConfigurationSetTasks(BuildConfigurationSet buildConfigurationSet, BuildOptions buildOptions, BuildSetTask buildSetTask) {
        checkForEmptyBuildSetTask(buildSetTask);
        if (!buildOptions.isForceRebuild()) {
            checkIfAnyBuildConfigurationNeedsARebuild(buildSetTask,
                    buildConfigurationSet,
                    buildOptions.isImplicitDependenciesCheck(),
                    buildOptions.isTemporaryBuild());
        }

        checkForCyclicDependencies(buildSetTask);
        build(buildSetTask);
    }

    private void checkIfAnyBuildConfigurationNeedsARebuild(BuildSetTask buildSetTask,
            BuildConfigurationSet buildConfigurationSet,
            boolean checkImplicitDependencies,
            boolean temporaryBuild) {
        Set<BuildConfiguration> buildConfigurations = buildConfigurationSet.getBuildConfigurations();
        int requiresRebuild = buildConfigurations.size();
        log.debug("There are {} configurations in a set {}.", requiresRebuild, buildConfigurationSet.getId());

        Set<Integer> processedDependenciesCache = new HashSet<Integer>();
        for (BuildConfiguration buildConfiguration : buildConfigurations) {
            BuildConfigurationAudited buildConfigurationAudited =
                    datastoreAdapter.getLatestBuildConfigurationAuditedInitializeBCDependencies(buildConfiguration.getId());
            if (!datastoreAdapter.requiresRebuild(buildConfigurationAudited, checkImplicitDependencies, temporaryBuild, processedDependenciesCache)) {
                requiresRebuild--;
            }
        }
        if (requiresRebuild == 0) {
            updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.REJECTED, "All build configs were previously built");
        }
    }

    private void build(BuildSetTask buildSetTask) {

        synchronized (buildMethodLock) {

            if (!BuildSetStatus.REJECTED.equals(buildSetTask.getStatus())) {
                buildQueue.enqueueTaskSet(buildSetTask);
                buildSetTask.getBuildTasks().stream()
                        .filter(this::rejectAlreadySubmitted)
                        .forEach(this::addTaskToBuildQueue);
            }

        }
    }

    private void addTaskToBuildQueue(BuildTask buildTask) {

        MDCUtils.addContext(getMDCMeta(buildTask));

        if (isBuildConfigurationAlreadyInQueue(buildTask)) {
            log.debug("Skipping buildTask {}, its buildConfiguration is already in the buildQueue.", buildTask);
            return;
        }
        log.debug("Adding buildTask {} to buildQueue.", buildTask);

        if (buildTask.readyToBuild()) {
            updateBuildTaskStatus(buildTask, BuildCoordinationStatus.ENQUEUED);
            buildQueue.addReadyTask(buildTask);
            ProcessStageUtils.logProcessStageBegin(BuildCoordinationStatus.ENQUEUED.toString());
        } else {
            updateBuildTaskStatus(buildTask, BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES);
            Runnable onTaskReady = () -> {

                // we need to add this because we need to re-put those metadata in the thread context since the
                // current thread context probably belongs to another build
                MDCUtils.addContext(getMDCMeta(buildTask));

                ProcessStageUtils.logProcessStageEnd(BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES.toString());
                updateBuildTaskStatus(buildTask, BuildCoordinationStatus.ENQUEUED);
                ProcessStageUtils.logProcessStageBegin(BuildCoordinationStatus.ENQUEUED.toString());

                MDCUtils.clear();
            };
            buildQueue.addWaitingTask(buildTask, onTaskReady);
            ProcessStageUtils.logProcessStageBegin(BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES.toString());
        }

        MDCUtils.clear();
    }

    private boolean isBuildConfigurationAlreadyInQueue(BuildTask buildTask) {

        MDCUtils.addContext(getMDCMeta(buildTask));

        BuildConfigurationAudited buildConfigurationAudited = buildTask.getBuildConfigurationAudited();
        Optional<BuildTask> unfinishedTask = buildQueue.getUnfinishedTask(buildConfigurationAudited);
        if (unfinishedTask.isPresent()) {
            log.debug("Task with the same buildConfigurationAudited is in the queue {}.", unfinishedTask.get());
            MDCUtils.clear();
            return true;
        } else {
            MDCUtils.clear();
            return false;
        }
    }

    @Override
    public boolean cancel(int buildTaskId) throws CoreException {
        Optional<BuildTask> taskOptional = getSubmittedBuildTasks().stream()
                    .filter(buildTask -> buildTask.getId() == buildTaskId)
                    .findAny();
        if (taskOptional.isPresent()) {
            MDCUtils.addContext(getMDCMeta(taskOptional.get()));
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
            MDCUtils.clear();
            return true;
        } else {
            log.warn("Cannot find task {} to cancel.", buildTaskId);
            MDCUtils.clear();
            return false;
        }
    }

    @Override
    public Optional<BuildTaskContext> getMDCMeta(Integer buildTaskId) {
        return getSubmittedBuildTasks()
                .stream()
                .filter(buildTask -> buildTaskId.equals(buildTask.getId()))
                .map(this::getMDCMeta)
                .findAny();
    }

    private BuildTaskContext getMDCMeta(BuildTask buildTask) {
        return new BuildTaskContext(
                buildTask.getContentId(),
                buildTask.getBuildOptions().isTemporaryBuild(),
                systemConfig.getTemporalBuildExpireDate());
    }

    @Override
    public boolean cancelSet(int buildSetTaskId) {
        BuildConfigSetRecord record = datastoreAdapter.getBuildCongigSetRecordById(buildSetTaskId);
        if (record == null) {
            log.error("Could not find buildConfigSetRecord with id : {}", buildSetTaskId);
            return false;
        }
        log.debug("Cancelling Build Configuration Set: {}",buildSetTaskId);
        getSubmittedBuildTasks().stream()
                .filter(t -> t != null)
                .filter(t -> t.getBuildSetTask() != null
                        && t.getBuildSetTask().getId().equals(buildSetTaskId))
                .forEach(buildTask -> {
                    try {
                        MDCUtils.addContext(getMDCMeta(buildTask));
                        log.debug("Received cancel request for buildTaskId: {}.", buildTask.getId());
                        cancel(buildTask.getId());
                    } catch (CoreException e){
                        log.error("Unable to cancel the build [" + buildTask.getId() + "].",e);
                    }
                });
        record.setStatus(BuildStatus.CANCELLED);
        record.setEndTime(Date.from(Instant.now()));
        try {
            datastoreAdapter.saveBuildConfigSetRecord(record);
        } catch (DatastoreException e) {
            log.error("Failed to update BuildConfigSetRecord (id: {} ) with status CANCELLED",record.getId(),e);
        }
        return true;
    }

    private void monitorCancellation(BuildTask buildTask) {
        int cancellationTimeout = 30;
        PullingMonitor monitor = new PullingMonitor();

        Runnable invokeCancelInternal = () -> {

            MDCUtils.addContext(getMDCMeta(buildTask));

            if (!getSubmittedBuildTasks().contains(buildTask)) {
                log.debug("Task {} cancellation already completed.", buildTask.getId());
                return;
            }
            log.warn("Cancellation did not complete in {} seconds.", cancellationTimeout);
            cancelInternal(buildTask);

            MDCUtils.clear();
        };
        ScheduledFuture<?> timer = monitor.timer(invokeCancelInternal, cancellationTimeout, TimeUnit.SECONDS);
        //TODO optimization: cancel the timer when the task is canceled
        //timer.cancel(false);
    }

    private void cancelInternal(BuildTask buildTask) {

        MDCUtils.addContext(getMDCMeta(buildTask));

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
        //TODO 2.0 completeNoBuild(buildTask, CompletionStatus.CANCELLED); NCL-4242

        log.info("Task {} canceled internally.", buildTask.getId());

        MDCUtils.clear();
    }

    private void checkForCyclicDependencies(BuildSetTask buildSetTask) {
        Set<BuildTask> buildTasks = buildSetTask.getBuildTasks();
        if (hasCycle(buildTasks, BuildTask::getDependencies)) {
            updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.REJECTED, "Build config set has a cycle");
        }
    }

    /**
     * Check if the given build set task is empty and update the status message appropriately
     */
    private void checkForEmptyBuildSetTask(BuildSetTask buildSetTask) {
        if (buildSetTask.getBuildTasks() == null || buildSetTask.getBuildTasks().isEmpty()) {
            updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.REJECTED, "Build config set is empty");
        }
    }

    private boolean rejectAlreadySubmitted(BuildTask buildTask) {

        MDCUtils.addContext(getMDCMeta(buildTask));

        Optional<BuildTask> alreadyActiveBuildTask = buildQueue.getUnfinishedTask(buildTask.getBuildConfigurationAudited());
        if (alreadyActiveBuildTask.isPresent()) {
            updateBuildTaskStatus(buildTask, BuildCoordinationStatus.REJECTED,
                    "The configuration is already in the build queue.");
            MDCUtils.clear();
            return false;
        } else {
            MDCUtils.clear();
            return true;
        }
    }

    public void updateBuildTaskStatus(BuildTask task, BuildCoordinationStatus status){
        updateBuildTaskStatus(task, status, null);
    }

    private void updateBuildTaskStatus(BuildTask task, BuildCoordinationStatus status, String statusDescription){

        MDCUtils.addContext(getMDCMeta(task));
        BuildCoordinationStatus oldStatus = task.getStatus();

        BuildConfigurationAudited buildConfigurationAudited = task.getBuildConfigurationAudited();
        Project projectDb = task.getBuildConfigurationAudited().getProject();
        ProjectRef project = new ProjectRef(
                projectDb.getId(),
                projectDb.getName(),
                projectDb.getDescription(),
                projectDb.getIssueTrackerUrl(),
                projectDb.getProjectUrl()
        );

        org.jboss.pnc.model.RepositoryConfiguration repositoryConfiguration = task.getBuildConfigurationAudited()
                .getRepositoryConfiguration();
        RepositoryConfiguration repository = new RepositoryConfiguration(
                repositoryConfiguration.getId(),
                repositoryConfiguration.getInternalUrl(),
                repositoryConfiguration.getExternalUrl(),
                repositoryConfiguration.isPreBuildSyncEnabled()
        );

        org.jboss.pnc.model.BuildEnvironment buildEnvironmentDb = buildConfigurationAudited.getBuildEnvironment();
        BuildEnvironment buildEnvironment = new BuildEnvironment(
                buildEnvironmentDb.getId(),
                buildEnvironmentDb.getName(),
                buildEnvironmentDb.getDescription(),
                buildEnvironmentDb.getSystemImageRepositoryUrl(),
                buildEnvironmentDb.getSystemImageId(),
                buildEnvironmentDb.getAttributes(),
                buildEnvironmentDb.getSystemImageType(),
                buildEnvironmentDb.isDeprecated()
        );

        User userDb = task.getUser();
        org.jboss.pnc.spi.dto.User user = new org.jboss.pnc.spi.dto.User(
                userDb.getId(),
                userDb.getUsername()
        );

        BuildConfigurationRevisionRef buildConfigurationRevisionRef = new BuildConfigurationRevisionRef(
                buildConfigurationAudited.getId(),
                buildConfigurationAudited.getRev(),
                buildConfigurationAudited.getName(),
                buildConfigurationAudited.getDescription(),
                buildConfigurationAudited.getBuildScript(),
                buildConfigurationAudited.getScmRevision()
        );

        List<Integer> dependants = task.getDependants().stream().map(t -> t.getId()).collect(Collectors.toList());
        List<Integer> dependencies = task.getDependencies().stream().map(t -> t.getId()).collect(Collectors.toList());
        Build build = new Build(
                project,
                repository,
                buildEnvironment,
                Collections.<String,String>emptyMap(),
                user,
                buildConfigurationRevisionRef,
                dependants,
                dependencies,
                task.getId(),
                status,
                task.getContentId(),
                task.getBuildOptions().isTemporaryBuild()
        );
        BuildCoordinationStatusChangedEvent buildStatusChanged = new DefaultBuildStatusChangedEvent(
                build,
                oldStatus,
                task.getStartTime(),
                task.getEndTime());

        log.debug("Updating build task {} status to {}", task.getId(), buildStatusChanged);
        if (status.isCompleted()) {
            markFinished(task,
                    status, statusDescription);
        } else {
            task.setStatus(status);
            task.setStatusDescription(statusDescription);
        }

        userLog.info("Build status updated to {}; previous: {}", status, oldStatus);

        buildStatusChangedEventNotifier.fire(buildStatusChanged);
        log.debug("Fired buildStatusChangedEventNotifier after task {} status update to {}.", task.getId(), status);
        MDCUtils.clear();
    }

    private void updateBuildSetTaskStatus(BuildSetTask buildSetTask, BuildSetStatus status) {
        updateBuildSetTaskStatus(buildSetTask, status, null);
    }

    private void updateBuildSetTaskStatus(BuildSetTask buildSetTask, BuildSetStatus status, String description) {
        log.info("Setting new status {} on buildSetTask.id {}. Description: {}.", status, buildSetTask.getId(), description);
        BuildSetStatus oldStatus = buildSetTask.getStatus();
        Optional<BuildConfigSetRecord> buildConfigSetRecord = buildSetTask.getBuildConfigSetRecord();
        sendSetStatusChangeEvent(buildSetTask, status, oldStatus, buildConfigSetRecord, description);

        // Rejected status needs to be propagated to the BuildConfigSetRecord in database. 
        // Completed BuildSets are updated using BuildSetTask#taskStatusUpdatedToFinalState()
        if( buildConfigSetRecord.isPresent() && BuildSetStatus.REJECTED.equals(status)) {
        	buildConfigSetRecord.get().setStatus(BuildStatus.REJECTED);
        	try {
        		datastoreAdapter.saveBuildConfigSetRecord(buildConfigSetRecord.get());
        	} catch (DatastoreException de) {
                log.warn("Failed to update build config set record to REJECTED status: " + de);
            }
        }
        
        buildSetTask.setStatus(status);
        buildSetTask.setStatusDescription(description);
    }

    private void sendSetStatusChangeEvent(BuildSetTask buildSetTask,
                                          BuildSetStatus status,
                                          BuildSetStatus oldStatus,
                                          Optional<BuildConfigSetRecord> maybeRecord,
                                          String description) {
        maybeRecord.ifPresent(record -> {
            Integer userId = Optional.ofNullable(record.getUser()).map(User::getId).orElse(null);

            BuildSetStatusChangedEvent event = new DefaultBuildSetStatusChangedEvent(
                    oldStatus,
                    status,
                    buildSetTask.getId(),
                    record.getBuildConfigurationSet().getId(),
                    record.getBuildConfigurationSet().getName(),
                    record.getStartTime(),
                    record.getEndTime(),
                    userId,
                    description);
            log.debug("Notifying build set status update {}.", event);
            buildSetStatusChangedEventNotifier.fire(event);
        });
    }

    private void processBuildTask(BuildTask task) {

        MDCUtils.addContext(getMDCMeta(task));

        Consumer<BuildResult> onComplete = (result) ->  completeBuild(task, result);

        try {
            //check if task is already been build or is currently building
            //in case when task depends on two or more other tasks, all dependents call this method
            //process only tasks with status ENQUEUED
            synchronized (task) {
                if (task.getStatus() != BuildCoordinationStatus.ENQUEUED) {
                    log.debug("Skipping the execution of build task {} as it has been processed already. Status: {}.", task.getId(), task.getStatus());
                    return;
                }

                if (!task.getBuildOptions().isForceRebuild() && !datastoreAdapter.requiresRebuild(task, new HashSet<Integer>())) {
                    completeNoBuild(task, CompletionStatus.NO_REBUILD_REQUIRED);
                    return;
                }
                task.setStartTime(new Date());
                updateBuildTaskStatus(task, BuildCoordinationStatus.BUILDING);
            }
            buildScheduler.startBuilding(task, onComplete);
        } catch (CoreException | ExecutorException e) {
            log.debug(" Build coordination task failed. Setting it as SYSTEM_ERROR.", e);
            updateBuildTaskStatus(task, BuildCoordinationStatus.SYSTEM_ERROR, e.getMessage());
            try {
                datastoreAdapter.storeResult(task, Optional.empty(), e);
            } catch (DatastoreException e1) {
                log.error("Unable to store error [" + e.getMessage() + "] of build coordination task [" + task.getId() + "].", e1);
            }
        } catch (Error error) {
            log.error("Build coordination task failed with error! Setting it as SYSTEM_ERROR.", error);
            log.error("The system probably is in an invalid state!");
            updateBuildTaskStatus(task,BuildCoordinationStatus.SYSTEM_ERROR, error.getMessage());
            try {
                datastoreAdapter.storeResult(task, Optional.empty(), error);
            } catch (DatastoreException e1) {
                log.error("Unable to store error [" + error.getMessage() + "] of build coordination task [" + task.getId() + "].", e1);
            }
            throw error;
        } finally {
            ProcessStageUtils.logProcessStageEnd(BuildCoordinationStatus.ENQUEUED.toString());
            MDCUtils.clear();
        }
    }

    public void completeNoBuild(BuildTask buildTask, CompletionStatus completionStatus) {

        MDCUtils.addContext(getMDCMeta(buildTask));

        int buildTaskId = buildTask.getId();
        BuildCoordinationStatus coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        try {
            if (CompletionStatus.NO_REBUILD_REQUIRED.equals(completionStatus)) {
                updateBuildTaskStatus(buildTask, BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
//TODO cancel should be here enable in 2.0 as CANCELed is not failed build
//            } else if (CompletionStatus.CANCELLED.equals(completionStatus)) {
//                updateBuildTaskStatus(buildTask, BuildCoordinationStatus.CANCELLED);
            } else {
                throw new BuildCoordinationException(String.format("Invalid status %s.", completionStatus));
            }

            log.debug("Storing no build required result.", buildTaskId);
            BuildRecord buildRecord = datastoreAdapter.storeRecordForNoRebuild(buildTask);
            if (buildRecord.getStatus().completedSuccessfully()) {
                coordinationStatus = BuildCoordinationStatus.DONE;
            } else {
                log.warn("[buildTaskId: {}] Something went wrong while storing the success result. The status has changed to {}.", buildTaskId, buildRecord.getStatus());
                coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
            }

        } catch (Throwable e ) {
            log.error("[buildTaskId: "+buildTaskId+"] Cannot store results to datastore.", e);
            coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        } finally {
            updateBuildTaskStatus(buildTask, coordinationStatus);
            MDCUtils.clear();
        }
    }

    public void completeBuild(BuildTask buildTask, BuildResult buildResult) {

        MDCUtils.addContext(getMDCMeta(buildTask));

        int buildTaskId = buildTask.getId();

        updateBuildTaskStatus(buildTask, BuildCoordinationStatus.BUILD_COMPLETED);

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
                                log.debug("[buildTaskId: {}] Storing build result with system error from repour: {}.",
                                        buildTaskId,
                                        repourResult.getLog());
                            } else {
                                exception = new ProcessException("Build completed with system error but no exception.");
                                log.error("[buildTaskId: {}] Storing build result with system_error and missing exception.", buildTaskId);
                            }
                        } else {
                            exception = new ProcessException("Build completed with system error but no exception and no Repour result.");
                            log.error("[buildTaskId: {}] Storing build result with system_error no exception and no Repour result.", buildTaskId);
                        }
                        datastoreAdapter.storeResult(buildTask, Optional.of(buildResult), exception);
                        coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
                        break;

                    case CANCELLED:
                    case TIMED_OUT:
                        log.debug("[buildTaskId: {}] Storing failed build result. FailedReasonStatus: {}", buildTaskId, operationCompletionStatus);
                        datastoreAdapter.storeResult(buildTask, buildResult);
                        coordinationStatus = BuildCoordinationStatus.CANCELLED;
                        break;

                    case FAILED:
                        log.debug("[buildTaskId: {}] Storing failed build result. FailedReasonStatus: {}", buildTaskId, operationCompletionStatus);
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
                    log.warn("[buildTaskId: {}] Something went wrong while storing the success result. The status has changed to {}.", buildTaskId, buildRecord.getStatus());
                    coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
                }
            }
        } catch (Throwable e ) {
            log.error("[buildTaskId: "+buildTaskId+"] Cannot store results to datastore.", e);
            coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        } finally {
            updateBuildTaskStatus(buildTask, coordinationStatus);
            //Starts when the build execution completes
            ProcessStageUtils.logProcessStageEnd("FINALIZING_BUILD", "Finalizing completed.");
            MDCUtils.clear();
        }
    }

    private synchronized void markFinished(BuildTask task, BuildCoordinationStatus status, String statusDescription) {

        MDCUtils.addContext(getMDCMeta(task));

        log.debug("Finishing buildTask {}. Setting status {}.", task, status);
        buildQueue.removeTask(task);
        task.setStatus(status);
        task.setStatusDescription(statusDescription);
        switch (status) {
            case DONE:
            case REJECTED_ALREADY_BUILT:
                buildQueue.executeNewReadyTasks();
                break;
            case REJECTED:
            case REJECTED_FAILED_DEPENDENCIES:
            case SYSTEM_ERROR:
            case DONE_WITH_ERRORS:
            case CANCELLED:
                handleErroneousFinish(task);
                break;
            default:
                throw new IllegalArgumentException("Unhandled build task status: " + task.getStatus() + ". Build task: " + task);
        }

        BuildSetTask buildSetTask = task.getBuildSetTask();
        if (buildSetTask != null && buildSetTask.isFinished()) {
            completeBuildSetTask(buildSetTask);
        }

        MDCUtils.clear();
    }

    private void handleErroneousFinish(BuildTask failedTask) {
        MDCUtils.addContext(getMDCMeta(failedTask));

        BuildSetTask taskSet = failedTask.getBuildSetTask();
        if (taskSet != null) {
            log.debug("Finishing tasks in set {}, after failedTask {}.", taskSet, failedTask);
            taskSet.getBuildTasks().stream()
                    .filter(t -> isDependentOn(failedTask, t))
                    .filter(t -> !t.getStatus().isCompleted())
                    .forEach(t -> finishDueToFailedDependency(failedTask, t));
        }
        MDCUtils.clear();
    }

    /**
     * checks if the possible dependant depends on the possible dependency
     * @param dependency - possible dependency
     * @param dependant - task to be checked
     * @return true if dependant indeed depends on the dependency
     */
    private boolean isDependentOn(BuildTask dependency, BuildTask dependant) {
        return dependant.getDependencies().contains(dependency);
    }

    private void storeRejectedTask(BuildTask buildTask) {
        try {
            log.debug("Storing rejected task {}", buildTask);
            datastoreAdapter.storeRejected(buildTask);
        } catch (DatastoreException e) {
            log.error("Unable to store rejected task.", e);
        }
    }

    private void completeBuildSetTask(BuildSetTask buildSetTask) {
        log.debug("Completing buildSetTask {} ...", buildSetTask);
        buildQueue.removeSet(buildSetTask);
        buildSetTask.taskStatusUpdatedToFinalState();
        updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.DONE);

        buildSetTask.getBuildConfigSetRecord().ifPresent(r -> {
                    try {
                        datastoreAdapter.saveBuildConfigSetRecord(r);
                    } catch (DatastoreException e) {
                        log.error("Unable to save build config set record", e);
                    }
                }
        );
    }

    private void finishDueToFailedDependency(BuildTask failedTask, BuildTask dependentTask) {

        MDCUtils.addContext(getMDCMeta(dependentTask));

        log.debug("Finishing task {} due to a failed dependency.", dependentTask);
        buildQueue.removeTask(dependentTask);

        ProcessStageUtils.logProcessStageEnd(dependentTask.getStatus().toString(), "Ended due to failed dependency of " + failedTask.getContentId());

        if (failedTask.getStatus() == BuildCoordinationStatus.CANCELLED) {
            updateBuildTaskStatus(dependentTask, BuildCoordinationStatus.CANCELLED,
                    "Dependent build " + failedTask.getBuildConfigurationAudited().getName() + " was cancelled");
        }
        else {
            updateBuildTaskStatus(dependentTask, BuildCoordinationStatus.REJECTED_FAILED_DEPENDENCIES,
                    "Dependent build " + failedTask.getBuildConfigurationAudited().getName() + " failed.");
        }
        log.trace("Status of build task {} updated.", dependentTask);
        storeRejectedTask(dependentTask);

        MDCUtils.clear();
    }

    public List<BuildTask> getSubmittedBuildTasks() {
        return buildQueue.getSubmittedBuildTasks();
    }

    @PostConstruct
    public void start() {
        startThreads();
    }

    private void startThreads() {
        int threadPoolSize = systemConfig.getCoordinatorThreadPoolSize();
        ExecutorService executorService = MDCExecutors.newFixedThreadPool(threadPoolSize, new NamedThreadFactory("build-coordinator"));
        for (int i = 0; i < threadPoolSize; i++) {
            executorService.execute(this::takeAndProcessTask);
        }
    }

    private void takeAndProcessTask() {
        while (true) {
            try {
                BuildTask task = buildQueue.take();
                MDCUtils.addContext(getMDCMeta(task));
                processBuildTask(task);
                log.info("Build task: " + task + ", will pick up next task");
            } catch (InterruptedException e) {
                log.warn("BuildCoordinator thread interrupted. Possibly the system is being shut down", e);
                break;
            } finally {
                MDCUtils.clear();
            }
        }
    }
}
