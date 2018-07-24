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
package org.jboss.pnc.rest.utils;

import org.jboss.pnc.rest.restmodel.graph.EdgeRest;
import org.jboss.pnc.rest.restmodel.graph.GraphRest;
import org.jboss.pnc.rest.restmodel.graph.VertexRest;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RestGraphBuilder {

    public static <T> GraphRest<T> from(Graph<T> graph, Class<T> dataType) {
        Map<String, VertexRest<T>> verticles = new LinkedHashMap<>();
        List<EdgeRest<T>> edges = new ArrayList<>();

        for (Vertex<T> vertex : graph.getVerticies()) {
            VertexRest<T> vertexRest = new VertexRest<>(
                    vertex.getName(),
                    dataType.getName(),
                    vertex.getData());
            verticles.put(vertexRest.getName(), vertexRest);

            for (Object o : vertex.getOutgoingEdges()) {
                Edge<BuildTask> edge = (Edge<BuildTask>) o;
                EdgeRest<T> edgeRest = new EdgeRest<T>(edge.getFrom().getName(), edge.getTo().getName(), edge.getCost());
                edges.add(edgeRest);
            }
        }
        GraphRest<T> graphRest = new GraphRest<>(verticles, edges);
        return graphRest;
    }
}
