/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class GraphBuilder<T, S> {

    private final Logger logger = LoggerFactory.getLogger(GraphBuilder.class);

    private Function<S, T> nodeSupplier;

    private Function<T, Collection<S>> dependencySupplier;

    private Function<T, Collection<S>> dependantSupplier;

    public GraphBuilder(
            Function<S, T> nodeSupplier,
            Function<T, Collection<S>> dependencySupplier,
            Function<T, Collection<S>> dependantSupplier) {
        this.nodeSupplier = nodeSupplier;
        this.dependencySupplier = dependencySupplier;
        this.dependantSupplier = dependantSupplier;
    }

    private T getNode(S id) {
        return nodeSupplier.apply(id);
    }

    private Collection<S> getDependencyIds(T node) {
        return dependencySupplier.apply(node);
    }

    private Collection<S> getDependantIds(T node) {
        return dependantSupplier.apply(node);
    }

    public Vertex<T> buildDependencyGraph(Graph<T> graph, S nodeId) {
        T node = getNode(nodeId);
        Vertex<T> vertex = getVisited(nodeId, graph);
        if (vertex == null) {
            vertex = new NameUniqueVertex<>(nodeId.toString(), node);
            graph.addVertex(vertex);
        }
        for (S dependencyId : getDependencyIds(node)) {
            Vertex<T> dependency = getVisited(dependencyId, graph);
            if (dependency == null) {
                dependency = buildDependencyGraph(graph, dependencyId);
            }
            if (dependency != null) {
                logger.trace("Creating new dependency edge from {} to {}.", vertex, dependency);
                graph.addEdge(vertex, dependency, 1);
            }
        }
        return vertex;
    }

    public Vertex<T> buildDependentGraph(Graph<T> graph, S nodeId) {
        T node = getNode(nodeId);
        Vertex<T> vertex = getVisited(nodeId, graph);
        if (vertex == null) {
            vertex = new NameUniqueVertex<>(nodeId.toString(), node);
            graph.addVertex(vertex);
        }
        for (S dependantId : getDependantIds(node)) {
            Vertex<T> dependant = getVisited(dependantId, graph);
            if (dependant == null) {
                dependant = buildDependentGraph(graph, dependantId);
            }
            if (dependant != null) {
                logger.trace("Creating new dependant edge from {} to {}.", dependant, vertex);
                graph.addEdge(dependant, vertex, 1);
            }
        }
        return vertex;
    }

    private Vertex<T> getVisited(S nodeId, Graph<T> graph) {
        return graph.findVertexByName(nodeId.toString());
    }
}
