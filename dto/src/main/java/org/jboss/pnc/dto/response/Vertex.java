/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Vertex in graph of objects.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Vertex.Builder.class)
public class Vertex<T> {

    /**
     * Name of the object.
     */
    private final String name;

    /**
     * The object type.
     */
    private final String dataType;

    /**
     * The object stored in the vertex.
     */
    private final T data;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder<T> {
    }
}
