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
 * Edge in graph of objects.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Edge.Builder.class)
public class Edge<T> {

    /**
     * Source vertex name.
     */
    private final String source;

    /**
     * Target vertex name.
     */
    private final String target;

    /**
     * Edge cost.
     */
    private final int cost;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder<T> {
    }
}
