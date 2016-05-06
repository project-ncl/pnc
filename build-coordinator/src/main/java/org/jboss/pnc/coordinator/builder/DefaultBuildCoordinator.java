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
     * @param rebuildAll Run the build even if it has been already built
     *
     * @return The new build task
     * @throws BuildConflictException If there is already a build running with the same build configuration Id and version
     */
    public BuildTask build(BuildConfiguration buildConfiguration, User user, boolean rebuildAll) throws BuildConflictException {

        BuildConfigurationAudited config = datastoreAdapter.getLatestBuildConfigurationAudited(buildConfiguration.getId());
        Optional<BuildTask> alreadyActiveBuildTask = buildQueue.getTask(config);
        if (alreadyActiveBuildTask.isPresent()) {
            throw new BuildConflictException("Active build task found using the same configuration",
                    alreadyActiveBuildTask.get().getId());
        }

        BuildTask buildTask = BuildTask.build(
                buildConfiguration,
                config,
                user,
                getBuildStatusChangedEventNotifier(),
                datastoreAdapter.getNextBuildRecordId(),
                null,
                new Date(),
                rebuildAll);

        buildQueue.enqueueTask(buildTask);

        return buildTask;
    }

    /**
     * Run a set of builds.  Only the current/latest version of builds in the given set will be executed.  The
     * order of execution is determined by the dependency relations between the build configurations.
     *
     * @param buildConfigurationSet The set of builds to be executed.
     * @param user The user who triggered the build.
     * @return The new build set task
     * @throws CoreException Thrown if there is a problem initializing the build
     */
    public BuildSetTask build(BuildConfigurationSet buildConfigurationSet, User user, boolean rebuildAll) throws CoreException {

        BuildTasksInitializer buildTasksInitializer = new BuildTasksInitializer(datastoreAdapter, Optional.of(buildSetStatusChangedEventNotifier));
        BuildSetTask buildSetTask = buildTasksInitializer.createBuildSetTask(
                buildConfigurationSet,
                user,
                rebuildAll,
                getBuildStatusChangedEventNotifier(),
                () -> datastoreAdapter.getNextBuildRecordId());
        BuildSetStatusChangedEvent event = buildSetTask.setStatus(BuildSetStatus.NEW);
        buildSetStatusChangedEventNotifier.fire(event);

        build(buildSetTask);
        return buildSetTask;
    }

    private void build(BuildSetTask buildSetTask) {
        if (!BuildSetStatus.REJECTED.equals(buildSetTask.getStatus())) {
            buildQueue.enqueueTaskSet(buildSetTask);
        }
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

                log.info("BuildTask.id [{}]: Checking if task should be skipped(rebuildAll: {}, predicateResult: {}). Task is linked to BuildConfigurationAudited.IdRev {}.",
                        task.getId(), task.getRebuildAll(), prepareBuildTaskFilterPredicate().test(task), task.getBuildConfigurationAudited().getIdRev());
                if(!task.getRebuildAll() && prepareBuildTaskFilterPredicate().test(task)) {
                    log.info("[{}] Marking task as REJECTED_ALREADY_BUILT, because it has been already built", task.getId());
                    task.setStatus(BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
                    task.setStatusDescription("The configuration has already been built.");
                    markFinished(task);
                    return;
                }
                task.setStartTime(new Date());
                task.setStatus(BuildCoordinationStatus.BUILDING); //status must be updated before startBuild as if build takes 0 time it complete before having Building status.
            }
            buildScheduler.startBuilding(task, onComplete);
        } catch (CoreException | ExecutorException e) {
            log.debug(" Build coordination task failed. Setting it as SYSTEM_ERROR.", e);
            task.setStatus(BuildCoordinationStatus.SYSTEM_ERROR);
            task.setStatusDescription(e.getMessage());
            try {
                datastoreAdapter.storeResult(task, Optional.empty(), e);
            } catch (DatastoreException e1) {
                log.error("Unable to store error [" + e.getMessage() + "] of build coordination task [" + task.getId() + "].", e1);
            }
        }
    }

    public void updateBuildStatus(BuildTask buildTask, BuildResult buildResult) {

        buildTask.setStatus(BuildCoordinationStatus.BUILD_COMPLETED);

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
            buildTask.setStatus(coordinationStatus);
            markFinished(buildTask);
        }
    }

    public synchronized void markFinished(BuildTask task) {
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

    Event<BuildCoordinationStatusChangedEvent> getBuildStatusChangedEventNotifier() {
        return buildStatusChangedEventNotifier;
    }

    private void storeRejectedTask(BuildTask buildTask) {
        try {
            log.debug("Storing rejected task {}", buildTask);
            datastoreAdapter.storeRejected(buildTask);
        } catch (DatastoreException e) {
            log.error("Unable to store rejected task.", e);
        }
    }

    public void completeBuildSetTask(BuildSetTask buildSetTask) {
        buildQueue.removeSet(buildSetTask);
        Optional<BuildSetStatusChangedEvent> eventOption = buildSetTask.taskStatusUpdatedToFinalState();
        eventOption.ifPresent(buildSetStatusChangedEventNotifier::fire);
        try {
            datastoreAdapter.saveBuildConfigSetRecord(buildSetTask.getBuildConfigSetRecord());
        } catch (DatastoreException e) {
            log.error("Unable to save build config set record", e);
        }
    }

    public void finishDueToFailedDependency(BuildTask failedTask, BuildTask task) {
        task.setStatusDescription("Dependent build " + failedTask.getBuildConfiguration().getName() + " failed.");
        task.setStatus(BuildCoordinationStatus.REJECTED_FAILED_DEPENDENCIES);
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

    protected void startThreads() {
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
