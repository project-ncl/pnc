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

import org.jboss.pnc.core.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@ApplicationScoped
public class BuildCoordinator {

    private Logger log = LoggerFactory.getLogger(BuildCoordinator.class);

    /**
     * Build tasks which are either waiting to be run or currently running.
     * The task is removed from the queue when the build is complete and the results
     * are stored to the database.
     */
    private Queue<BuildTask> activeBuildTasks = new ConcurrentLinkedQueue<>(); //TODO garbage collector (time-out, error state)

    private DatastoreAdapter datastoreAdapter;
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    private BuildScheduler buildScheduler;


    @Deprecated
    public BuildCoordinator(){} //workaround for CDI constructor parameter injection

    @Inject
    public BuildCoordinator(DatastoreAdapter datastoreAdapter,
                            Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier,
                            Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier,
                            BuildScheduler buildScheduler) {
        this.datastoreAdapter = datastoreAdapter;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
        this.buildScheduler = buildScheduler;
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
                datastoreAdapter.getNextBuildRecordId(), //TODO in bpm case we are not storing this task ?
                null,
                new Date(),
                rebuildAll);

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

        BuildSetTask buildSetTask = createBuildSetTask(buildConfigurationSet, user, rebuildAll);

        build(buildSetTask);
        return buildSetTask;
    }

    public BuildSetTask createBuildSetTask(BuildConfigurationSet buildConfigurationSet, User user, boolean rebuildAll) throws CoreException {
        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .startTime(new Date())
                .status(org.jboss.pnc.model.BuildStatus.BUILDING)
                .build();

        try {
            buildConfigSetRecord = this.saveBuildConfigSetRecord(buildConfigSetRecord);
        } catch (DatastoreException e) {
            log.error("Failed to store build config set record: " + e);
            throw new CoreException(e);
        }

        Date buildSubmitTime = new Date();
        BuildSetTask buildSetTask = new BuildSetTask(
                this,
                buildConfigSetRecord,
                getProductMilestone(buildConfigurationSet),
                buildSubmitTime,
                rebuildAll);

        initializeBuildTasksInSet(buildSetTask, rebuildAll);
        return buildSetTask;
    }

    /**
     * Creates build tasks and sets up the appropriate dependency relations
     * 
     * @param buildSetTask The build set task which will contain the build tasks.  This must already have
     * initialized the BuildConfigSet, BuildConfigSetRecord, Milestone, etc.
     */
    private void initializeBuildTasksInSet(BuildSetTask buildSetTask, boolean rebuildAll) {
        String topContentId = ContentIdentityManager.getProductContentId(buildSetTask.getBuildConfigurationSet().getProductVersion());
        String buildSetContentId = ContentIdentityManager.getBuildSetContentId(buildSetTask.getBuildConfigurationSet().getName());

        // Loop to create the build tasks
        for(BuildConfiguration buildConfig : buildSetTask.getBuildConfigurationSet().getBuildConfigurations()) {
            BuildConfigurationAudited buildConfigAudited = datastoreAdapter.getLatestBuildConfigurationAudited(buildConfig.getId());

            BuildTask buildTask = BuildTask.build(
                    buildConfig,
                    buildConfigAudited,
                    buildSetTask.getBuildConfigSetRecord().getUser(),
                    getBuildStatusChangedEventNotifier(),
                    (bt) -> processBuildTask(bt),
                    datastoreAdapter.getNextBuildRecordId(), //TODO in bpm case we are not storing this task ?
                    buildSetTask,
                    buildSetTask.getSubmitTime(), rebuildAll);

            buildSetTask.addBuildTask(buildTask);
        }
        // Loop again to set dependencies
        for (BuildTask buildTask : buildSetTask.getBuildTasks()) {
            for (BuildConfiguration dep : buildTask.getBuildConfigurationDependencies()) {
                if (buildSetTask.getBuildConfigurationSet().getBuildConfigurations().contains(dep)) {
                    BuildTask depTask = buildSetTask.getBuildTask(dep);
                    if (depTask != null) {
                        buildTask.addDependency(depTask);
                    }
                }
            }
        }
    }

    /**
     * Searches the active build tasks to see if there is already one running the give audited
     * build config.  If yes, returns the associated build task.  If none are found, returns null.
     * 
     * @param buildConfigAudited The build config to look for in the active build tasks
     * @return An Optional containing the matching build task if there is one.
     */
    private Optional<BuildTask> getActiveBuildTask(BuildConfigurationAudited buildConfigAudited) {
        return activeBuildTasks.stream().filter(bt -> bt.getBuildConfigurationAudited().equals(buildConfigAudited)).findFirst();
    }

    /**
     * Get the product milestone (if any) associated with this build config set.
     * @param buildConfigSet
     * @return The product milestone, or null if there is none
     */
    private ProductMilestone getProductMilestone(BuildConfigurationSet buildConfigSet) {
        if(buildConfigSet.getProductVersion() == null || buildConfigSet.getProductVersion().getCurrentProductMilestone() == null) {
            return null;
        }
        return buildConfigSet.getProductVersion().getCurrentProductMilestone();
    }

    private void build(BuildSetTask buildSetTask) throws CoreException {


        Predicate<BuildTask> readyToBuild = (buildTask) -> {
            return buildTask.readyToBuild();
        };

        Predicate<BuildTask> rejectAlreadySubmitted = (buildTask) -> {
            if (isBuildAlreadySubmitted(buildTask)) {
                buildTask.setStatus(BuildStatus.REJECTED);
                buildTask.setStatusDescription("The configuration is already in the build queue.");
                return false;
            } else {
                return true;
            }
        };

        if (!BuildSetStatus.REJECTED.equals(buildSetTask.getStatus())) {
            buildSetTask.getBuildTasks().stream()
                    .filter(readyToBuild)
                    .filter(rejectAlreadySubmitted)
                    .forEach(v -> processBuildTask(v));
        }
    }

    void processBuildTask(BuildTask buildTask) {
        Consumer<BuildStatus> onComplete = (buildStatus) -> {
            activeBuildTasks.remove(buildTask);
            buildTask.setStatus(buildStatus);
        };
        try {
            buildScheduler.startBuilding(buildTask, onComplete);
            activeBuildTasks.add(buildTask);
        } catch (CoreException e) {
            buildTask.setStatus(BuildStatus.SYSTEM_ERROR);
            buildTask.setStatusDescription(e.getMessage());
        }
    }

    private boolean isConfigurationBuilt(BuildConfiguration buildConfiguration) {
        return datastoreAdapter.isBuildConfigurationBuilt();
    }


    /**
     * Save the build config set record using a single thread for all db operations.
     * This ensures that database operations are done in the correct sequence, for example
     * in the case of a build config set.
     *
     * @param buildConfigSetRecord
     * @return The build config set record which has been saved to the db
     * @throws DatastoreException if there is a db problem which prevents this record being stored
     */
    protected BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) throws DatastoreException {
        return datastoreAdapter.saveBuildConfigSetRecord(buildConfigSetRecord);
    }

    public List<BuildTask> getActiveBuildTasks() {
        return Collections.unmodifiableList(activeBuildTasks.stream().collect(Collectors.toList()));
    }

    private boolean isBuildAlreadySubmitted(BuildTask buildTask) {
        return activeBuildTasks.contains(buildTask);
    }

    Event<BuildStatusChangedEvent> getBuildStatusChangedEventNotifier() {
        return buildStatusChangedEventNotifier;
    }

    Event<BuildSetStatusChangedEvent> getBuildSetStatusChangedEventNotifier() {
        return buildSetStatusChangedEventNotifier;
    }
}
