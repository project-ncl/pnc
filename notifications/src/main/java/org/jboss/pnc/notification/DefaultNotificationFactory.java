/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.notification;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.notifications.model.BuildChangedPayload;
import org.jboss.pnc.spi.notifications.model.BuildSetChangedPayload;
import org.jboss.pnc.spi.notifications.model.EventType;
import org.jboss.pnc.spi.notifications.model.Notification;
import org.jboss.pnc.spi.notifications.model.NotificationFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultNotificationFactory implements NotificationFactory {

    public DefaultNotificationFactory() {
    }

    @Override
    public Notification createNotification(BuildStatusChangedEvent event) {
        Build build = event.getBuild();
        BuildChangedPayload payload = BuildChangedPayload.builder()
                .oldStatus(event.getOldStatus().toString())
                .build(build)
                .buildMe();

        return new Notification(EventType.BUILD_STATUS_CHANGED, null, payload);
    }

    @Override
    public Notification createNotification(BuildSetStatusChangedEvent event) {
        BuildSetChangedPayload payload = new BuildSetChangedPayload(event.getBuildSetTaskId(), event.getNewStatus(),
                event.getBuildSetConfigurationId(), event.getBuildSetConfigurationName(), event.getBuildSetStartTime(),
                event.getBuildSetEndTime(), event.getUserId(), event.getDescription());

        return new Notification(EventType.BUILD_SET_STATUS_CHANGED, null, payload);
    }

}
