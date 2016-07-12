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
import org.jboss.pnc.coordinator.BuildCoordinationException;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.coordinator.builder.filtering.BuildTaskFilter;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildResult;
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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

    private Instance<BuildTaskFilter> taskFilters;

    private BuildQueue buildQueue;

    private Optional<BuildSetStatusChangedEvent> buildSetStatusChangedEvent;

    @Deprecated
    public DefaultBuildCoordinator(){} //workaround for CDI constructor parameter injection

    @Inject
    public DefaultBuildCoordinator(DatastoreAdapter datastoreAdapter, Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier,
                            Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier, BuildSchedulerFactory buildSchedulerFactory,
                            Instance<BuildTaskFilter> taskFilters, BuildQueue buildQueue,
                            Configuration configuration) {
        this.datastoreAdapter = datastoreAdapter;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
        this.buildScheduler = buildSchedulerFactory.getBuildScheduler();
        this.taskFilters = taskFilters;
        this.configuration = configuration;
        this.buildQueue = buildQueue;
    }

    /**
     * Run a single build.  Uses the settings from the latest saved/audited build configuration.
     *
     * @param buildConfiguration The build configuration which will be used.  The latest version of this
     * build config will be built.
     * @param user The user who triggered the build.
     * @param keepPodAliveAfterFailure Don't stop the pod in which the build is running after build failure
     * @param forceRebuild Run the build even if it has been already built
     *
     * @return The new build task
     * @throws BuildConflictException If there is already a build running with the same build configuration Id and version
     */
    public BuildTask build(BuildConfiguration buildConfiguration, User user,
                           boolean keepPodAliveAfterFailure, boolean forceRebuild) throws BuildConflictException {

        BuildConfigurationAudited config = datastoreAdapter.getLatestBuildConfigurationAudited(buildConfiguration.getId());
        Optional<BuildTask> alreadyActiveBuildTask = buildQueue.getTask(config);
        if (alreadyActiveBuildTask.isPresent()) {
            throw new BuildConflictException("Active build task found using the same configuration",
                    alreadyActiveBuildTask.get().getId());
        }

        BuildTask buildTask = BuildTask.build(
                buildConfiguration,
                config,
                keepPodAliveAfterFailure,
                user,
                datastoreAdapter.getNextBuildRecordId(),
                null,
                new Date(),
                buildConfiguration.getCurrentProductMilestone(),
                forceRebuild);

        buildQueue.enqueueTask(buildTask);

        return buildTask;
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
    public BuildSetTask build(BuildConfigurationSet buildConfigurationSet, User user,
                              boolean keepPodAliveAfterFailure, boolean forceRebuildAll) throws CoreException {

        BuildTasksInitializer buildTasksInitializer = new BuildTasksInitializer(datastoreAdapter);
        BuildSetTask buildSetTask = buildTasksInitializer.createBuildSetTask(
                buildConfigurationSet,
                user,
                forceRebuildAll,
                keepPodAliveAfterFailure,
                () -> datastoreAdapter.getNextBuildRecordId());
        updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.NEW);
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
        task.setStatus(status);
        task.setStatusDescription(statusDescription);
        if (status.isCompleted()) {
            markFinished(task);
        }
        buildStatusChangedEventNotifier.fire(buildStatusChanged);
        log.debug("Fired buildStatusChangedEventNotifier after task {} status update to {}.", task.getId(), status);
    }

    private void updateBuildSetTaskStatus(BuildSetTask buildSetTask, BuildSetStatus status){
        log.debug("Setting new status {} on buildSetTask.id {}.", status, buildSetTask.getId());
        BuildSetStatus oldStatus = buildSetTask.getStatus();
        Integer userId = Optional.ofNullable( buildSetTask.getBuildConfigSetRecord().getUser()).map(User::getId).orElse(null);

        buildSetStatusChangedEvent = Optional.of(new DefaultBuildSetStatusChangedEvent(
                oldStatus,
                status,
                buildSetTask.getId(),
                buildSetTask.getBuildConfigSetRecord().getBuildConfigurationSet().getId(),
                buildSetTask.getBuildConfigSetRecord().getBuildConfigurationSet().getName(),
                buildSetTask.getBuildConfigSetRecord().getStartTime(),
                buildSetTask.getBuildConfigSetRecord().getEndTime(),
                userId));
        log.debug("Notifying build set status update {}.", buildSetStatusChangedEvent);
        buildSetStatusChangedEventNotifier.fire(buildSetStatusChangedEvent.get());
        buildSetTask.setStatus(status);
    }

    private Predicate<BuildTask> prepareBuildTaskFilterPredicate() {
        Predicate<BuildTask> filteringPredicate = Objects::nonNull;
        if(!taskFilters.isUnsatisfied()) {
            for(BuildTaskFilter filter : taskFilters) {
                filteringPredicate = filteringPredicate.and(filter.filter());
            }
        }
        return filteringPredicate;
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

                log.info("BuildTask.id [{}]: Checking if task should be skipped(forceRebuild: {}, predicateResult: {}). Task is linked to BuildConfigurationAudited.IdRev {}.",
                        task.getId(), task.getForceRebuild(), prepareBuildTaskFilterPredicate().test(task), task.getBuildConfigurationAudited().getIdRev());
                if(!task.getForceRebuild() && prepareBuildTaskFilterPredicate().test(task)) {
                    log.info("[{}] Marking task as REJECTED_ALREADY_BUILT, because it has been already built", task.getId());
                    updateBuildTaskStatus(task, BuildCoordinationStatus.REJECTED_ALREADY_BUILT, "The configuration has already been built.");
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

        updateBuildTaskStatus(buildTask, BuildCoordinationStatus.BUILD_COMPLETED);

        BuildCoordinationStatus coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        try {
            if (buildResult.hasFailed()) {
                if (buildResult.getException().isPresent()) {
                    ExecutorException exception = buildResult.getException().get();
                    datastoreAdapter.storeResult(buildTask, Optional.of(buildResult), exception);
                    coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
                } else if (buildResult.getFailedReasonStatus().isPresent()) {
                    datastoreAdapter.storeResult(buildTask, buildResult);
                    coordinationStatus = BuildCoordinationStatus.DONE_WITH_ERRORS;
                } else {
                    throw new BuildCoordinationException("Failed task should have set exception or failed reason status.");
                }
            } else {
                datastoreAdapter.storeResult(buildTask, buildResult);
                coordinationStatus = BuildCoordinationStatus.DONE;
            }
        } catch (DatastoreException | BuildCoordinationException e ) {
            log.error("Cannot store results to datastore.", e);
            coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        } finally {
            updateBuildTaskStatus(buildTask, coordinationStatus);
        }
    }

    private synchronized void markFinished(BuildTask task) {
        buildQueue.removeTask(task);
        switch (task.getStatus()) {
            case DONE:
            case REJECTED_ALREADY_BUILT:
                buildQueue.executeNewReadyTasks();
                break;
            case REJECTED:
            case REJECTED_FAILED_DEPENDENCIES:
            case SYSTEM_ERROR:
            case DONE_WITH_ERRORS:
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
        buildQueue.removeSet(buildSetTask);
        buildSetTask.taskStatusUpdatedToFinalState();
        updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.DONE);
        buildSetStatusChangedEvent.ifPresent(buildSetStatusChangedEventNotifier::fire);
        try {
            datastoreAdapter.saveBuildConfigSetRecord(buildSetTask.getBuildConfigSetRecord());
        } catch (DatastoreException e) {
            log.error("Unable to save build config set record", e);
        }
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
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
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
