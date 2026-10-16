/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Statistics of running builds.
 *
 * @author jbrazdil
 */
@AllArgsConstructor
@Data
public class RunningBuildCount {

    /**
     * Number of running builds.
     */
    private int running;
    /**
     * Number of enqueued builds.
     */
    private int enqueued;
    /**
     * Number of builds waiting for dependencies.
     */
    private int waitingForDependencies;

}
