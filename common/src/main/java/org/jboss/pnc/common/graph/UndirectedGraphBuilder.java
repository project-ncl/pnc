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
package org.jboss.pnc.common.graph;

import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author Patrik Korytár &lt;pkorytar@redhat.com&gt;
 */
public class UndirectedGraphBuilder<T, S> {

    private final Logger logger = LoggerFactory.getLogger(UndirectedGraphBuilder.class);

    private Function<S, T> nodeSupplier;

    private Function<T, Collection<VertexNeighbor<S>>> neighborSupplier;

    public UndirectedGraphBuilder(
            Function<S, T> nodeSupplier,
            Function<T, Collection<VertexNeighbor<S>>> neighborSupplier) {
        this.nodeSupplier = nodeSupplier;
        this.neighborSupplier = neighborSupplier;
    }

    private T getNode(S id) {
        return nodeSupplier.apply(id);
    }

    private Collection<VertexNeighbor<S>> getNeighbors(T node) {
        return neighborSupplier.apply(node);
    }

    public Vertex<T> buildGraph(Graph<T> graph, S nodeId, int depthLimit) {
        T node = getNode(nodeId);
        Vertex<T> newVertex = new NameUniqueVertex<>(nodeId.toString(), node);
        graph.addVertex(newVertex);

        if (depthLimit <= 0) {
            return newVertex;
        }

        for (VertexNeighbor<S> vertexNeighbor : getNeighbors(node)) {
            S neighborId = vertexNeighbor.getNeighborId();
            boolean doesNeighborExist = getGraphVertex(graph, neighborId) != null;
            if (doesNeighborExist) {
                continue;
            }

            Vertex<T> neighborVertex = buildGraph(graph, neighborId, depthLimit - 1);

            logger.trace("Creating new neighbor edge from {} to {}.", newVertex, neighborVertex);
            graph.addEdge(newVertex, neighborVertex, vertexNeighbor.getCost());
        }

        return newVertex;
    }

    private Vertex<T> getGraphVertex(Graph<T> graph, S nodeId) {
        return graph.findVertexByName(nodeId.toString());
    }
}
