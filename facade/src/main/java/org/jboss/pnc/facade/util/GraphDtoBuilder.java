/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.jboss.pnc.dto.response.Edge;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Vertex;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class GraphDtoBuilder {

    /**
     * @param <S> Source graph data type
     * @param <T> Target graph data type
     */
    public static <S, T> Graph<T> from(
            org.jboss.util.graph.Graph<S> graph,
            Class<T> dataType,
            Function<org.jboss.util.graph.Vertex<S>, T> dataMapper) {
        Map<String, Vertex<T>> vertices = new TreeMap<>();
        List<Edge<T>> edges = new ArrayList<>();

        for (org.jboss.util.graph.Vertex<S> vertex : graph.getVerticies()) {
            Vertex<T> vertexRest = new Vertex<>(vertex.getName(), dataType.getName(), dataMapper.apply(vertex));
            vertices.put(vertexRest.getName(), vertexRest);
        }

        for (org.jboss.util.graph.Edge<S> edge : graph.getEdges()) {
            Edge<T> edgeDto = new Edge<>(edge.getFrom().getName(), edge.getTo().getName(), edge.getCost());
            edges.add(edgeDto);
        }

        Graph<T> graphRest = new Graph<>(vertices, edges);
        return graphRest;
    }

}
