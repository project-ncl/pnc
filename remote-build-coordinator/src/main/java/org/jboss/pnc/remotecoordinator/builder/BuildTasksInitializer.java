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

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildTasksInitializer {

    private final Logger log = LoggerFactory.getLogger(BuildTasksInitializer.class);

    private final DatastoreAdapter datastoreAdapter;

    private final long temporaryBuildLifespanDays;

    public BuildTasksInitializer(DatastoreAdapter datastoreAdapter, long temporaryBuildLifespanDays) {
        this.datastoreAdapter = datastoreAdapter;
        this.temporaryBuildLifespanDays = temporaryBuildLifespanDays;
    }

    public BuildSetTask createBuildSetTask(
            BuildConfigurationAudited buildConfigurationAudited,
            User user,
            BuildOptions buildOptions,
            Collection<BuildTaskRef> submittedBuildTasks) {

        BuildSetTask buildSetTask = BuildSetTask.Builder.newBuilder()
                .buildOptions(buildOptions)
                .startTime(new Date())
                .build();

        Set<BuildConfigurationAudited> toBuild = new HashSet<>();
        collectBuildTasks(buildConfigurationAudited, buildOptions, toBuild);
        log.debug(
                "Collected build tasks for the BuildConfigurationAudited: {}. Collected: {}.",
                buildConfigurationAudited,
                toBuild.stream().map(BuildConfigurationAudited::toString).collect(Collectors.joining(", ")));

        createNewBuildTasksFromBCAs(
                buildSetTask,
                user,
                buildConfigurationAudited.getBuildConfiguration().getCurrentProductMilestone(),
                toBuild,
                submittedBuildTasks);
        //TODO Loop again to set dependencies
        for (BuildTask buildTask : buildSetTask.getBuildTasks()) {
            buildSetTask.addBuildTask(buildTask);
            for (BuildTask checkDepBuildTask : buildSetTask.getBuildTasks()) {
                if (buildTask.hasDirectConfigDependencyOn(checkDepBuildTask)) {
                    buildTask.addDependency(checkDepBuildTask);
                }
            }
        }

        return buildSetTask;
    }

    private void collectBuildTasks(
            BuildConfigurationAudited buildConfigurationAudited,
            BuildOptions buildOptions,
            Set<BuildConfigurationAudited> toBuild) {
        log.debug(
                "will create build tasks for scope: {} and configuration: {}",
                buildOptions,
                buildConfigurationAudited);
        Set<BuildConfiguration> visited = new HashSet<>();
        if (toBuild.contains(buildConfigurationAudited)) {
            return;
        }

        toBuild.add(buildConfigurationAudited);
        if (buildOptions.isBuildDependencies()) {

            Set<Integer> processedDependenciesCache = new HashSet<>();
            buildConfigurationAudited.getBuildConfiguration()
                    .getDependencies()
                    .forEach(
                            dependencyConfiguration -> collectDependentConfigurations(
                                    dependencyConfiguration,
                                    datastoreAdapter.getLatestBuildConfigurationAuditedInitializeBCDependencies(
                                            dependencyConfiguration.getId()),
                                    toBuild,
                                    visited,
                                    buildOptions.isImplicitDependenciesCheck(),
                                    buildOptions.isForceRebuild(),
                                    buildOptions.isTemporaryBuild(),
                                    buildOptions.getAlignmentPreference(),
                                    processedDependenciesCache));
        }
    }

    /**
     * Collects all BuildConfigurationAudited entities, that needs to be built.
     *
     * @param buildConfiguration Current BuildConfiguration used to resolve dependencies.
     * @param buildConfigurationAudited Specific revision of a BuildConfiguration (passed as first parameter) to be
     *        potentially built
     * @param toBuild Set of BuildConfigurationAudited entities planned to be built
     * @param visited Set of BuildConfigurations, which were already evaluated, if should be built
     * @param checkImplicitDependencies if implicit check of dependencies needs to be done
     * @param forceRebuild if force build is required
     * @param temporaryBuild if build is temporary
     * @param processedDependenciesCache list containing any dependency which was already processed in previous
     *        iterations
     * @return Returns true, if the buildConfiguration should be rebuilt, otherwise returns false.
     */
    private boolean collectDependentConfigurations(
            BuildConfiguration buildConfiguration,
            BuildConfigurationAudited buildConfigurationAudited,
            Set<BuildConfigurationAudited> toBuild,
            Set<BuildConfiguration> visited,
            boolean checkImplicitDependencies,
            boolean forceRebuild,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference,
            Set<Integer> processedDependenciesCache) {
        if (visited.contains(buildConfiguration)) {
            return toBuild.contains(buildConfigurationAudited);
        }
        visited.add(buildConfiguration);

        boolean requiresRebuild = forceRebuild || datastoreAdapter.requiresRebuild(
                buildConfigurationAudited,
                checkImplicitDependencies,
                temporaryBuild,
                alignmentPreference,
                processedDependenciesCache);
        for (BuildConfiguration dependency : buildConfiguration.getDependencies()) {
            boolean dependencyRequiresRebuild = collectDependentConfigurations(
                    dependency,
                    datastoreAdapter.getLatestBuildConfigurationAuditedInitializeBCDependencies(dependency.getId()),
                    toBuild,
                    visited,
                    checkImplicitDependencies,
                    forceRebuild,
                    temporaryBuild,
                    alignmentPreference,
                    processedDependenciesCache);

            requiresRebuild = requiresRebuild || dependencyRequiresRebuild;

        }
        log.debug("Configuration {} requires rebuild: {}", buildConfiguration.getId(), requiresRebuild);
        if (requiresRebuild) {
            toBuild.add(buildConfigurationAudited);
        }

        return requiresRebuild;
    }

    /**
     * Create a BuildSetTask of BuildConfigurations contained in the BuildConfigurationSet.
     *
     * A specific revision of the BuildConfigurations contained in the set is used, if it's available in the
     * buildConfigurationAuditedsMap parameter. If it's not available, latest revision of the BuildConfiguration is
     * used.
     *
     * @param buildConfigurationSet BuildConfigurationSet to be built
     * @param buildConfigurationAuditedsMap A map BuildConfiguration::id:BuildConfigurationAudited of specific revisions
     *        of BuildConfigurations contained in the buildConfigurationSet
     * @param user A user, who triggered the build
     * @param buildOptions Build options
     * @param submittedBuildTasks Already submitted build tasks
     * @return Prepared BuildSetTask
     * @throws CoreException Thrown if the BuildConfigSetRecord cannot be stored
     */
    public BuildSetTask createBuildSetTask(
            BuildConfigurationSet buildConfigurationSet,
            Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap,
            User user,
            BuildOptions buildOptions,
            Collection<BuildTaskRef> submittedBuildTasks) {
        BuildSetTask buildSetTask = initBuildSetTask(buildConfigurationSet, user, buildOptions);

        Set<BuildConfigurationAudited> buildConfigurationAuditeds = new HashSet<>();
        for (BuildConfiguration buildConfiguration : datastoreAdapter.getBuildConfigurations(buildConfigurationSet)) {
            BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedsMap
                    .get(buildConfiguration.getId());
            if (buildConfigurationAudited == null) {
                buildConfigurationAudited = datastoreAdapter
                        .getLatestBuildConfigurationAuditedInitializeBCDependencies(buildConfiguration.getId());
            }
            buildConfigurationAuditeds.add(buildConfigurationAudited);
        }

        // initializeBuildTasksInSet
        log.debug(
                "Initializing BuildTasks In Set for BuildConfigurationAuditeds: {}.",
                buildConfigurationAuditeds.stream()
                        .map(BuildConfigurationAudited::toString)
                        .collect(Collectors.joining("; ")));
        Collection<BuildTask> newBuildTasksFromBCAs = createNewBuildTasksFromBCAs(
                buildSetTask,
                user,
                buildConfigurationSet.getCurrentProductMilestone(),
                buildConfigurationAuditeds,
                submittedBuildTasks);

        //TODO Loop again to set dependencies
        for (BuildTask buildTask : buildSetTask.getBuildTasks()) {
            buildSetTask.addBuildTask(buildTask);
            for (BuildTask checkDepBuildTask : buildSetTask.getBuildTasks()) {
                if (buildTask.hasDirectConfigDependencyOn(checkDepBuildTask)) {
                    buildTask.addDependency(checkDepBuildTask);
                }
            }
        }
        return buildSetTask;
    }

    private BuildSetTask initBuildSetTask(
            BuildConfigurationSet buildConfigurationSet,
            User user,
            BuildOptions buildOptions) {

        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .startTime(new Date())
                .status(org.jboss.pnc.enums.BuildStatus.BUILDING)
                .temporaryBuild(buildOptions.isTemporaryBuild())
                .alignmentPreference(buildOptions.getAlignmentPreference())
                .build();

        return BuildSetTask.Builder.newBuilder()
                .buildConfigSetRecord(buildConfigSetRecord)
                .buildOptions(buildOptions)
                .build();
    }

    /**
     * Creates build tasks from {@link BuildConfigurationAudited} collection.
     * Skip already submitted tasks.
     *
     * @param buildSetTask The build set task which will contain the build tasks. This must already have initialized the
     *        BuildConfigSet, BuildConfigSetRecord, Milestone, etc.
     */
    private Collection<BuildTask> createNewBuildTasksFromBCAs(
            BuildSetTask buildSetTask,
            User user,
            ProductMilestone productMilestone,
            Set<BuildConfigurationAudited> toBuild,
            Collection<BuildTaskRef> alreadySubmittedBuildTasks) {

        Set<BuildTask> tasks = new HashSet<>();

        for (BuildConfigurationAudited buildConfigAudited : toBuild) {
            Optional<BuildTaskRef> taskOptional = alreadySubmittedBuildTasks.stream()
                    .filter(bt -> bt.getIdRev().equals(buildConfigAudited.getIdRev()))
                    .findAny();

            if (!taskOptional.isPresent()) {
                String buildId = Sequence.nextBase32Id();
                String buildContentId = ContentIdentityManager.getBuildContentId(buildId);

                Optional<String> requestContext = MDCUtils.getRequestContext();
                BuildTask buildTask = BuildTask.build(
                        buildConfigAudited,
                        buildSetTask.getBuildOptions(),
                        user,
                        buildId,
                        buildSetTask,
                        buildSetTask.getStartTime(),
                        productMilestone,
                        buildContentId,
                        requestContext.orElse(null));
                log.debug(
                        "Created new buildTask {} for BuildConfigurationAudited {}.",
                        buildTask,
                        buildConfigAudited);
                tasks.add(buildTask);
            }
        }
        return tasks;
    }
}
