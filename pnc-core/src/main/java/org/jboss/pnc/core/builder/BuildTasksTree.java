package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.BuildExecutionType;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-05.
 */
public class BuildTasksTree {

    public static final Logger log = Logger.getLogger(BuildTasksTree.class);

    private Graph<BuildTask> tree = new Graph<>();

    /**
     * If there are cycles found in buildSetTask the status is set to BuildStatus.REJECTED;
     *
     * @param buildCoordinator
     * @param buildSetTask
     * @return
     */
    public static BuildTasksTree newInstance(BuildCoordinator buildCoordinator, BuildSetTask buildSetTask) {
        ContentIdentityManager contentIdentityManager = new ContentIdentityManager();

        BuildConfigurationSet buildConfigurationSet = buildSetTask.getBuildConfigurationSet();

        BuildTasksTree instance = new BuildTasksTree();

        String topContentId = contentIdentityManager.getProductContentId(buildConfigurationSet.getProductVersion());
        String buildSetContentId = contentIdentityManager.getBuildSetContentId(buildConfigurationSet);

        instance.buildTree(buildSetTask, buildCoordinator, topContentId, buildSetContentId);

        Edge<BuildTask>[] cycles = instance.tree.findCycles();
        if (cycles.length > 0) {
            buildSetTask.setStatus(BuildStatus.REJECTED);
            String configurationsInCycles = Arrays.asList(cycles).stream()
                    .map(e -> e.getFrom().getName() + "->" + e.getTo().getName())
                    .collect(Collectors.joining(", "));
            buildSetTask.setStatusDescription("Cycle dependencies found [" + configurationsInCycles + "]");
        }

        return instance;
    }

    private void buildTree(BuildSetTask buildSetTask,
            BuildCoordinator buildCoordinator,
            String topContentId,
            String buildSetContentId) {
        BuildConfigurationSet buildConfigurationSet = buildSetTask.getBuildConfigurationSet();

        Set<PotentialDependency> potentialDependencies = new HashSet<>();
        buildConfigurationSet.getBuildConfigurations().stream()
            .map(buildConfiguration -> addToTree(
                    buildConfiguration,
                    potentialDependencies,
                    buildCoordinator,
                    topContentId,
                    buildSetContentId,
                    buildSetTask.getBuildTaskType()))
            .forEach(buildTask -> buildSetTask.addBuildTask(buildTask));
    }

    private BuildTask addToTree(BuildConfiguration buildConfiguration,
            Set<PotentialDependency> potentialDependencies,
            BuildCoordinator buildCoordinator,
            String topContentId,
            String buildSetContentId,
            BuildExecutionType buildTaskType) {

        ContentIdentityManager contentIdentityManager = new ContentIdentityManager();
        Vertex<BuildTask> buildVertex = getVertexByBuildConfiguration(buildConfiguration);
        if (buildVertex == null) {
            String buildContentId = contentIdentityManager.getBuildContentId(buildConfiguration);
            BuildTask buildTask = new BuildTask(
                    buildCoordinator,
                    buildConfiguration,
                    topContentId,
                    buildSetContentId,
                    buildContentId,
                    buildTaskType);

            Vertex<BuildTask> vertex = new Vertex(buildTask.getId().toString(), buildTask);
            tree.addVertex(vertex);

            potentialDependencies.stream()
                    //keep only objects that depend on this
                    .filter(potentialDependency -> potentialDependency.getTo().equals(buildConfiguration))
                    .forEach(potentialDependency -> tree.addEdge(potentialDependency.getFrom(), vertex, 1));

            for (BuildConfiguration childBuildConfiguration : buildConfiguration.getDependencies()) {
                if (checkIfDependsOnItself(buildTask, childBuildConfiguration)) {
                    buildTask.setStatus(BuildStatus.REJECTED);
                    buildTask.setStatusDescription("Configuration depends on itself.");
                    break;
                }
                Vertex<BuildTask> childVertex = getVertexByBuildConfiguration(childBuildConfiguration);
                if (childVertex != null) {
                    //connect nodes if dependency exists in the tree
                    tree.addEdge(vertex, childVertex, 1);
                } else {
                    potentialDependencies.add(new PotentialDependency(vertex, childBuildConfiguration));
                }
            }
            return buildTask;
        } else {
            return buildVertex.getData();
        }
    }

    private Vertex<BuildTask> getVertexByBuildConfiguration(BuildConfiguration buildConfiguration) {
        return tree.findVertexByName(buildConfiguration.getId().toString());
    }

    public List<Vertex<BuildTask>> getBuildTasks() {
        return tree.getVerticies();
    }

    private boolean checkIfDependsOnItself(BuildTask buildTask, BuildConfiguration childBuildConfiguration) {
        BuildConfiguration buildConfiguration = buildTask.getBuildConfiguration();
        if (buildConfiguration.equals(childBuildConfiguration)) {
            log.debugf("Project build configuration %s depends on itself.", buildTask.getId());
            return true;
        }
        return false;
    }

    private class PotentialDependency {
        private final Vertex<BuildTask> from;
        private BuildConfiguration to;

        public PotentialDependency(Vertex<BuildTask> from, BuildConfiguration to) {
            this.from = from;
            this.to = to;
        }

        public Vertex<BuildTask> getFrom() {
            return from;
        }

        public BuildConfiguration getTo() {
            return to;
        }
    }
}
