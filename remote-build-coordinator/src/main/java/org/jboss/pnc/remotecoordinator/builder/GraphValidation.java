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

import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Optional;

public class GraphValidation {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * @throws BuildConflictException
     */
    public static void checkIfAnyDependencyOfAlreadyRunningIsSubmitted(Graph<RemoteBuildTask> buildGraph)
            throws BuildConflictException {
        for (Vertex<RemoteBuildTask> parent : buildGraph.getVerticies()) {
            if (parent.getData().isAlreadyRunning()) {
                Collection<Edge<RemoteBuildTask>> edges = parent.getOutgoingEdges();
                for (Edge<RemoteBuildTask> edge : edges) {
                    RemoteBuildTask child = edge.getTo().getData();
                    boolean hasNoRebuildCause = child.getNoRebuildCause() != null
                            && child.getNoRebuildCause().isPresent();
                    if (!hasNoRebuildCause && !child.isAlreadyRunning()) {
                        throw new BuildConflictException(
                                "Submitted build " + child.getBuildConfigurationAudited().getName()
                                        + " is a dependency of already running build: "
                                        + parent.getData().getBuildConfigurationAudited().getName());
                    }
                }
            }
        }
    }

    /**
     * Returns a {@link BuildStatusWithDescription} if there are validation errors (no build is required or not
     * possible), otherwise Optional.empty() is returned.
     */
    public static Optional<BuildStatusWithDescription> validateBuildConfigurationSetTask(
            Graph buildGraph,
            BuildOptions buildOptions) {

        // Check if the given build set task is empty and update the status message appropriately
        if (buildGraph.isEmpty()) {
            return Optional.of(new BuildStatusWithDescription(BuildStatus.REJECTED, "Build config set is empty"));
        }

        // check if no rebuild is required
        if (!buildOptions.isForceRebuild()) {
            boolean noRebuildsRequired = checkIfNoRebuildIsRequired(buildGraph);
            if (noRebuildsRequired) {
                return Optional.of(
                        new BuildStatusWithDescription(
                                BuildStatus.NO_REBUILD_REQUIRED,
                                "All build configs were previously built"));
            }
        }

        // check if there are cycles
        if (GraphUtils.hasCycle(buildGraph)) {
            return Optional.of(new BuildStatusWithDescription(BuildStatus.REJECTED, "Build config set has a cycle"));
        }
        return Optional.empty();
    }

    /**
     * Returns true if no build configurations needs a rebuild
     */
    private static boolean checkIfNoRebuildIsRequired(Graph<RemoteBuildTask> buildGraph) {
        Collection<RemoteBuildTask> buildTasks = GraphUtils.unwrap(buildGraph.getVerticies());
        long requiresRebuild = buildTasks.stream()
                .filter(bt -> !bt.isAlreadyRunning())
                .filter(bt -> bt.getNoRebuildCause().isEmpty())
                .count();
        logger.debug("{} configurations require a rebuild.", requiresRebuild);
        return requiresRebuild == 0;
    }
}
