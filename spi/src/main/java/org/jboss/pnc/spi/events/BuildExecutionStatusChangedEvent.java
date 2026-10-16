/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.events;

import java.util.Optional;

import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;

public interface BuildExecutionStatusChangedEvent {

    BuildExecutionStatus getOldStatus();

    BuildExecutionStatus getNewStatus();

    String getBuildTaskId();

    Integer getBuildConfigurationId();

    /**
     * @return Returns non-empty only for completed states.
     */
    Optional<BuildResult> getBuildResult();

    boolean isFinal();
}
