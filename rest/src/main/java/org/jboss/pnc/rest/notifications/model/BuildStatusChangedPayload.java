package org.jboss.pnc.rest.notifications.model;

public class BuildStatusChangedPayload implements NotificationPayload {

    private final Integer id;
    private final NotificationEventType eventType;
    private Integer userId;

    public BuildStatusChangedPayload(Integer id, NotificationEventType eventType, Integer userId) {
        this.id = id;
        this.eventType = eventType;
        this.userId = userId;
    }

    @Override
    public NotificationEventType getEventType() {
        return eventType;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }
}
