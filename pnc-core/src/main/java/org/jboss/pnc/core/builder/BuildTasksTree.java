package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-05.
 */
public class BuildTasksTree {

    public static final Logger log = Logger.getLogger(BuildTasksTree.class);

    private Graph<BuildTask> tree = new Graph<>();

    BuildCoordinator buildCoordinator;

    public BuildTasksTree(BuildCoordinator buildCoordinator) {
        this.buildCoordinator = buildCoordinator;
    }

    public BuildTask getOrCreateSubmittedBuild(BuildConfiguration buildConfiguration) {
        return getOrCreateSubmittedBuild(buildConfiguration, Collections.emptySet(), Collections.emptySet());
    }

    BuildTask getOrCreateSubmittedBuild(BuildConfiguration buildConfiguration, Set<Consumer<BuildStatusChangedEvent>> statusUpdateListeners, Set<Consumer<String>> logConsumers) {
        Vertex<BuildTask> submittedBuildVertex = getVertexByBuildConfiguration(buildConfiguration);
        if (submittedBuildVertex != null) {
            return submittedBuildVertex.getData();
        } else {
            BuildTask buildTask = new BuildTask(buildCoordinator, buildConfiguration, statusUpdateListeners, logConsumers);
            addElementAndItsChildrenToTree(buildTask);
            return buildTask;
        }
    }

    private Vertex<BuildTask> getVertexByBuildConfiguration(BuildConfiguration buildConfiguration) {
        return tree.findVertexByName(buildConfiguration.getId().toString());
    }

    private Vertex<BuildTask> addElementAndItsChildrenToTree(BuildTask parentBuildTask) {
        Vertex<BuildTask> parentVertex = new Vertex(parentBuildTask.getId().toString(), parentBuildTask);
        tree.addVertex(parentVertex);
        BuildConfiguration parentBuildConfiguration = parentBuildTask.getBuildConfiguration();
        for (BuildConfiguration childBuildConfiguration : parentBuildConfiguration.getDependencies()) {
            if (parentBuildConfiguration.equals(childBuildConfiguration)) {
                log.debugf("Project build configuration %s depends on itself.", parentBuildTask.getId());
                parentBuildTask.setStatus(BuildStatus.REJECTED);
                parentBuildTask.setStatusDescription("Configuration depends on itself.");
                break;
            }
            Vertex<BuildTask> childVertex = getVertexByBuildConfiguration(childBuildConfiguration);
            if (childVertex == null) { //if it don't exists yet (it could exist in case of cycle)
                BuildTask childBuildTask = new BuildTask(buildCoordinator, childBuildConfiguration);
                childVertex = addElementAndItsChildrenToTree(childBuildTask);
            }
            tree.addEdge(parentVertex, childVertex, 1);
        }
        return parentVertex;
    }


    Edge<BuildTask>[] findCycles() {
        return tree.findCycles();
    }

    public List<Vertex<BuildTask>> getSubmittedBuilds() {
        return tree.getVerticies();
    }


}
