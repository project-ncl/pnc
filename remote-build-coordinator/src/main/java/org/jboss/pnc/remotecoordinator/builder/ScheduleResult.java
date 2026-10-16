/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.builder;

import java.util.Collection;

import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.util.graph.Graph;

public class ScheduleResult {
    Graph<RemoteBuildTask> buildGraph;
    BuildCoordinationStatus coordinationStatus;
    BuildStatusWithDescription buildStatusWithDescription;
    Collection<RemoteBuildTask> noRebuildTasks;

    public ScheduleResult(
            Graph<RemoteBuildTask> buildGraph,
            BuildCoordinationStatus coordinationStatus,
            BuildStatusWithDescription buildStatusWithDescription,
            Collection<RemoteBuildTask> noRebuildTasks) {
        this.buildGraph = buildGraph;
        this.coordinationStatus = coordinationStatus;
        this.buildStatusWithDescription = buildStatusWithDescription;
        this.noRebuildTasks = noRebuildTasks;
    }
}
