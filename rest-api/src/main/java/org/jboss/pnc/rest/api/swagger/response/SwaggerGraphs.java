package org.jboss.pnc.rest.api.swagger.response;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.Edge;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Vertex;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class SwaggerGraphs {
    public class BuildsGraph extends Graph<Build>{

        public BuildsGraph(Map<String, Vertex<Build>> vertices, List<Edge<Build>> edges, Map<String, String> metadata) {
            super(vertices, edges, metadata);
        }
    };
}
