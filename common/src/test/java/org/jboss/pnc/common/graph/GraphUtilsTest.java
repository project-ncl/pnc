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
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class GraphUtilsTest {

    private final Logger logger = LoggerFactory.getLogger(GraphUtilsTest.class);

    @Test
    public void shouldMergeGraphs() {
        Graph graphTarget = new Graph();

        Entry entry1 = new Entry("1");
        Vertex<Entry> vertex1 = new NameUniqueVertex<>(entry1.name, entry1);
        graphTarget.addVertex(vertex1);

        Entry entry2 = new Entry("2");
        Vertex<Entry> vertex2 = new NameUniqueVertex<>(entry2.name, entry2);
        graphTarget.addVertex(vertex2);
        graphTarget.addEdge(vertex1, vertex2, 1);

        logger.debug("TargetGraph.edges {}.", graphTarget.getEdges());

        Entry entry3 = new Entry("3");
        Vertex<Entry> vertex3 = new NameUniqueVertex<>(entry3.name, entry3);
        graphTarget.addVertex(vertex3);
        Graph graphToMerge = new Graph();
        graphToMerge.addVertex(vertex1);
        graphToMerge.addVertex(vertex3);
        graphToMerge.addEdge(vertex1, vertex3, 1);
        GraphUtils.merge(graphTarget, graphToMerge);

        Entry entry4 = new Entry("4");
        Vertex<Entry> vertex4 = new NameUniqueVertex<>(entry4.name, entry4);
        Graph graphToMerge2 = new Graph();
        graphToMerge2.addVertex(vertex2);
        graphToMerge2.addVertex(vertex4);
        graphToMerge2.addEdge(vertex2, vertex4, 1);
        GraphUtils.merge(graphTarget, graphToMerge2);
        logger.debug("TargetGraph.edges {}.", graphTarget.getEdges());

        Entry entry5 = new Entry("5");
        Vertex<Entry> vertex5 = new NameUniqueVertex<>(entry5.name, entry5);
        Graph graphToMerge3 = new Graph();
        graphToMerge3.addVertex(vertex5);
        graphToMerge3.addVertex(vertex2);
        graphToMerge3.addEdge(vertex5, vertex2, 1);
        GraphUtils.merge(graphTarget, graphToMerge3);

        logger.info(graphTarget.toString());

        Assert.assertEquals(5, graphTarget.size());
        Assert.assertEquals(4, graphTarget.getEdges().size());

        Assert.assertEquals("1", graphTarget.findVertexByName("3").getIncomingEdge(0).getFrom().getName());
        Assert.assertEquals("3", graphTarget.findVertexByName("1").getOutgoingEdge(1).getTo().getName());

        Assert.assertEquals("2", graphTarget.findVertexByName("4").getIncomingEdge(0).getFrom().getName());
        Assert.assertEquals("4", graphTarget.findVertexByName("2").getOutgoingEdge(0).getTo().getName());

        Assert.assertEquals("5", graphTarget.findVertexByName("2").getIncomingEdge(1).getFrom().getName());
        Assert.assertEquals("2", graphTarget.findVertexByName("5").getOutgoingEdge(0).getTo().getName());
    }

    private static class Entry {
        String name;

        public Entry(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
