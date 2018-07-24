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
package org.jboss.pnc.common.graph;

import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class GraphUtils {

    /**
     * Adds all elements from toMerge to target.
     */
    public static <T> void merge(Graph<T> target, Graph<T> toMerge) {
        List<Vertex<T>> addedVerticies = new ArrayList<>();
        for (Vertex<T> vertex : toMerge.getVerticies()) {
            boolean added = target.addVertex(vertex);
            if (added) {
                addedVerticies.add(vertex);
            }
        }

        //create edges
        for (Vertex<T> vertex: addedVerticies) {
            for (Object o : Collections.unmodifiableCollection(vertex.getOutgoingEdges())) {
                Edge<T> edge = (Edge<T>) o;
                target.addEdge(
                        target.findVertexByName(edge.getFrom().getName()),
                        target.findVertexByName(edge.getTo().getName()),
                        edge.getCost());
            }
        }

    }
}
