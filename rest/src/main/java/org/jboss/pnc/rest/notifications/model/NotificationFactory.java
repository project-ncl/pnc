package org.jboss.pnc.rest.notifications.model;

import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class NotificationFactory {

    private Map<BuildStatus, NotificationEventType> externalEvents = new HashMap<>();

    public NotificationFactory() {
        externalEvents.put(BuildStatus.REPO_SETTING_UP, NotificationEventType.BUILD_STARTED);
        externalEvents.put(BuildStatus.BUILD_COMPLETED_SUCCESS, NotificationEventType.BUILD_COMPLETED);
        externalEvents.put(BuildStatus.BUILD_COMPLETED_WITH_ERROR, NotificationEventType.BUILD_FAILED);
    }

    public Notification createNotification(BuildStatusChangedEvent event) {
        if(!isExternal(event.getNewStatus())) {
            throw new IllegalArgumentException("This is not an external status.");
        }
        BuildStatusChangedPayload payload = new BuildStatusChangedPayload(event.getBuildTaskId(),
                externalEvents.get(event.getNewStatus()), event.getUserId());

        return new Notification(null, payload);
    }

    public boolean isExternal(BuildStatus buildStatus) {
        return externalEvents.containsKey(buildStatus);
    }
}
