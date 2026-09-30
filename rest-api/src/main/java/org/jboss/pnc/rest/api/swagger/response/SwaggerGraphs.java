/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.api.swagger.response;

import java.util.List;
import java.util.Map;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.Edge;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Vertex;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class SwaggerGraphs {
    public class BuildsGraph extends Graph<Build> {

        public BuildsGraph(Map<String, Vertex<Build>> vertices, List<Edge<Build>> edges) {
            super(vertices, edges);
        }
    }
}
