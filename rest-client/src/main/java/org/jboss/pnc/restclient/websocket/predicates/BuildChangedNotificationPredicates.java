/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.restclient.websocket.predicates;

import java.util.function.Predicate;

import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.enums.BuildStatus;

public final class BuildChangedNotificationPredicates {

    private BuildChangedNotificationPredicates() {
    }

    public static Predicate<BuildChangedNotification> withBuildId(String buildId) {
        return (notification) -> notification.getBuild().getId().equals(buildId);
    }

    public static Predicate<BuildChangedNotification> withBuildStatus(BuildStatus status) {
        return (notification) -> notification.getBuild().getStatus().equals(status);
    }

    public static Predicate<BuildChangedNotification> isSuccessful() {
        return (notification) -> notification.getBuild().getStatus().completedSuccessfully();
    }

    public static Predicate<BuildChangedNotification> withBuildCompleted() {
        return (notification) -> notification.getBuild().getStatus().isFinal();
    }

    public static Predicate<BuildChangedNotification> withBuildConfiguration(String buildConfigId) {
        return (notification) -> notification.getBuild().getBuildConfigRevision().getId().equals(buildConfigId);
    }
}
