/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
            Vertex<T> vertexRest = new Vertex<>(vertex.getName(), dataType.getName(), vertex.getData());
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
