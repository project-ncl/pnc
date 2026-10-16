/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class GraphUtils {

    private static Logger logger = LoggerFactory.getLogger(GraphUtils.class);

    /**
     * Adds all elements from toMerge to target.
     */
    public static <T> void merge(Graph<T> target, Graph<T> toMerge) {
        for (Vertex<T> vertex : toMerge.getVerticies()) {
            target.addVertex(vertex);
        }

        // merge edges
        List<Edge<T>> edges = target.getEdges();
        for (Edge newEdge : toMerge.getEdges()) {
            Optional<Edge<T>> any = edges.stream()
                    .filter(
                            existing -> existing.getFrom().getName().equals(newEdge.getFrom().getName())
                                    && existing.getTo().getName().equals(newEdge.getTo().getName()))
                    .findAny();
            if (!any.isPresent()) {
                edges.add(newEdge);
            }
        }
    }

    public static <T> Graph<T> clone(Graph<T> graph) {
        Graph<T> clone = new Graph<>();
        for (Vertex<T> vertex : graph.getVerticies()) {
            clone.addVertex(new Vertex<>(vertex.getName(), vertex.getData()));
        }

        for (Edge<T> edge : graph.getEdges()) {
            clone.addEdge(
                    clone.findVertexByName(edge.getFrom().getName()),
                    clone.findVertexByName(edge.getTo().getName()),
                    edge.getCost());
        }

        if (graph.getRootVertex() != null) {
            clone.setRootVertex(new Vertex<>(graph.getRootVertex().getName(), graph.getRootVertex().getData()));
        }
        return clone;
    }

    public static <T> Collection<T> unwrap(Collection<Vertex<T>> verticies) {
        return verticies.stream().map(Vertex::getData).collect(Collectors.toSet());
    }

    public static <T> Optional<Vertex<T>> findRoot(Graph<T> graph) throws GraphStructureException {
        List<Vertex<T>> verticies = graph.getVerticies();
        List<Vertex<T>> possibleRoots = verticies.stream()
                .filter(v -> v.getIncomingEdgeCount() == 0)
                .collect(Collectors.toList());
        if (possibleRoots.size() == 1) {
            return Optional.of(possibleRoots.get(0));
        } else if (possibleRoots.size() == 0) {
            return Optional.empty();
        } else {
            throw new GraphStructureException("Can't find root, there are more possible candidates.");
        }
    }

    public static <T> boolean hasCycle(Graph<T> graph) {
        List<Vertex<T>> vertices = graph.getVerticies();
        Set<String> notVisited = vertices.stream().map(Vertex::getName).collect(Collectors.toSet());
        List<String> visiting = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        while (notVisited.size() > 0) {
            String current = notVisited.iterator().next();
            if (dfs(graph, current, notVisited, visiting, visited)) {
                return true;
            }
        }
        return false;
    }

    private static <T> boolean dfs(
            Graph<T> graph,
            String current,
            Set<String> notVisited,
            List<String> visiting,
            Set<String> visited) {
        move(current, notVisited, visiting);
        Vertex<T> currentTask = graph.findVertexByName(current);
        for (Object edge : currentTask.getOutgoingEdges()) {
            String dependency = ((Edge<T>) edge).getTo().getName();
            // attached dependencies are not in the builder declaration, therefore if discovered, they have to be add as
            // notVisited
            if (!notVisited.contains(dependency) && !visiting.contains(dependency) && !visited.contains(dependency)) {
                notVisited.add(dependency);
            }
            // already explored, continue
            if (visited.contains(dependency)) {
                continue;
            }
            // visiting again, cycle found
            if (visiting.contains(dependency)) {
                return true;
            }
            // recursive call
            if (dfs(graph, dependency, notVisited, visiting, visited)) {
                return true;
            }
        }
        move(current, visiting, visited);
        return false;
    }

    private static void move(String name, Collection<String> sourceSet, Collection<String> destinationSet) {
        sourceSet.remove(name);
        destinationSet.add(name);
    }

    public static <T> List<Vertex<T>> getFromVerticies(List<Edge<T>> edges) {
        return edges.stream().map(Edge::getFrom).collect(Collectors.toList());
    }

    public static <T> List<Vertex<T>> getToVerticies(List<Edge<T>> edges) {
        return edges.stream().map(Edge::getTo).collect(Collectors.toList());
    }

    public static <T> Optional<Vertex<T>> getVertex(Graph<T> buildGraph, String name) {
        return Optional.ofNullable(buildGraph.findVertexByName(name));
    }
}
