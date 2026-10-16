/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.graph;

import java.util.Collection;
import java.util.function.Function;

import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Vertex<T> existingVertex = getGraphVertex(graph, nodeId);
        if (existingVertex != null) {
            return existingVertex;
        }

        T node = getNode(nodeId);
        Vertex<T> newVertex = new NameUniqueVertex<>(nodeId.toString(), node);
        graph.addVertex(newVertex);

        if (depthLimit <= 0) {
            return newVertex;
        }

        for (VertexNeighbor<S> vertexNeighbor : getNeighbors(node)) {
            S neighborId = vertexNeighbor.getNeighborId();
            Vertex<T> neighborVertex = buildGraph(graph, neighborId, depthLimit - 1);

            // add A->B edge only if B->A edge does not exist
            if (graph.getEdges()
                    .stream()
                    .noneMatch(edge -> edge.getFrom().equals(neighborVertex) && edge.getTo().equals(newVertex))) {
                logger.trace("Creating new neighbor edge from {} to {}.", newVertex, neighborVertex);
                graph.addEdge(newVertex, neighborVertex, vertexNeighbor.getCost());
            }
        }

        return newVertex;
    }

    private Vertex<T> getGraphVertex(Graph<T> graph, S nodeId) {
        return graph.findVertexByName(nodeId.toString());
    }
}
