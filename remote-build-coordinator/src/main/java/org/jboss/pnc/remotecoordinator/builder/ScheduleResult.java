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
package org.jboss.pnc.remotecoordinator.builder;

import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.util.graph.Graph;

import java.util.Collection;

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
