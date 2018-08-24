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

import org.jboss.pnc.common.mdc.MDCUtils;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildTasksInitializer {

    private final Logger log = LoggerFactory.getLogger(BuildTasksInitializer.class);

    private DatastoreAdapter datastoreAdapter; //TODO remove datastore dependency

    private Date temporaryBuildExpireDate;

    public BuildTasksInitializer(DatastoreAdapter datastoreAdapter, Date temporaryBuildExpireDate) {
        this.datastoreAdapter = datastoreAdapter;
        this.temporaryBuildExpireDate = temporaryBuildExpireDate;
    }

    public BuildSetTask createBuildSetTask(BuildConfiguration configuration, User user, BuildOptions buildOptions,
            Supplier<Integer> buildTaskIdProvider, Set<BuildTask> submittedBuildTasks) throws CoreException {

        BuildSetTask buildSetTask =
                BuildSetTask.Builder.newBuilder()
                        .buildOptions(buildOptions)
                        .startTime(new Date()).build();

        Set<BuildConfigurationAudited> toBuild = new HashSet<>();
        collectBuildTasks(datastoreAdapter.getLatestBuildConfigurationAudited(configuration.getId()), buildOptions, toBuild);
        log.debug("Collected build tasks for configuration: {}. Collected: {}.", configuration, toBuild.stream().map(BuildConfigurationAudited::toString).collect(Collectors.joining(", ")));
        fillBuildTaskSet(
                buildSetTask,
                user,
                buildTaskIdProvider,
                configuration.getCurrentProductMilestone(),
                toBuild,
                submittedBuildTasks,
                buildOptions);
        return buildSetTask;
    }

    private void collectBuildTasks(BuildConfigurationAudited buildConfigurationAudited, BuildOptions buildOptions, Set<BuildConfigurationAudited> toBuild) {
        log.debug("will create build tasks for scope: {} and configuration: {}", buildOptions, buildConfigurationAudited);
        Set<BuildConfiguration> visited = new HashSet<>();
        if (toBuild.contains(buildConfigurationAudited)) {
            return;
        }

        toBuild.add(buildConfigurationAudited);
        if (buildOptions.isBuildDependencies()) {
            buildConfigurationAudited.getBuildConfiguration().getDependencies().forEach(c -> collectDependentConfigurations(c, toBuild, visited));
        }
    }

    private boolean collectDependentConfigurations(BuildConfiguration configuration, Set<BuildConfigurationAudited> toBuild, Set<BuildConfiguration> visited) {
        if (visited.contains(configuration)) {
            return toBuild.contains(configuration);
        }
        visited.add(configuration);

        boolean requiresRebuild = datastoreAdapter.requiresRebuild(configuration);
        for (BuildConfiguration dependency : configuration.getDependencies()) {
            requiresRebuild |= collectDependentConfigurations(dependency, toBuild, visited);
        }
        if (requiresRebuild) {
            toBuild.add(datastoreAdapter.getLatestBuildConfigurationAudited(configuration.getId()));
        }

        return requiresRebuild;
    }


    /**
     * Create a BuildSetTask of latest revisions of BuildConfigurations contained in the BuildConfigurationSet
     *
     * @param buildConfigurationSet BuildConfigurationSet to be built
     * @param user A user, who triggered the build
     * @param buildOptions Build options
     * @param buildTaskIdProvider Provider to get build task ID
     * @param submittedBuildTasks Already submitted build tasks
     * @return Prepared BuildSetTask
     * @throws CoreException Thrown if the BuildConfigSetRecord cannot be stored
     */
    public BuildSetTask createBuildSetTask(
            BuildConfigurationSet buildConfigurationSet,
            User user,
            BuildOptions buildOptions,
            Supplier<Integer> buildTaskIdProvider,
            Set<BuildTask> submittedBuildTasks) throws CoreException {
        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .startTime(new Date())
                .status(org.jboss.pnc.model.BuildStatus.BUILDING)
                .temporaryBuild(buildOptions.isTemporaryBuild())
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
                        .buildOptions(buildOptions).build();

        Set<BuildConfigurationAudited> buildConfigurationAuditeds = datastoreAdapter
                .getBuildConfigurations(buildConfigurationSet)
                .stream()
                .map(buildConfiguration -> datastoreAdapter.getLatestBuildConfigurationAudited(buildConfiguration.getId()))
                .collect(Collectors.toSet());

        // initializeBuildTasksInSet
        log.debug("Initializing BuildTasks In Set for BuildConfigurationAuditeds: {}.", buildConfigurationAuditeds.stream().map(bc ->bc.toString()).collect(Collectors.joining("; ")));
        fillBuildTaskSet(
                buildSetTask,
                user,
                buildTaskIdProvider,
                buildConfigurationSet.getCurrentProductMilestone(),
                buildConfigurationAuditeds,
                submittedBuildTasks,
                buildOptions);
        return buildSetTask;
    }

    /**
     * Creates build tasks and sets up the appropriate dependency relations
     *
     * @param buildSetTask The build set task which will contain the build tasks.  This must already have
     *                     initialized the BuildConfigSet, BuildConfigSetRecord, Milestone, etc.
     */
    private void fillBuildTaskSet(
            BuildSetTask buildSetTask,
            User user,
            Supplier<Integer> buildTaskIdProvider,
            ProductMilestone productMilestone,
            Set<BuildConfigurationAudited> toBuild,
            Set<BuildTask> alreadySubmittedBuildTasks,
            BuildOptions buildOptions) {
        for (BuildConfigurationAudited buildConfigAudited : toBuild) {
            String buildContentId = ContentIdentityManager.getBuildContentId(buildConfigAudited.getName());
            MDCUtils.setMDC(buildContentId, buildOptions.isTemporaryBuild(), temporaryBuildExpireDate);

            Optional<BuildTask> taskOptional = alreadySubmittedBuildTasks.stream()
                    .filter(bt -> bt.getBuildConfigurationAudited().equals(buildConfigAudited))
                    .findAny();

            BuildTask buildTask;
            if (taskOptional.isPresent()) {
                buildTask = taskOptional.get();
                log.debug("Linking BuildConfigurationAudited {} to existing task {}.", buildConfigAudited, buildTask);
            } else {
                buildTask = BuildTask.build(
                        buildConfigAudited,
                        buildSetTask.getBuildOptions(),
                        user,
                        buildTaskIdProvider.get(),
                        buildSetTask,
                        buildSetTask.getStartTime(),
                        productMilestone,
                        buildContentId);
                log.debug("Created new buildTask {} for BuildConfigurationAudited {}.", buildTask, buildConfigAudited);
            }

            buildSetTask.addBuildTask(buildTask);
        }

        // Loop again to set dependencies
        for (BuildTask buildTask : buildSetTask.getBuildTasks()) {
            for (BuildTask checkDepBuildTask : buildSetTask.getBuildTasks()) {
                if (buildTask.hasDirectConfigDependencyOn(checkDepBuildTask)) {
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
