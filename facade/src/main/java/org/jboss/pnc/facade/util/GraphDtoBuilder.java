package org.jboss.pnc.facade.util;

import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Edge;
import org.jboss.pnc.dto.response.Vertex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class GraphDtoBuilder<T> {
    private final Map<String, String> metadata;

    public GraphDtoBuilder() {
        metadata = Collections.EMPTY_MAP;
    }

    public GraphDtoBuilder(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Graph<T> from(org.jboss.util.graph.Graph<T> graph, Class<T> dataType) {
        Map<String, Vertex<T>> verticies = new LinkedHashMap<>();
        List<Edge<T>> edges = new ArrayList<>();

        for (org.jboss.util.graph.Vertex<T> vertex : graph.getVerticies()) {
            Vertex<T> vertexRest = new Vertex<>(
                    vertex.getName(),
                    dataType.getName(),
                    vertex.getData());
            verticies.put(vertexRest.getName(), vertexRest);
        }

        for (org.jboss.util.graph.Edge<T> edge : graph.getEdges()) {
            Edge<T> edgeDto = new Edge<>(edge.getFrom().getName(), edge.getTo().getName(), edge.getCost());
            edges.add(edgeDto);
        }

        Graph<T> graphRest = new Graph<>(verticies, edges, metadata);
        return graphRest;
    }
}
