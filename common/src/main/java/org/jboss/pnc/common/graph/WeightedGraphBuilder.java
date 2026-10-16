/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.graph;

import java.util.Collection;
import java.util.function.Function;

import org.jboss.pnc.common.util.TriConsumer;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrik Korytár &lt;pkorytar@redhat.com&gt;
 */
@Slf4j
public class WeightedGraphBuilder<T, S> {

    private Function<S, T> nodeSupplier;

    private Function<T, Collection<VertexNeighbor<S>>> dependencySupplier;

    private Function<T, Collection<VertexNeighbor<S>>> dependantSupplier;

    public WeightedGraphBuilder(
            Function<S, T> nodeSupplier,
            Function<T, Collection<VertexNeighbor<S>>> dependencySupplier,
            Function<T, Collection<VertexNeighbor<S>>> dependantSupplier) {
        this.nodeSupplier = nodeSupplier;
        this.dependencySupplier = dependencySupplier;
        this.dependantSupplier = dependantSupplier;
    }

    private T getNode(S id) {
        return nodeSupplier.apply(id);
    }

    private Collection<VertexNeighbor<S>> getDependencies(T node) {
        return dependencySupplier.apply(node);
    }

    private Collection<VertexNeighbor<S>> getDependants(T node) {
        return dependantSupplier.apply(node);
    }

    public void buildGraph(Graph<T> graph, S nodeId, int depthLimit) {
        buildDependencyGraph(graph, nodeId, depthLimit);
        buildDependantGraph(graph, nodeId, depthLimit);
    }

    private void buildDependencyGraph(Graph<T> graph, S nodeId, int depthLimit) {
        buildGraphNode(graph, nodeId, depthLimit, this::getDependencies, (vertex, dependencyVertex, cost) -> {
            log.trace("Creating new dependency edge from {} to {}.", vertex, dependencyVertex);
            graph.addEdge(vertex, dependencyVertex, cost);
        });
    }

    private void buildDependantGraph(Graph<T> graph, S nodeId, int depthLimit) {
        buildGraphNode(graph, nodeId, depthLimit, this::getDependants, (vertex, dependantVertex, cost) -> {
            log.trace("Creating new dependant edge from {} to {}.", dependantVertex, vertex);
            graph.addEdge(dependantVertex, vertex, cost);
        });
    }

    private Vertex<T> buildGraphNode(
            Graph<T> graph,
            S nodeId,
            int depthLimit,
            Function<T, Collection<VertexNeighbor<S>>> neighborSupplier,
            TriConsumer<Vertex<T>, Vertex<T>, Integer> edgeAdder) {
        T node = getNode(nodeId);
        Vertex<T> vertex = getOrCreateGraphVertex(graph, nodeId, node);

        if (depthLimit <= 0) {
            return vertex;
        }

        for (VertexNeighbor<S> vertexNeighbor : neighborSupplier.apply(node)) {
            S neighborId = vertexNeighbor.getNeighborId();

            Vertex<T> neighborVertex = getGraphVertex(graph, neighborId);
            if (neighborVertex == null) {
                neighborVertex = buildGraphNode(graph, neighborId, depthLimit - 1, neighborSupplier, edgeAdder);
            }

            edgeAdder.accept(vertex, neighborVertex, vertexNeighbor.getCost());
        }

        return vertex;
    }

    private Vertex<T> getOrCreateGraphVertex(Graph<T> graph, S nodeId, T node) {
        Vertex<T> vertex = getGraphVertex(graph, nodeId);
        if (vertex == null) {
            vertex = new NameUniqueVertex<>(nodeId.toString(), node);
            graph.addVertex(vertex);
        }

        return vertex;
    }

    private Vertex<T> getGraphVertex(Graph<T> graph, S nodeId) {
        return graph.findVertexByName(nodeId.toString());
    }
}
