package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;
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

    private final String topContentId;

    private final String buildSetContentId;

    private ContentIdentityManager contentIdentityManager;

    public BuildTasksTree(BuildCoordinator buildCoordinator, ContentIdentityManager contentIdentityManager) {
        this.buildCoordinator = buildCoordinator;
        this.contentIdentityManager = contentIdentityManager;
        this.topContentId = null;
        this.buildSetContentId = null;
    }

    public BuildTasksTree(BuildCoordinator buildCoordinator, ContentIdentityManager contentIdentityManager,
            ProductVersion productVersion) {
        this.buildCoordinator = buildCoordinator;
        this.contentIdentityManager = contentIdentityManager;
        this.topContentId = contentIdentityManager.getProductContentId(productVersion);
        this.buildSetContentId = null;
    }

    public BuildTasksTree(BuildCoordinator buildCoordinator, ContentIdentityManager contentIdentityManager,
            BuildConfigurationSet buildConfigurationSet) {
        this.buildCoordinator = buildCoordinator;
        this.contentIdentityManager = contentIdentityManager;
        this.topContentId = contentIdentityManager.getProductContentId(buildConfigurationSet.getProductVersion());
        this.buildSetContentId = contentIdentityManager.getBuildSetContentId(buildConfigurationSet);

        for (BuildConfiguration buildConfiguration : buildConfigurationSet.getBuildConfigurations()) {
            getOrCreateSubmittedBuild(buildConfiguration);
        }
    }

    public BuildTask getOrCreateSubmittedBuild(BuildConfiguration buildConfiguration) {
        return getOrCreateSubmittedBuild(buildConfiguration, Collections.emptySet(), Collections.emptySet());
    }

    BuildTask getOrCreateSubmittedBuild(BuildConfiguration buildConfiguration, Set<Consumer<BuildStatusChangedEvent>> statusUpdateListeners, Set<Consumer<String>> logConsumers) {
        Vertex<BuildTask> submittedBuildVertex = getVertexByBuildConfiguration(buildConfiguration);
        if (submittedBuildVertex != null) {
            return submittedBuildVertex.getData();
        } else {
            String buildContentId = contentIdentityManager.getBuildContentId(buildConfiguration);
            BuildTask buildTask = new BuildTask(buildCoordinator, buildConfiguration, topContentId, buildSetContentId,
                    buildContentId, statusUpdateListeners, logConsumers);

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
                String buildContentId = contentIdentityManager.getBuildContentId(childBuildConfiguration);
                BuildTask childBuildTask = new BuildTask(buildCoordinator, childBuildConfiguration, topContentId,
                        buildSetContentId, buildContentId);

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
