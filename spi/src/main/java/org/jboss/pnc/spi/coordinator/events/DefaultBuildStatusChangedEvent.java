/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.coordinator.events;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

public class DefaultBuildStatusChangedEvent implements BuildStatusChangedEvent {

    private final BuildStatus oldStatus;
    private final BuildStatus newStatus;
    private final Build build;

    public DefaultBuildStatusChangedEvent(Build build, BuildStatus oldStatus, BuildStatus newStatus) {
        this.build = build;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public BuildStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public BuildStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public Build getBuild() {
        return build;
    }

    @Override
    public String toString() {

        return "DefaultBuildStatusChangedEvent{" + "oldStatus=" + oldStatus + ", newStatus=" + newStatus + ", build="
                + build + '}';
    }
}
