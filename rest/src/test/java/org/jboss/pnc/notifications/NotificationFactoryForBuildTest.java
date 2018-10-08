/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.notifications;

import org.jboss.pnc.rest.notifications.DefaultNotificationFactory;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.notifications.model.BuildChangedPayload;
import org.jboss.pnc.spi.notifications.model.EventType;
import org.jboss.pnc.spi.notifications.model.Notification;
import org.jboss.pnc.spi.notifications.model.NotificationFactory;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationFactoryForBuildTest {

    @Test
    public void shouldConvertSuccessfulNotificationEvent() throws Exception {

        // given
        BuildCoordinationStatusChangedEvent event = new DefaultBuildStatusChangedEvent(BuildCoordinationStatus.NEW,
                BuildCoordinationStatus.DONE, 1, 1, 1, "Build1", new Date(1453118400000L), new Date(1453122000000L), 1);

        NotificationFactory notificationFactory = new DefaultNotificationFactory();

        //when
        Notification notification = notificationFactory.createNotification(event);

        //then
        assertThat(notification.getExceptionMessage()).isNull();
        assertThat(notification.getEventType()).isEqualTo(EventType.BUILD_STATUS_CHANGED);
        assertThat(((BuildChangedPayload)notification.getPayload()).getBuildCoordinationStatus()).isEqualTo(BuildCoordinationStatus.DONE);
        assertThat(((BuildChangedPayload) notification.getPayload()).getBuildConfigurationId()).isEqualTo(1);
        assertThat(((BuildChangedPayload) notification.getPayload()).getBuildConfigurationName()).isEqualTo("Build1");
        assertThat(((BuildChangedPayload) notification.getPayload()).getBuildStartTime()).isEqualTo(new Date(1453118400000L));
        assertThat(((BuildChangedPayload) notification.getPayload()).getBuildEndTime()).isEqualTo(new Date(1453122000000L));
        assertThat(notification.getPayload()).isNotNull();
        assertThat(notification.getPayload().getId()).isEqualTo(1);
        assertThat(notification.getPayload().getUserId()).isEqualTo(1);
    }
}