/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.graph;

import lombok.Data;

/**
 * Neighbor of a node in a Graph;
 *
 * @author Patrik Korytár &lt;pkorytar@redhat.com&gt;
 */
@Data
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class VertexNeighbor<T> {

    private T neighborId;

    private Integer cost;

    public static final class Builder<T> {
    }
}
