package org.jboss.pnc.rest.notifications.model;

public interface NotificationPayload {
    NotificationEventType getEventType();
    Integer getId();
    Integer getUserId();
}
