/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.builder;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.jboss.pnc.spi.exception.ScheduleException;
import org.jboss.util.graph.Graph;

public interface RexBuildScheduler {

    /**
     *
     * @param buildGraph
     * @param user
     * @param buildConfigSetRecordId group recordId or null if it is not a group build
     * @throws ScheduleException
     */
    void startBuilding(Graph<RemoteBuildTask> buildGraph, User user, Base32LongID buildConfigSetRecordId)
            throws ScheduleException;

    void cancel(String taskId) throws RemoteRequestException;

    long getBuildQueueSize() throws RemoteRequestException;

    void setBuildQueueSize(long queueSize) throws RemoteRequestException;
}
