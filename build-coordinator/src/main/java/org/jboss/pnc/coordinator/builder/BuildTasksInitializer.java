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

import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildScope;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildTasksInitializer {

    private final Logger log = LoggerFactory.getLogger(BuildTasksInitializer.class);

    private DatastoreAdapter datastoreAdapter; //TODO remove datastore dependency

    public BuildTasksInitializer(DatastoreAdapter datastoreAdapter) {
        this.datastoreAdapter = datastoreAdapter;
    }

    public BuildSetTask createBuildSetTask(
            BuildConfiguration configuration,
            User user,
            BuildScope scope,
            boolean keepAfterFailure,
            Supplier<Integer> buildTaskIdProvider) throws CoreException {
        BuildSetTask buildSetTask =
                BuildSetTask.Builder.newBuilder()
                        .forceRebuildAll(scope.isForceRebuild())
                        .startTime(new Date())
                        .keepAfterFailure(keepAfterFailure).build();


        Set<BuildConfiguration> buildTasks = new HashSet<>();
        createBuildTasks(configuration, scope, buildTasks);
        fillBuildTaskSet(buildSetTask, user, buildTaskIdProvider, configuration.getCurrentProductMilestone(), buildTasks);
        return buildSetTask;
    }

    private void createBuildTasks(BuildConfiguration configuration, BuildScope scope, Set<BuildConfiguration> configs) {
        if (configs.contains(configuration)) {
            return;
        }
        configs.add(configuration);
        if (scope.isRecursive()) {
            createDependencyBuildTasks(configuration, configs);
        }
    }

    private void createDependencyBuildTasks(BuildConfiguration configuration, Set<BuildConfiguration> configs) {
        if (!configs.contains(configuration) && datastoreAdapter.requiresRebuild(configuration)) {
            configs.add(configuration);
            configuration.getDependencies().stream()
                    .filter(c -> !configs.contains(c))
                    .forEach(c -> createDependencyBuildTasks(c, configs));
        }
    }


    public BuildSetTask createBuildSetTask(
            BuildConfigurationSet buildConfigurationSet,
            User user,
            boolean rebuildAll,
            boolean keepAfterFailure,
            Supplier<Integer> buildTaskIdProvider) throws CoreException {
        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .startTime(new Date())
                .status(org.jboss.pnc.model.BuildStatus.BUILDING)
                .build();

        final BuildConfigSetRecord configSetRecord;
        try {
            configSetRecord = saveBuildConfigSetRecord(buildConfigSetRecord);
        } catch (DatastoreException e) {
            log.error("Failed to store build config set record: " + e);
            throw new CoreException(e);
        }

        BuildSetTask buildSetTask =
                BuildSetTask.Builder.newBuilder()
                        .buildConfigSetRecord(configSetRecord)
                        .forceRebuildAll(rebuildAll)
                        .keepAfterFailure(keepAfterFailure).build();

        initializeBuildTasksInSet(
                buildSetTask,
                user,
                buildTaskIdProvider,
                buildConfigurationSet.getCurrentProductMilestone());

        return buildSetTask;
    }

    /**
     * Creates build tasks and sets up the appropriate dependency relations
     *
     * @param buildSetTask The build set task which will contain the build tasks.  This must already have
     *                     initialized the BuildConfigSet, BuildConfigSetRecord, Milestone, etc.
     */
    private void initializeBuildTasksInSet(
            BuildSetTask buildSetTask,
            User user,
            Supplier<Integer> buildTaskIdProvider,
            ProductMilestone productMilestone) {
        // Loop to create the build tasks
        Set<BuildConfiguration> toBuild =
                buildSetTask.getBuildConfigurationSet().getBuildConfigurations();

        fillBuildTaskSet(buildSetTask, user, buildTaskIdProvider, productMilestone, toBuild);
    }

    private void fillBuildTaskSet(BuildSetTask buildSetTask,
                                  User user,
                                  Supplier<Integer> buildTaskIdProvider,
                                  ProductMilestone productMilestone,
                                  Set<BuildConfiguration> toBuild) {
        for (BuildConfiguration buildConfig : toBuild) {
            BuildConfigurationAudited buildConfigAudited =
                    datastoreAdapter.getLatestBuildConfigurationAudited(buildConfig.getId());

            BuildTask buildTask = BuildTask.build(
                    buildConfig,
                    buildConfigAudited,
                    buildSetTask.isKeepAfterFailure(),
                    buildSetTask.getForceRebuildAll(),
                    user,
                    buildTaskIdProvider.get(),
                    buildSetTask,
                    buildSetTask.getStartTime(), productMilestone);

            buildSetTask.addBuildTask(buildTask);
        }

        // Loop again to set dependencies
        for (BuildTask buildTask : buildSetTask.getBuildTasks()) {
            for (BuildTask checkDepBuildTask : buildSetTask.getBuildTasks()) {
                if (buildTask.hasConfigDependencyOn(checkDepBuildTask)) {
                    buildTask.addDependency(checkDepBuildTask);
                }
            }
        }
    }

    /**
     * Save the build config set record using a single thread for all db operations.
     * This ensures that database operations are done in the correct sequence, for example
     * in the case of a build config set.
     *
     * @param buildConfigSetRecord The bcs record to save
     * @return The build config set record which has been saved to the db
     * @throws org.jboss.pnc.spi.datastore.DatastoreException if there is a db problem which prevents this record being stored
     */
    private BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) throws DatastoreException {
        return datastoreAdapter.saveBuildConfigSetRecord(buildConfigSetRecord);
    }

}
