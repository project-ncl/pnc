package org.jboss.pnc.restclient.websocket.predicates;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;

import java.util.function.Predicate;

import org.jboss.pnc.dto.notification.BuildPushResultNotification;

public final class BuildPushResultNotificationPredicates {

    private BuildPushResultNotificationPredicates() {
    }

    public static Predicate<BuildPushResultNotification> withPushCompleted() {
        return (notification) -> notification.getProgress().equals(FINISHED);
    }

    public static Predicate<BuildPushResultNotification> withBuildId(String buildId) {
        return (notification) -> notification.getBuildPushResult() == null ? false
                : notification.getBuildPushResult().getBuildId().equals(buildId);
    }

    public static Predicate<BuildPushResultNotification> withPushId(String pushId) {
        return (notification) -> notification.getBuildPushResult() == null ? false
                : notification.getBuildPushResult().getId().equals(pushId);
    }

}
