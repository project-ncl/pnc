/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.test;

import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 4/26/16 Time: 9:29 AM
 */
public class BuildCoordinatorBeans {
    public final BuildQueue queue;
    public final BuildCoordinator coordinator;

    public BuildCoordinatorBeans(BuildQueue queue, BuildCoordinator coordinator) {
        this.queue = queue;
        this.coordinator = coordinator;
    }
}
