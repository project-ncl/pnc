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
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildTasksInitializer {

    private final Logger log = LoggerFactory.getLogger(BuildTasksInitializer.class);

    DatastoreAdapter datastoreAdapter; //TODO remove datastore dependency
    private final Optional<Event<BuildSetStatusChangedEvent>> buildSetStatusChangedEventNotifier;

    public BuildTasksInitializer(DatastoreAdapter datastoreAdapter, Optional<Event<BuildSetStatusChangedEvent>> buildSetStatusChangedEventNotifier) {
        this.datastoreAdapter = datastoreAdapter;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
    }

    public BuildSetTask createBuildSetTask(
            BuildConfigurationSet buildConfigurationSet,
            User user,
            boolean rebuildAll,
            Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier,
            Supplier<Integer> buildTaskIdProvider,
            Consumer<BuildTask> onAllTasksDependenciesCompleted,
            Consumer<BuildConfigSetRecord> onBuildSetTaskCompleted) throws CoreException {
        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .startTime(new Date())
                .status(org.jboss.pnc.model.BuildStatus.BUILDING)
                .build();

        final BuildConfigSetRecord persistedBuildConfigSetRecord;
        try {
            persistedBuildConfigSetRecord = saveBuildConfigSetRecord(buildConfigSetRecord);
        } catch (DatastoreException e) {
            log.error("Failed to store build config set record: " + e);
            throw new CoreException(e);
        }

        Consumer<BuildSetStatusChangedEvent> buildSetStatusChangedEventConsumer = (event) -> {
            if (event.getNewStatus().isCompleted()) {
                onBuildSetTaskCompleted.accept(persistedBuildConfigSetRecord);
            }
            buildSetStatusChangedEventNotifier.ifPresent(n -> n.fire(event));
        };

        Date buildSubmitTime = new Date();
        BuildSetTask buildSetTask = new BuildSetTask(
                persistedBuildConfigSetRecord,
                getProductMilestone(buildConfigurationSet),
                buildSubmitTime,
                rebuildAll,
                buildSetStatusChangedEventConsumer);

        initializeBuildTasksInSet(
                buildSetTask,
                user,
                rebuildAll,
                buildStatusChangedEventNotifier,
                buildTaskIdProvider,
                onAllTasksDependenciesCompleted);

        return buildSetTask;
    }

    /**
     * Creates build tasks and sets up the appropriate dependency relations
     *
     * @param buildSetTask The build set task which will contain the build tasks.  This must already have
     * initialized the BuildConfigSet, BuildConfigSetRecord, Milestone, etc.
     */
    public void initializeBuildTasksInSet(
            BuildSetTask buildSetTask,
            User user,
            boolean rebuildAll,
            Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier,
            Supplier<Integer> buildTaskIdProvider,
            Consumer<BuildTask> onAllDependenciesCompleted) {
        // Loop to create the build tasks
        for(BuildConfiguration buildConfig : buildSetTask.getBuildConfigurationSet().getBuildConfigurations()) {
            BuildConfigurationAudited buildConfigAudited = datastoreAdapter.getLatestBuildConfigurationAudited(buildConfig.getId());

            BuildTask buildTask = BuildTask.build(
                    buildConfig,
                    buildConfigAudited,
                    user,
                    buildStatusChangedEventNotifier,
                    (bt) -> onAllDependenciesCompleted.accept(bt),
                    buildTaskIdProvider.get(),
                    buildSetTask,
                    buildSetTask.getSubmitTime(),
                    rebuildAll);

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
     * Save the build config set record using a single thread for all db operations.
     * This ensures that database operations are done in the correct sequence, for example
     * in the case of a build config set.
     *
     * @param buildConfigSetRecord
     * @return The build config set record which has been saved to the db
     * @throws org.jboss.pnc.spi.datastore.DatastoreException if there is a db problem which prevents this record being stored
     */
    protected BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) throws DatastoreException {
        return datastoreAdapter.saveBuildConfigSetRecord(buildConfigSetRecord);
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
}
