package org.jboss.pnc.rest.notifications.model;

public class Notification {

    private final String exceptionMessage;

    private final NotificationPayload payload;

    public Notification(String exceptionMessage, NotificationPayload payload) {
        this.exceptionMessage = exceptionMessage;
        this.payload = payload;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public NotificationPayload getPayload() {
        return payload;
    }
}
