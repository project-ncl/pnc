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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.common.util.NamedThreadFactory;
import org.jboss.pnc.coordinator.BuildCoordinationException;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.BuildScope;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.jboss.pnc.common.util.CollectionUtils.hasCycle;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@ApplicationScoped
public class DefaultBuildCoordinator implements BuildCoordinator {

    private final Logger log = LoggerFactory.getLogger(DefaultBuildCoordinator.class);

    private Configuration configuration;
    private DatastoreAdapter datastoreAdapter;
    private Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier;
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    private BuildScheduler buildScheduler;

    private BuildQueue buildQueue;

    private BuildTasksInitializer buildTasksInitializer;


    @Deprecated
    public DefaultBuildCoordinator(){} //workaround for CDI constructor parameter injection

    @Inject
    public DefaultBuildCoordinator(DatastoreAdapter datastoreAdapter, Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier,
                                   Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier, BuildSchedulerFactory buildSchedulerFactory,
                                   BuildQueue buildQueue,
                                   Configuration configuration) {
        this.datastoreAdapter = datastoreAdapter;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
        this.buildScheduler = buildSchedulerFactory.getBuildScheduler();
        this.configuration = configuration;
        this.buildQueue = buildQueue;
        this.buildTasksInitializer = new BuildTasksInitializer(datastoreAdapter);
    }

    /**
     * Run a single build.  Uses the settings from the latest saved/audited build configuration.
     *
     * @param buildConfiguration The build configuration which will be used.  The latest version of this
     * build config will be built.
     * @param user The user who triggered the build.
     * @param keepPodAliveAfterFailure Don't stop the pod in which the build is running after build failure
     * @param scope Build scope.
     *
     * @return The new build task
     * @throws BuildConflictException If there is already a build running with the same build configuration Id and version
     */
    @Override
    public BuildSetTask build(BuildConfiguration buildConfiguration,
                           User user,
                           BuildScope scope,
                           boolean keepPodAliveAfterFailure) throws BuildConflictException, CoreException {

        BuildConfigurationAudited auditedBuildConfig = datastoreAdapter.getLatestBuildConfigurationAudited(buildConfiguration.getId());
        Optional<BuildTask> alreadyActiveBuildTask = buildQueue.getTask(auditedBuildConfig);
        if (alreadyActiveBuildTask.isPresent()) {
            throw new BuildConflictException("Active build task found using the same configuration",
                    alreadyActiveBuildTask.get().getId());
        }

        BuildSetTask buildSetTask =
                buildTasksInitializer.createBuildSetTask(buildConfiguration, user, scope, keepPodAliveAfterFailure, this::buildRecordIdSupplier);

        buildQueue.enqueueTaskSet(buildSetTask);
        buildSetTask.getBuildTasks().forEach(buildQueue::enqueueTask);
        return buildSetTask;
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
     * @param forceRebuildAll Rebuild all configs in the set even if some of them have already been built
     * @param keepPodAliveAfterFailure Don't kill the pod after build failure
     *
     * @return The new build set task
     * @throws CoreException Thrown if there is a problem initializing the build
     */
    @Override
    public BuildSetTask build(
            BuildConfigurationSet buildConfigurationSet,
            User user,
            boolean keepPodAliveAfterFailure,
            boolean forceRebuildAll) throws CoreException {

        BuildSetTask buildSetTask = buildTasksInitializer.createBuildSetTask(
                buildConfigurationSet,
                user,
                forceRebuildAll,
                keepPodAliveAfterFailure,
                this::buildRecordIdSupplier);
        updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.NEW);
        checkForEmptyBuildSetTask(buildSetTask);
        checkForCyclicDependencies(buildSetTask);
        build(buildSetTask);
        return buildSetTask;
    }

    private void build(BuildSetTask buildSetTask) {
        if (!BuildSetStatus.REJECTED.equals(buildSetTask.getStatus())) {
            buildQueue.enqueueTaskSet(buildSetTask);
            buildSetTask.getBuildTasks().stream()
                    .filter(this::rejectAlreadySubmitted)
                    .forEach(buildQueue::enqueueTask);
        }
    }

    @Override
    public boolean cancel(int buildTaskId) throws CoreException {
        Optional<BuildTask> taskOptional = getSubmittedBuildTasks().stream()
                    .filter(buildTask -> buildTask.getId() == buildTaskId)
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

    private void monitorCancellation(BuildTask buildTask) {
        int cancellationTimeout = 30;
        PullingMonitor monitor = new PullingMonitor();

        Runnable invokeCancelInternal = () -> {
            if (!getSubmittedBuildTasks().contains(buildTask)) {
                log.debug("Task {} cancellation already completed.", buildTask.getId());
                return;
            }
            log.warn("Cancellation did not complete in {} seconds.", cancellationTimeout);
            cancelInternal(buildTask);
        };
        monitor.timer(invokeCancelInternal, cancellationTimeout, TimeUnit.SECONDS);
    }

    private void cancelInternal(BuildTask buildTask) {

        BuildResult result = new BuildResult(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(BuildExecutionStatus.CANCELLED),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        updateBuildStatus(buildTask, result);

        log.info("Task {} canceled internally.", buildTask.getId());
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
            if (buildSetTask.getBuildConfigurationSet().getBuildConfigurations() == null
                    || buildSetTask.getBuildConfigurationSet().getBuildConfigurations().isEmpty()) {
                updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.REJECTED, "Build config set is empty");
            } else {
                updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.REJECTED, "All build configs were previously built");
            }
        }
    }

    private boolean rejectAlreadySubmitted(BuildTask buildTask) {
        if (buildQueue.isBuildAlreadySubmitted(buildTask)) {
            updateBuildTaskStatus(buildTask, BuildCoordinationStatus.REJECTED,
                    "The configuration is already in the build queue.");
            return false;
        } else {
            return true;
        }
    }

    public void updateBuildTaskStatus(BuildTask task, BuildCoordinationStatus status){
        updateBuildTaskStatus(task, status, null);
    }

    private void updateBuildTaskStatus(BuildTask task, BuildCoordinationStatus status, String statusDescription){
        BuildCoordinationStatus oldStatus = task.getStatus();
        Integer userId = Optional.ofNullable(task.getUser()).map(User::getId).orElse(null);

        BuildCoordinationStatusChangedEvent buildStatusChanged = new DefaultBuildStatusChangedEvent(
                oldStatus,
                status,
                task.getId(),
                task.getBuildConfigurationAudited().getId().getId(),
                task.getBuildConfigurationAudited().getName(),
                task.getStartTime(),
                task.getEndTime(),
                userId);
        log.debug("Updating build task {} status to {}", task.getId(), buildStatusChanged);
        if (status.isCompleted()) {
            markFinished(task, status, statusDescription);
        } else {
            task.setStatus(status);
            task.setStatusDescription(statusDescription);
        }
        buildStatusChangedEventNotifier.fire(buildStatusChanged);
        log.debug("Fired buildStatusChangedEventNotifier after task {} status update to {}.", task.getId(), status);
    }

    private void updateBuildSetTaskStatus(BuildSetTask buildSetTask, BuildSetStatus status) {
        updateBuildSetTaskStatus(buildSetTask, status, null);
    }

    private void updateBuildSetTaskStatus(BuildSetTask buildSetTask, BuildSetStatus status, String description) {
        log.debug("Setting new status {} on buildSetTask.id {}.", status, buildSetTask.getId());
        BuildSetStatus oldStatus = buildSetTask.getStatus();
        Optional<BuildConfigSetRecord> buildConfigSetRecord = buildSetTask.getBuildConfigSetRecord();
        sendSetStatusChangeEvent(buildSetTask, status, oldStatus, buildConfigSetRecord);
        buildSetTask.setStatus(status);
        buildSetTask.setStatusDescription(description);
    }

    private void sendSetStatusChangeEvent(BuildSetTask buildSetTask,
                                          BuildSetStatus status,
                                          BuildSetStatus oldStatus,
                                          Optional<BuildConfigSetRecord> maybeRecord) {
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
                    userId);
            log.debug("Notifying build set status update {}.", event);
            buildSetStatusChangedEventNotifier.fire(event);
        });
    }

    private void processBuildTask(BuildTask task) {
        Consumer<BuildResult> onComplete = (result) ->  updateBuildStatus(task, result);

        try {
            //check if task is already been build or is currently building
            //in case when task depends on two other tasks, both call this method
            //process only tasks with status NEW
            synchronized (task) {
                if (!task.getStatus().equals(BuildCoordinationStatus.NEW)) {
                    log.debug("Skipping the execution of build task {} as it has been processed already.", task.getId());
                    return;
                }

                if (!task.isForcedRebuild() && !datastoreAdapter.requiresRebuild(task)) {
                    updateBuildTaskStatus(task,
                            BuildCoordinationStatus.REJECTED_ALREADY_BUILT,
                            "The configuration has already been built");
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
        }
    }


    public void updateBuildStatus(BuildTask buildTask, BuildResult buildResult) {
        int buildTaskId = buildTask.getId();

        updateBuildTaskStatus(buildTask, BuildCoordinationStatus.BUILD_COMPLETED);

        BuildCoordinationStatus coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        try {
            if (buildResult.hasFailed()) {
                if (buildResult.getException().isPresent()) {
                    ExecutorException exception = buildResult.getException().get();
                    log.debug("[buildTaskId: {}] Storing build result with exception {}.", buildTaskId, exception.getMessage());
                    datastoreAdapter.storeResult(buildTask, Optional.of(buildResult), exception);
                    coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
                } else if (buildResult.getFailedReasonStatus().isPresent()) {
                    log.debug("[buildTaskId: {}] Storing failed build result. FailedReasonStatus: {}", buildTaskId, buildResult.getFailedReasonStatus().get());
                    datastoreAdapter.storeResult(buildTask, buildResult);
                    if (buildResult.getFailedReasonStatus().get().equals(BuildExecutionStatus.CANCELLED)) {
                        coordinationStatus = BuildCoordinationStatus.CANCELED;
                    } else {
                        coordinationStatus = BuildCoordinationStatus.DONE_WITH_ERRORS;
                    }
                } else {
                    throw new BuildCoordinationException("Failed task should have set exception or failed reason status.");
                }
            } else {
                log.debug("[buildTaskId: {}] Storing success build result.", buildTaskId);
                datastoreAdapter.storeResult(buildTask, buildResult);
                coordinationStatus = BuildCoordinationStatus.DONE;
            }
        } catch (Throwable e ) {
            log.error("Cannot store results to datastore.", e);
            coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        } finally {
            updateBuildTaskStatus(buildTask, coordinationStatus);
        }
    }

    private synchronized void markFinished(BuildTask task, BuildCoordinationStatus status, String statusDescription) {
        task.setStatus(status);
        task.setStatusDescription(statusDescription);
        log.debug("Finishing buildTask {} ...", task);
        buildQueue.removeTask(task);
        switch (status) {
            case DONE:
            case REJECTED_ALREADY_BUILT:
                buildQueue.executeNewReadyTasks();
                break;
            case REJECTED:
            case REJECTED_FAILED_DEPENDENCIES:
            case SYSTEM_ERROR:
            case DONE_WITH_ERRORS:
            case CANCELED:
                handleErroneousFinish(task);
                break;
            default:
                throw new IllegalArgumentException("Unhandled build task status: " + task.getStatus() + ". Build task: " + task);
        }

        BuildSetTask buildSetTask = task.getBuildSetTask();
        if (buildSetTask != null && isFinished(buildSetTask)) {
            completeBuildSetTask(buildSetTask);
        }
    }

    private boolean isFinished(BuildSetTask buildSetTask) {
        return buildSetTask
                .getBuildTasks()
                .stream()
                .allMatch(t -> t.getStatus().isCompleted());
    }

    private void handleErroneousFinish(BuildTask failedTask) {
        BuildSetTask taskSet = failedTask.getBuildSetTask();
        if (taskSet != null) {
            taskSet.getBuildTasks().stream()
                    .filter(t -> isDependentOn(failedTask, t))
                    .forEach(t -> finishDueToFailedDependency(failedTask, t));
        }
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

    private void finishDueToFailedDependency(BuildTask failedTask, BuildTask task) {
        updateBuildTaskStatus(task, BuildCoordinationStatus.REJECTED_FAILED_DEPENDENCIES,
                "Dependent build " + failedTask.getBuildConfiguration().getName() + " failed.");
        storeRejectedTask(task);
        buildQueue.removeTask(task);
    }

    public List<BuildTask> getSubmittedBuildTasks() {
        return buildQueue.getSubmittedBuildTasks();
    }

    @PostConstruct
    public void start() {
        startThreads();
    }

    private void startThreads() {
        int threadPoolSize = 1;
        try {
            SystemConfig systemConfig = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class));
            threadPoolSize = systemConfig.getCoordinatorThreadPoolSize();
        } catch (ConfigurationParseException e) {
            log.error("Error parsing configuration. Will set BuildCoordinator.threadPoolSize to {}", threadPoolSize, e);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize, new NamedThreadFactory("build-coordinator"));
        for (int i = 0; i < threadPoolSize; i++) {
            executorService.execute(this::takeAndProcessTask);
        }
    }

    private void takeAndProcessTask() {
        while (true) {
            try {
                BuildTask task = buildQueue.take();
                processBuildTask(task);
                log.info("Build task: {}, will pick up next task");
            } catch (InterruptedException e) {
                log.warn("BuildCoordinator thread interrupted. Possibly the system is being shut down", e);
                break;
            }
        }
    }
}
