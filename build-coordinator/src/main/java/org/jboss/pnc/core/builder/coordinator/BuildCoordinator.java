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
package org.jboss.pnc.core.builder.coordinator;

import org.jboss.pnc.core.BuildCoordinationException;
import org.jboss.pnc.core.builder.coordinator.filtering.BuildTaskFilter;
import org.jboss.pnc.core.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@ApplicationScoped
public class BuildCoordinator {

    private final Logger log = LoggerFactory.getLogger(BuildCoordinator.class);

    /**
     * Build tasks which are either waiting to be run or currently running.
     * The task is removed from the queue when the build is complete and the results
     * are stored to the database.
     */
    private final Queue<BuildTask> submittedBuildTasks = new ConcurrentLinkedQueue<>(); //TODO garbage collector (time-out, error state)

    private DatastoreAdapter datastoreAdapter;
    private Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier;
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    private BuildScheduler buildScheduler;

    private Instance<BuildTaskFilter> taskFilters;

    @Deprecated
    public BuildCoordinator(){} //workaround for CDI constructor parameter injection

    @Inject
    public BuildCoordinator(DatastoreAdapter datastoreAdapter, Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier,
            Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier, BuildSchedulerFactory buildSchedulerFactory,
            Instance<BuildTaskFilter> taskFilters) {
        this.datastoreAdapter = datastoreAdapter;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
        this.buildScheduler = buildSchedulerFactory.getBuildScheduler();
        this.taskFilters = taskFilters;
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

        BuildConfigurationAudited buildConfigAudited = datastoreAdapter.getLatestBuildConfigurationAudited(buildConfiguration.getId());
        Optional<BuildTask> alreadyActiveBuildTask = this.getActiveBuildTask(buildConfigAudited);
        if (alreadyActiveBuildTask.isPresent()) {
            throw new BuildConflictException("Active build task found using the same configuration", alreadyActiveBuildTask.get().getId());
        }

        BuildTask buildTask = BuildTask.build(
                buildConfiguration,
                buildConfigAudited,
                user,
                getBuildStatusChangedEventNotifier(),
                (bt) -> processBuildTask(bt),
                datastoreAdapter.getNextBuildRecordId(),
                null,
                new Date(),
                rebuildAll,
                (bt) -> storeRejectedTask(bt));

        addTaskToQueue(buildTask);
        processBuildTask(buildTask);

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

        Consumer<BuildConfigSetRecord> onBuildSetTaskCompleted = (buildConfigSetRecord) -> {
            completeBuildSetTask(buildConfigSetRecord);
        };

        BuildTasksInitializer buildTasksInitializer = new BuildTasksInitializer(datastoreAdapter, Optional.of(buildSetStatusChangedEventNotifier));
        BuildSetTask buildSetTask = buildTasksInitializer.createBuildSetTask(
                buildConfigurationSet,
                user,
                rebuildAll,
                getBuildStatusChangedEventNotifier(),
                () -> datastoreAdapter.getNextBuildRecordId(),
                (bt) -> processBuildTask(bt),
                onBuildSetTaskCompleted,
                (bt) -> storeRejectedTask(bt));

        build(buildSetTask);
        return buildSetTask;
    }

    /**
     * Searches the active build tasks to see if there is already one running the give audited
     * build config.  If yes, returns the associated build task.  If none are found, returns null.
     * 
     * @param buildConfigAudited The build config to look for in the active build tasks
     * @return An Optional containing the matching build task if there is one.
     */
    private Optional<BuildTask> getActiveBuildTask(BuildConfigurationAudited buildConfigAudited) {
        return submittedBuildTasks.stream().filter(bt -> bt.getBuildConfigurationAudited().equals(buildConfigAudited)).findFirst();
    }

    private void build(BuildSetTask buildSetTask) {
        if (!BuildSetStatus.REJECTED.equals(buildSetTask.getStatus())) {
            buildSetTask.getBuildTasks().stream()
                    .filter((buildTask) -> rejectAlreadySubmitted(buildTask))
                    .map((buildTask) -> addTaskToQueue(buildTask))
                    .filter((buildTask) -> buildTask.readyToBuild())
                    .forEach(v -> processBuildTask(v));
        }
    }

    private BuildTask addTaskToQueue(BuildTask buildTask) {
        submittedBuildTasks.add(buildTask);
        return buildTask;
    }

    private boolean rejectAlreadySubmitted(BuildTask buildTask) {
        if (isBuildAlreadySubmitted(buildTask)) {
            buildTask.setStatus(BuildCoordinationStatus.REJECTED);
            buildTask.setStatusDescription("The configuration is already in the build queue.");
            return false;
        } else {
            return true;
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

    void processBuildTask(BuildTask buildTask) {
        Consumer<BuildResult> onComplete = (buildResult) -> {
            buildTask.setStatus(BuildCoordinationStatus.BUILD_COMPLETED);
            BuildCoordinationStatus coordinationStatus;
            try {
                if (buildResult.hasFailed()) {
                    if (buildResult.getException().isPresent()) {
                        ExecutorException exception = buildResult.getException().get();
                        datastoreAdapter.storeResult(buildTask, exception);
                        coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
                    } else if (buildResult.getFailedReasonStatus().isPresent()) {
                        datastoreAdapter.storeResult(buildTask, buildResult);
                        coordinationStatus = BuildCoordinationStatus.DONE_WITH_ERRORS;
                    } else {
                        removeSubmittedTask(buildTask);
                        throw new BuildCoordinationException("Failed task should have set exception or failed reason status.");
                    }
                } else {
                    datastoreAdapter.storeResult(buildTask, buildResult);
                    coordinationStatus = BuildCoordinationStatus.DONE;
                }
            } catch (DatastoreException | BuildCoordinationException e ) {
                log.error("Cannot store results to datastore.", e);
                coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
            }
            //remove before status update which could triggers further actions and cause dead lock
            removeSubmittedTask(buildTask);
            buildTask.setStatus(coordinationStatus);
        };

        try {
            //check if task is already been build or is currently building
            //in case when task depends on two other tasks, both call this method
            //process only tasks with status NEW
            synchronized (buildTask) {
                if (!buildTask.getStatus().equals(BuildCoordinationStatus.NEW)) {
                    log.debug("Skipping the execution of build task {} as it has been processed already.", buildTask.getId());
                    return;
                }

                log.info("BuildTask.id [{}]: Checking if task should be skipped(rebuildAll: {}, predicateResult: {}). Task is linked to BuildConfigurationAudited.IdRev {}.",
                        buildTask.getId(), buildTask.getRebuildAll(), prepareBuildTaskFilterPredicate().test(buildTask), buildTask.getBuildConfigurationAudited().getIdRev());
                if(!buildTask.getRebuildAll() && prepareBuildTaskFilterPredicate().test(buildTask)) {
                    log.info("[{}] Marking task as REJECTED_ALREADY_BUILT, because it has been already built", buildTask.getId());
                    removeSubmittedTask(buildTask);
                    buildTask.setStatus(BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
                    buildTask.setStatusDescription("The configuration has already been built.");
                    return;
                }
                buildTask.setStartTime(new Date());
                buildTask.setStatus(BuildCoordinationStatus.BUILDING); //status must be updated before startBuild as if build takes 0 time it complete before having Building status.
            }
            buildScheduler.startBuilding(buildTask, onComplete);
        } catch (CoreException | ExecutorException e) {
            log.debug(" Build coordination task failed. Setting it as SYSTEM_ERROR.", e);
            buildTask.setStatus(BuildCoordinationStatus.SYSTEM_ERROR);
            buildTask.setStatusDescription(e.getMessage());
            removeSubmittedTask(buildTask);
            try {
                datastoreAdapter.storeResult(buildTask, e);
            } catch (DatastoreException e1) {
                log.error("Unable to store error [" + e.getMessage() + "] of build coordination task [" + buildTask.getId() + "].", e1);
            }
        }
    }

    private void removeSubmittedTask(BuildTask buildTask) {
        log.trace("Removing task {} from submittedBuildTasks.", buildTask.getId());
        submittedBuildTasks.remove(buildTask);
    }

    public List<BuildTask> getSubmittedBuildTasks() {
        return Collections.unmodifiableList(submittedBuildTasks.stream().collect(Collectors.toList()));
    }

    @Deprecated //Used only in tests
    public boolean hasActiveTasks() {
        if (log.isTraceEnabled()) {
            String activeTasks = submittedBuildTasks.stream().map(bt -> bt.getId() + "-" + bt.getStatus()).collect(Collectors.joining(","));
            log.trace("Build Coordinator Active Tasks {}", activeTasks);
        }
        return submittedBuildTasks.peek() != null;
    }

    private boolean isBuildAlreadySubmitted(BuildTask buildTask) {
        return submittedBuildTasks.contains(buildTask);
    }

    Event<BuildCoordinationStatusChangedEvent> getBuildStatusChangedEventNotifier() {
        return buildStatusChangedEventNotifier;
    }

    private void completeBuildSetTask(BuildConfigSetRecord buildConfigSetRecord) {
        try {
            datastoreAdapter.saveBuildConfigSetRecord(buildConfigSetRecord);
        } catch (DatastoreException e) {
            log.error("Unable to save build config set record", e);
        }
    }

    private void storeRejectedTask(BuildTask buildTask) {
        removeSubmittedTask(buildTask);
        try {
            log.debug("Storing rejected task {}", buildTask);
            datastoreAdapter.storeRejected(buildTask);
        } catch (DatastoreException e) {
            log.error("Unable to store rejected task.", e);
        }
    }
}
