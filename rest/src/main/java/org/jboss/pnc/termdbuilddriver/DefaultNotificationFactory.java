/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.notifications.model.*;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class DefaultNotificationFactory implements NotificationFactory {

    private Map<BuildStatus, NotificationEventType> externalBuildStatusChangedEvents = new HashMap<>();
    private Map<BuildSetStatus, NotificationEventType> externalBuildSetStatusChangedEvents = new HashMap<>();

    public DefaultNotificationFactory() {
        externalBuildStatusChangedEvents.put(BuildStatus.REPO_SETTING_UP, NotificationEventType.BUILD_STARTED);
        externalBuildStatusChangedEvents.put(BuildStatus.BUILD_COMPLETED_SUCCESS, NotificationEventType.BUILD_COMPLETED);
        externalBuildStatusChangedEvents.put(BuildStatus.BUILD_COMPLETED_WITH_ERROR, NotificationEventType.BUILD_FAILED);
        externalBuildStatusChangedEvents.put(BuildStatus.SYSTEM_ERROR, NotificationEventType.BUILD_FAILED);
        externalBuildStatusChangedEvents.put(BuildStatus.REJECTED, NotificationEventType.BUILD_FAILED);

        externalBuildSetStatusChangedEvents.put(BuildSetStatus.NEW, NotificationEventType.BUILD_SET_STARTED);
        externalBuildSetStatusChangedEvents.put(BuildSetStatus.DONE, NotificationEventType.BUILD_SET_COMPLETED);
        externalBuildSetStatusChangedEvents.put(BuildSetStatus.REJECTED, NotificationEventType.BUILD_SET_FAILED);
    }

    @Override
    public Notification createNotification(BuildStatusChangedEvent event) {
        if(!isExternal(event.getNewStatus())) {
            throw new IllegalArgumentException("This is not an external status.");
        }
        BuildChangedPayload payload = new BuildChangedPayload(event.getBuildTaskId(),
                externalBuildStatusChangedEvents.get(event.getNewStatus()), event.getUserId());

        return new Notification(null, payload);
    }

    @Override
    public Notification createNotification(BuildSetStatusChangedEvent event) {
        if(!isExternal(event.getNewStatus())) {
            throw new IllegalArgumentException("This is not an external status.");
        }
        BuildChangedPayload payload = new BuildChangedPayload(event.getBuildSetTaskId(),
                externalBuildSetStatusChangedEvents.get(event.getNewStatus()), event.getUserId());

        return new Notification(null, payload);
    }

    @Override
    public boolean isExternal(BuildStatus buildStatus) {
        return externalBuildStatusChangedEvents.containsKey(buildStatus);
    }

    @Override
    public boolean isExternal(BuildSetStatus buildStatus) {
        return externalBuildSetStatusChangedEvents.containsKey(buildStatus);
    }
}
