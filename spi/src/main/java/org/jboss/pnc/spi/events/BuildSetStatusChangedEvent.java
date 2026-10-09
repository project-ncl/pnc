/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.events;

import java.util.Date;

import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.BuildSetStatus;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildSetStatusChangedEvent {

    /**
     * May return null if this is the first status change.
     *
     * @return The status of the build set before the change
     */
    BuildSetStatus getOldStatus();

    BuildSetStatus getNewStatus();

    BuildStatus getOldBuildStatus();

    BuildStatus getNewBuildStatus();

    GroupBuild getGroupBuild();

    String getBuildSetTaskId();

    String getUserId();

    String getBuildSetConfigurationId();

    String getBuildSetConfigurationName();

    Date getBuildSetStartTime();

    Date getBuildSetEndTime();

    String getDescription();
}
