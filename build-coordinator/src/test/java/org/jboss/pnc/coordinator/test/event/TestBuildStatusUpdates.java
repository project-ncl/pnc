/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.test.event;

import javax.enterprise.event.Observes;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.junit.Assert;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TestBuildStatusUpdates {
    public void collectEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        Assert.assertNotEquals(
                "Status update event should not be fired if there is no status updates. " + buildStatusChangedEvent,
                buildStatusChangedEvent.getNewStatus(),
                buildStatusChangedEvent.getOldStatus());
    }
}