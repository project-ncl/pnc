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
package org.jboss.pnc.rest.notifications.model;

import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.notifications.model.NotificationFactory;
import org.jboss.pnc.spi.notifications.model.BuildStatusChangedPayload;
import org.jboss.pnc.spi.notifications.model.Notification;
import org.jboss.pnc.spi.notifications.model.NotificationEventType;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class DefaultNotificationFactory implements NotificationFactory {

    private Map<BuildStatus, NotificationEventType> externalEvents = new HashMap<>();

    public DefaultNotificationFactory() {
        externalEvents.put(BuildStatus.REPO_SETTING_UP, NotificationEventType.BUILD_STARTED);
        externalEvents.put(BuildStatus.BUILD_COMPLETED_SUCCESS, NotificationEventType.BUILD_COMPLETED);
        externalEvents.put(BuildStatus.BUILD_COMPLETED_WITH_ERROR, NotificationEventType.BUILD_FAILED);
    }

    @Override
    public Notification createNotification(BuildStatusChangedEvent event) {
        if(!isExternal(event.getNewStatus())) {
            throw new IllegalArgumentException("This is not an external status.");
        }
        BuildStatusChangedPayload payload = new BuildStatusChangedPayload(event.getBuildTaskId(),
                externalEvents.get(event.getNewStatus()), event.getUserId());

        return new Notification(null, payload);
    }

    @Override
    public boolean isExternal(BuildStatus buildStatus) {
        return externalEvents.containsKey(buildStatus);
    }
}
