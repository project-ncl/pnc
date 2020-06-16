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
package org.jboss.pnc.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Graph of objects.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Graph.Builder.class)
public class Graph<T> {

    /**
     * Map of vertices with the vertex name as a key.
     */
    private final Map<String, Vertex<T>> vertices;

    /**
     * List of graph edges.
     */
    private final List<Edge<T>> edges;

    /**
     * Generic map of key-value properties describing the graph.
     */
    private final Map<String, String> metadata;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder<T> {
    }
}
