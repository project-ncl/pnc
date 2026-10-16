/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test;

import org.jboss.pnc.remotecoordinator.builder.SetRecordTasks;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 4/26/16 Time: 9:29 AM
 */
public class BuildCoordinatorBeans {
    public final BuildTaskRepository taskRepository;
    public final BuildCoordinator coordinator;
    public final SetRecordTasks setJob;

    public BuildCoordinatorBeans(
            BuildTaskRepository taskRepository,
            BuildCoordinator coordinator,
            SetRecordTasks setJob) {
        this.taskRepository = taskRepository;
        this.coordinator = coordinator;
        this.setJob = setJob;
    }
}
