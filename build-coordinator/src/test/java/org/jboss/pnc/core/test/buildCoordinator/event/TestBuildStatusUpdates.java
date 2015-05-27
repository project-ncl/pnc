package org.jboss.pnc.core.test.buildCoordinator.event;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.junit.Assert;

import javax.enterprise.event.Observes;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TestBuildStatusUpdates {
    public void collectEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        Assert.assertNotEquals("Status update event should not be fired if there is no status updates. " + buildStatusChangedEvent, buildStatusChangedEvent.getNewStatus(), buildStatusChangedEvent.getOldStatus());
    }
}