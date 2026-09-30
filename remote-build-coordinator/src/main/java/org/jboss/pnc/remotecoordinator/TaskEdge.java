/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Edge in graph of objects.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@AllArgsConstructor
public class TaskEdge {

    /**
     * Source vertex name.
     */
    private final String source;

    /**
     * Target vertex name.
     */
    private final String target;

}
