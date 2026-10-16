/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Graph of objects.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@EqualsAndHashCode
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

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder<T> {
    }
}
