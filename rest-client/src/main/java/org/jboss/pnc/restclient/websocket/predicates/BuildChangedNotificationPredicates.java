/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
