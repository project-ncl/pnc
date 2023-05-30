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

import org.apache.commons.collections.CollectionUtils;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.util.Quicksort;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class BuildTasksInitializer {

    private final Logger log = LoggerFactory.getLogger(BuildTasksInitializer.class);

    private final DatastoreAdapter datastoreAdapter;

    @Inject
    public BuildTasksInitializer(DatastoreAdapter datastoreAdapter) {
        this.datastoreAdapter = datastoreAdapter;
    }

    public Graph<RemoteBuildTask> createBuildGraph(
            BuildConfigurationAudited buildConfigurationAudited,
            User user,
            BuildOptions buildOptions,
            Collection<BuildTaskRef> unfinishedTasks) {

        log.debug(
                "will create build tasks for scope: {} and configuration: {}",
                buildOptions,
                buildConfigurationAudited);

        Set<BuildConfigurationAudited> collectedConfigurations = new HashSet<>();
        Map<IdRev, BuildRecord> noRebuildRequiredCauses = new HashMap<>();
        Set<BuildConfiguration> visited = new HashSet<>();

        Set<Integer> processedDependenciesCache = new HashSet<>();
        collectConfigurations(
                buildConfigurationAudited.getBuildConfiguration(),
                buildConfigurationAudited,
                collectedConfigurations,
                noRebuildRequiredCauses,
                visited,
                buildOptions.isBuildDependencies(),
                buildOptions.isImplicitDependenciesCheck(),
                buildOptions.isForceRebuild(),
                buildOptions.isTemporaryBuild(),
                buildOptions.getAlignmentPreference(),
                processedDependenciesCache);

        log.debug(
                "Collected build tasks for the BuildConfigurationAudited: {}. Collected: {}.",
                buildConfigurationAudited,
                collectedConfigurations.stream()
                        .map(BuildConfigurationAudited::toString)
                        .collect(Collectors.joining(", ")));

        return doCreateBuildGraph(
                user,
                buildOptions,
                unfinishedTasks,
                collectedConfigurations,
                noRebuildRequiredCauses,
                buildConfigurationAudited.getBuildConfiguration().getCurrentProductMilestone());
    }

    /**
     * Collects all BuildConfigurationAudited entities. If no-rebuild is required for a
     * {@link BuildConfigurationAudited}, its IdRev is added to the map, where key is IdRev of BCA not requiring a
     * rebuild and a value is a {@link BuildRecord} that satisfies no-rebuild required condition.
     *
     * @param buildConfiguration Current BuildConfiguration used to resolve dependencies.
     * @param buildConfigurationAudited Specific revision of a BuildConfiguration (passed as first parameter) to be
     *        potentially built
     * @param collectedConfigurations Set of BuildConfigurationAudited entities planned to be built
     * @param noRebuildRequiredCauses
     * @param visited Set of BuildConfigurations, which were already evaluated, if should be built
     * @param buildDependencies
     * @param checkImplicitDependencies if implicit check of dependencies needs to be done
     * @param forceRebuild if force build is required
     * @param temporaryBuild if build is temporary
     * @param processedDependenciesCache list containing any dependency which was already processed in previous
     *        iterations
     * @return Returns true, if the buildConfiguration should be rebuilt, otherwise returns false.
     */
    private boolean collectConfigurations(
            BuildConfiguration buildConfiguration,
            BuildConfigurationAudited buildConfigurationAudited,
            Set<BuildConfigurationAudited> collectedConfigurations,
            Map<IdRev, BuildRecord> noRebuildRequiredCauses,
            Set<BuildConfiguration> visited,
            boolean buildDependencies,
            boolean checkImplicitDependencies,
            boolean forceRebuild,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference,
            Set<Integer> processedDependenciesCache) {
        if (visited.contains(buildConfiguration)) {
            return !noRebuildRequiredCauses.containsKey(buildConfigurationAudited.getIdRev());
        }
        visited.add(buildConfiguration);

        Optional<BuildRecord> noRebuildCause;
        if (!forceRebuild) {
            noRebuildCause = datastoreAdapter.requiresRebuild(
                    buildConfigurationAudited,
                    checkImplicitDependencies,
                    temporaryBuild,
                    alignmentPreference,
                    processedDependenciesCache);
            noRebuildCause.ifPresent(br -> noRebuildRequiredCauses.put(buildConfigurationAudited.getIdRev(), br));
        } else {
            noRebuildCause = Optional.empty();
        }

        boolean requiresRebuild = noRebuildCause.isEmpty();

        if (buildDependencies) {
            for (BuildConfiguration dependency : buildConfiguration.getDependencies()) {
                boolean dependencyRequiresRebuild = collectConfigurations(
                        dependency,
                        datastoreAdapter.getLatestBuildConfigurationAuditedInitializeBCDependencies(dependency.getId()),
                        collectedConfigurations,
                        noRebuildRequiredCauses,
                        visited,
                        buildDependencies,
                        checkImplicitDependencies,
                        forceRebuild,
                        temporaryBuild,
                        alignmentPreference,
                        processedDependenciesCache);
                requiresRebuild = requiresRebuild || dependencyRequiresRebuild;
            }
        }
        log.debug("Configuration {} requires rebuild: {}", buildConfiguration.getId(), requiresRebuild);
        collectedConfigurations.add(buildConfigurationAudited);

        return requiresRebuild;
    }

    /**
     * Create a Graph of all (including already running, no-rebuild required) associated dependencies.
     *
     * If idRev is present in the submittedBuildTasks a new {@link RemoteBuildTask} is marker as already running.
     *
     * @param buildConfigurationAuditedsMap A map BuildConfiguration::id:BuildConfigurationAudited of specific revisions
     *        of BuildConfigurations contained in the buildConfigurationSet
     * @param user A user, who triggered the build
     * @param buildOptions Build options
     * @param submittedBuildTasks Already submitted build tasks
     * @return a graph of all associated dependencies
     */
    public Graph<RemoteBuildTask> createBuildGraph(
            Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap,
            User user,
            BuildOptions buildOptions,
            Collection<BuildTaskRef> submittedBuildTasks,
            ProductMilestone currentProductMilestone) {

        Map<IdRev, BuildRecord> noRebuildRequiredCauses = new HashMap<>();
        Set<Integer> processedDependenciesCache = new HashSet<>();
        Collection<BuildConfigurationAudited> buildConfigurationAuditeds = buildConfigurationAuditedsMap.values();

        Set<BuildConfiguration> buildConfigurations = buildConfigurationAuditedsMap.values()
                .stream()
                .map(BuildConfigurationAudited::getBuildConfiguration)
                .collect(Collectors.toSet());

        List<BuildConfiguration> dependenciesFirst = new ArrayList<>(buildConfigurations);
        Quicksort.quicksort(dependenciesFirst, this::dependenciesFirst);

        Set<BuildConfiguration> toBuild = new HashSet<>();

        for (BuildConfiguration buildConfiguration : dependenciesFirst) {
            BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedsMap
                    .get(buildConfiguration.getId());

            boolean anyDependencyRequiresRebuild = CollectionUtils
                    .containsAny(buildConfiguration.getDependencies(), toBuild);

            if (!buildOptions.isForceRebuild() && !anyDependencyRequiresRebuild) {
                Optional<BuildRecord> noRebuildCause = datastoreAdapter.requiresRebuild(
                        buildConfigurationAudited,
                        buildOptions.isImplicitDependenciesCheck(),
                        buildOptions.isTemporaryBuild(),
                        buildOptions.getAlignmentPreference(),
                        processedDependenciesCache);
                if (noRebuildCause.isPresent()) {
                    noRebuildRequiredCauses.put(buildConfigurationAudited.getIdRev(), noRebuildCause.get());
                } else {
                    toBuild.add(buildConfiguration);
                }
            } else {
                toBuild.add(buildConfiguration);
            }
        }

        log.debug(
                "Initializing BuildTasks In Set for BuildConfigurationAuditeds: {}.",
                buildConfigurationAuditeds.stream()
                        .map(BuildConfigurationAudited::toString)
                        .collect(Collectors.joining("; ")));

        return doCreateBuildGraph(
                user,
                buildOptions,
                submittedBuildTasks,
                buildConfigurationAuditeds,
                noRebuildRequiredCauses,
                currentProductMilestone);
    }

    private int dependenciesFirst(BuildConfiguration configuration1, BuildConfiguration configuration2) {
        if (configuration1.getDependencies().contains(configuration2)) {
            return 1;
        }
        if (configuration2.getDependencies().contains(configuration1)) {
            return -1;
        }
        return 0;
    }

    private Graph<RemoteBuildTask> doCreateBuildGraph(
            User user,
            BuildOptions buildOptions,
            Collection<BuildTaskRef> unfinishedTasks,
            Collection<BuildConfigurationAudited> collectedConfigurations,
            Map<IdRev, BuildRecord> noRebuildRequiredCauses,
            ProductMilestone currentProductMilestone) {

        Graph<RemoteBuildTask> graph = new Graph<>();

        for (BuildConfigurationAudited buildConfigAudited : collectedConfigurations) {
            Optional<BuildTaskRef> unfinishedTask = unfinishedTasks.stream()
                    .filter(bt -> bt.getIdRev().equals(buildConfigAudited.getIdRev()))
                    .findAny();
            BuildRecord noRebuildRequired = noRebuildRequiredCauses.get(buildConfigAudited.getIdRev());
            RemoteBuildTask remoteBuildTask;
            if (unfinishedTask.isPresent()) {
                BuildTaskRef buildTaskRef = unfinishedTask.get();
                remoteBuildTask = new RemoteBuildTask(
                        buildTaskRef.getId(),
                        Instant.now(),
                        buildConfigAudited,
                        buildOptions,
                        user.getUsername(),
                        true,
                        Optional.ofNullable(noRebuildRequired),
                        currentProductMilestone,
                        new ArrayList<>(),
                        new ArrayList<>());
            } else {
                remoteBuildTask = new RemoteBuildTask(
                        Sequence.nextBase32Id(),
                        Instant.now(),
                        buildConfigAudited,
                        buildOptions,
                        user.getUsername(),
                        false,
                        Optional.ofNullable(noRebuildRequired),
                        currentProductMilestone,
                        new ArrayList<>(),
                        new ArrayList<>());
            }
            Vertex<RemoteBuildTask> remoteBuildTaskVertex = new Vertex<>(remoteBuildTask.getId(), remoteBuildTask);
            graph.addVertex(remoteBuildTaskVertex);
        }
        List<Vertex<RemoteBuildTask>> verticies = graph.getVerticies();

        for (Vertex<RemoteBuildTask> parentVertex : verticies) {
            for (Vertex<RemoteBuildTask> childVertex : verticies) {
                if (hasDirectConfigDependencyOn(
                        parentVertex.getData().getBuildConfigurationAudited(),
                        childVertex.getData().getBuildConfigurationAudited())) {
                    var parent = parentVertex.getData();
                    var child = childVertex.getData();

                    parent.getDependencies().add(child.getId());
                    child.getDependants().add(parent.getId());

                    graph.addEdge(parentVertex, childVertex, 1);
                }
            }
        }
        return graph;
    }

    public boolean hasDirectConfigDependencyOn(BuildConfigurationAudited parent, BuildConfigurationAudited child) {
        if (child == null || child.equals(parent)) {
            return false;
        }

        BuildConfiguration buildConfiguration = parent.getBuildConfiguration();
        if (buildConfiguration == null || buildConfiguration.getDependencies() == null) {
            return false;
        }

        return buildConfiguration.getDependencies().contains(child.getBuildConfiguration());
    }

    /**
     * @return NO_REBUILD_REQUIRED tasks
     */
    public static Collection<RemoteBuildTask> removeNRRTasks(Graph<RemoteBuildTask> buildGraph) {
        Set<Vertex<RemoteBuildTask>> toBuild = new HashSet<>();
        Set<Vertex<RemoteBuildTask>> notToBuild = new HashSet<>();

        for (Vertex<RemoteBuildTask> task : buildGraph.getVerticies()) {
            if (!toBuild.contains(task) && task.getData().getNoRebuildCause().isPresent()) {
                notToBuild.add(task);
            } else {
                markToBuild(task, toBuild, notToBuild);
            }
        }

        notToBuild.forEach(task -> {
            // Remove the reference if the task is not running (not present in Rex).
            // If it is running, it will be linked by the Rex to a running dependency.
            // Tasks that are not present in the Rex cannot be submitted to Rex,
            // because Rex cannot reference them.
            List<Edge<RemoteBuildTask>> dependantsEdges = task.getIncomingEdges();
            dependantsEdges.stream()
                    .filter(de -> !de.getTo().getData().isAlreadyRunning())
                    .collect(Collectors.toSet())
                    .stream()
                    .forEach(de -> buildGraph.removeEdge(de.getFrom(), de.getTo()));

            buildGraph.removeVertex(task);
        });
        return GraphUtils.unwrap(notToBuild);
    }

    private static void markToBuild(
            Vertex<RemoteBuildTask> task,
            Set<Vertex<RemoteBuildTask>> toBuild,
            Set<Vertex<RemoteBuildTask>> notToBuild) {
        toBuild.add(task);
        notToBuild.remove(task);
        markDependantsToBuild(task, toBuild, notToBuild);
    }

    private static void markDependantsToBuild(
            Vertex<RemoteBuildTask> task,
            Set<Vertex<RemoteBuildTask>> toBuild,
            Set<Vertex<RemoteBuildTask>> notToBuild) {
        List<Vertex<RemoteBuildTask>> dependants = GraphUtils.getFromVerticies(task.getIncomingEdges());
        for (Vertex<RemoteBuildTask> dependant : dependants) {
            if (!toBuild.contains(dependant)) {
                markToBuild(dependant, toBuild, notToBuild);
            }
        }
    }
}
