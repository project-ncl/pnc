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

import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

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
}
