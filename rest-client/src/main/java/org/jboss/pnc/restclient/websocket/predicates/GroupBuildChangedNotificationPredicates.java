package org.jboss.pnc.restclient.websocket.predicates;

import java.util.function.Predicate;

import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.enums.BuildStatus;

public final class GroupBuildChangedNotificationPredicates {

    private GroupBuildChangedNotificationPredicates() {
    }

    public static Predicate<GroupBuildChangedNotification> withGBuildId(String groupBuildId) {
        return (notification) -> notification.getGroupBuild().getId().equals(groupBuildId);
    }

    public static Predicate<GroupBuildChangedNotification> withGBuildStatus(BuildStatus groupBuildId) {
        return (notification) -> notification.getGroupBuild().getId().equals(groupBuildId);
    }

    public static Predicate<GroupBuildChangedNotification> isSuccessful() {
        return (notification) -> notification.getGroupBuild().getStatus().completedSuccessfully();
    }

    public static Predicate<GroupBuildChangedNotification> withGBuildCompleted() {
        return (notification) -> notification.getGroupBuild().getStatus().isFinal();
    }
}
