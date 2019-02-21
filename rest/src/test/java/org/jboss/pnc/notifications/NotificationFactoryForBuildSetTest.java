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
package org.jboss.pnc.notifications;

import org.jboss.pnc.rest.notifications.DefaultNotificationFactory;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.notifications.model.BuildSetChangedPayload;
import org.jboss.pnc.spi.notifications.model.EventType;
import org.jboss.pnc.spi.notifications.model.Notification;
import org.jboss.pnc.spi.notifications.model.NotificationFactory;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationFactoryForBuildSetTest {

    @Test
    public void shouldConvertSuccessfulNotificationEvent() throws Exception {

        //given
        DefaultBuildSetStatusChangedEvent event = new DefaultBuildSetStatusChangedEvent(
                BuildSetStatus.NEW,
                BuildSetStatus.DONE,
                1,
                1,
                "BuildSet1",
                new Date(1453118400000L),
                new Date(1453122000000L),
                1, "description");
        NotificationFactory notificationFactory = new DefaultNotificationFactory();

        //when
        Notification notification = notificationFactory.createNotification(event);

        //then
        assertThat(notification.getExceptionMessage()).isNull();
        assertThat(notification.getEventType()).isEqualTo(EventType.BUILD_SET_STATUS_CHANGED);
        assertThat(((BuildSetChangedPayload)notification.getPayload()).getBuildStatus()).isEqualTo(BuildSetStatus.DONE);
        assertThat(((BuildSetChangedPayload)notification.getPayload()).getBuildSetConfigurationId()).isEqualTo(1);
        assertThat(((BuildSetChangedPayload)notification.getPayload()).getBuildSetConfigurationName()).isEqualTo("BuildSet1");
        assertThat(((BuildSetChangedPayload)notification.getPayload()).getBuildSetStartTime()).isEqualTo(new Date(1453118400000L));
        assertThat(((BuildSetChangedPayload)notification.getPayload()).getBuildSetEndTime()).isEqualTo(new Date(1453122000000L));
        assertThat(notification.getPayload()).isNotNull();
        assertThat(notification.getPayload().getId()).isEqualTo(1);
        assertThat(notification.getPayload().getUserId()).isEqualTo(1);
    }
}