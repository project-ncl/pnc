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
    public class BuildsGraph extends Graph<Build> {

        public BuildsGraph(Map<String, Vertex<Build>> vertices, List<Edge<Build>> edges) {
            super(vertices, edges);
        }
    }
}
