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
import org.jboss.pnc.common.graph.GraphStructureException;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.util.Quicksort;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
public class BuildTasksInitializer { // TODO update docs

    private final Logger log = LoggerFactory.getLogger(BuildTasksInitializer.class);

    private final DatastoreAdapter datastoreAdapter;

    public BuildTasksInitializer(DatastoreAdapter datastoreAdapter) {
        this.datastoreAdapter = datastoreAdapter;
    }

    public Graph<RemoteBuildTask> createBuildGraph(
            BuildConfigurationAudited buildConfigurationAudited,
            User user,
            BuildOptions buildOptions,
            Collection<BuildTaskRef> submittedBuildTasks) throws GraphStructureException {

        Set<BuildConfigurationAudited> toBuild = new HashSet<>();
        collectBuildTasks(buildConfigurationAudited, buildOptions, toBuild);
        log.debug(
                "Collected build tasks for the BuildConfigurationAudited: {}. Collected: {}.",
                buildConfigurationAudited,
                toBuild.stream().map(BuildConfigurationAudited::toString).collect(Collectors.joining(", ")));

        return doCreateBuildGraph(
                user,
                buildOptions,
                submittedBuildTasks,
                toBuild,
                Collections.emptyMap(),
                buildConfigurationAudited.getBuildConfiguration().getCurrentProductMilestone());
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

        Set<Integer> processedDependenciesCache = new HashSet<>();
        if (buildOptions.isBuildDependencies()) {
            collectDependentConfigurations(
                    buildConfigurationAudited.getBuildConfiguration(),
                    buildConfigurationAudited,
                    toBuild,
                    visited,
                    buildOptions.isImplicitDependenciesCheck(),
                    buildOptions.isForceRebuild(),
                    buildOptions.isTemporaryBuild(),
                    buildOptions.getAlignmentPreference(),
                    processedDependenciesCache);
        } else {
            Optional<BuildRecord> noRebuildCause = datastoreAdapter.requiresRebuild(
                    buildConfigurationAudited,
                    buildOptions.isImplicitDependenciesCheck(),
                    buildOptions.isTemporaryBuild(),
                    buildOptions.getAlignmentPreference(),
                    processedDependenciesCache);
            if (buildOptions.isForceRebuild() || noRebuildCause.isEmpty()) {
                toBuild.add(buildConfigurationAudited);
            }
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

        Optional<BuildRecord> noRebuildCause = datastoreAdapter.requiresRebuild(
                buildConfigurationAudited,
                checkImplicitDependencies,
                temporaryBuild,
                alignmentPreference,
                processedDependenciesCache);
        boolean requiresRebuild = forceRebuild || noRebuildCause.isEmpty();
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
    public Graph<RemoteBuildTask> createBuildGraph(
            BuildConfigurationSet buildConfigurationSet,
            Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap,
            User user,
            BuildOptions buildOptions,
            Collection<BuildTaskRef> submittedBuildTasks) throws GraphStructureException {

        Map<IdRev, BuildRecord> noRebuildRequiredCauses = new HashMap<>();
        Set<Integer> processedDependenciesCache = new HashSet<>();
        Set<BuildConfigurationAudited> buildConfigurationAuditeds = new HashSet<>();
        Set<BuildConfiguration> buildConfigurations = datastoreAdapter.getBuildConfigurations(buildConfigurationSet);
        List<BuildConfiguration> dependenciesFirst = new ArrayList<>(buildConfigurations);
        Quicksort.quicksort(dependenciesFirst, this::dependenciesFirst);

        Set<BuildConfiguration> toBuild = new HashSet<>();

        for (BuildConfiguration buildConfiguration : dependenciesFirst) {
            BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedsMap.get(buildConfiguration.getId());
            if (buildConfigurationAudited == null) {
                buildConfigurationAudited = datastoreAdapter
                        .getLatestBuildConfigurationAuditedInitializeBCDependencies(buildConfiguration.getId());
            }
            buildConfigurationAuditeds.add(buildConfigurationAudited);

            boolean anyDependencyRequiresRebuild = CollectionUtils.containsAny(buildConfiguration.getDependencies(), toBuild);

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
                buildConfigurationSet.getCurrentProductMilestone());
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
            Collection<BuildTaskRef> submittedBuildTasks,
            Set<BuildConfigurationAudited> toBuild,
            Map<IdRev, BuildRecord> noRebuildRequiredCauses,
            ProductMilestone currentProductMilestone) throws GraphStructureException {

        Graph<RemoteBuildTask> graph = new Graph<>();

        for (BuildConfigurationAudited buildConfigAudited : toBuild) {
            Optional<BuildTaskRef> taskOptional = submittedBuildTasks.stream()
                    .filter(bt -> bt.getIdRev().equals(buildConfigAudited.getIdRev()))
                    .findAny();
            BuildRecord noRebuildRequired = noRebuildRequiredCauses.get(buildConfigAudited.getIdRev());
            RemoteBuildTask remoteBuildTask;
            if (taskOptional.isPresent()) {
                BuildTaskRef buildTaskRef = taskOptional.get();
                remoteBuildTask = new RemoteBuildTask(
                        buildTaskRef.getId(),
                        Instant.now(),
                        buildConfigAudited,
                        buildOptions,
                        user.getId().toString(),
                        true,
                        noRebuildRequired,
                        currentProductMilestone);
            } else {
                remoteBuildTask = new RemoteBuildTask(
                        Sequence.nextBase32Id(),
                        Instant.now(),
                        buildConfigAudited,
                        buildOptions,
                        user.getId().toString(),
                        false,
                        noRebuildRequired,
                        currentProductMilestone);
            }
            Vertex<RemoteBuildTask> remoteBuildTaskVertex = new Vertex<>(
                    remoteBuildTask.getId(),
                    remoteBuildTask);
            graph.addVertex(remoteBuildTaskVertex);
        }
        List<Vertex<RemoteBuildTask>> verticies = graph.getVerticies();

        for (Vertex<RemoteBuildTask> parentVertex : verticies) {
            for (Vertex<RemoteBuildTask> childVertex : verticies) {
                if (hasDirectConfigDependencyOn(
                        parentVertex.getData().getBuildConfigurationAudited(),
                        childVertex.getData().getBuildConfigurationAudited())) {
                    graph.addEdge(parentVertex, childVertex, 1);
                }
            }
        }
//        Optional<Vertex<RemoteBuildTask>> root = GraphUtils.findRoot(graph);
//        root.ifPresent(r -> graph.setRootVertex(r));

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

        return buildConfiguration.getDependencies()
                .contains(child.getBuildConfiguration());
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
            // NOTE: after removal NRR task can still be referenced as a dependency of other tasks
            buildGraph.removeVertex(task);
        });
        return GraphUtils.unwrap(notToBuild);
    }

    private static void markToBuild(Vertex<RemoteBuildTask> task, Set<Vertex<RemoteBuildTask>> toBuild, Set<Vertex<RemoteBuildTask>> notToBuild) {
        toBuild.add(task);
        notToBuild.remove(task);
        markDependantsToBuild(task, toBuild, notToBuild);
    }

    private static void markDependantsToBuild(Vertex<RemoteBuildTask> task, Set<Vertex<RemoteBuildTask>> toBuild, Set<Vertex<RemoteBuildTask>> notToBuild) {
        List<Vertex<RemoteBuildTask>> dependants = GraphUtils.getFromVerticies(task.getIncomingEdges());
        for (Vertex<RemoteBuildTask> dependant : dependants) {
            if (!toBuild.contains(dependant)) {
                markToBuild(dependant, toBuild, notToBuild);
            }
        }
    }

}
