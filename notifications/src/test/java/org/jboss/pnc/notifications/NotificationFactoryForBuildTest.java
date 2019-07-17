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

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mock.dto.BuildConfigurationRevisionMock;
import org.jboss.pnc.mock.dto.BuildEnvironmentMock;
import org.jboss.pnc.mock.dto.ProjectMock;
import org.jboss.pnc.mock.dto.SCMRepositoryMock;
import org.jboss.pnc.mock.dto.UserMock;
import org.jboss.pnc.notification.DefaultNotificationFactory;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.notifications.model.BuildChangedPayload;
import org.jboss.pnc.spi.notifications.model.EventType;
import org.jboss.pnc.spi.notifications.model.Notification;
import org.jboss.pnc.spi.notifications.model.NotificationFactory;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationFactoryForBuildTest {

    @Test
    public void shouldConvertSuccessfulNotificationEvent() throws Exception {

        // given
        String buildConfigurationName = "Build1";
        Instant startTime = new Date(1453118400000L).toInstant();
        Instant endTime = new Date(1453122000000L).toInstant();
        Build build = Build.builder()
                .id("1")
                .status(BuildStatus.SUCCESS)
                .buildContentId("build-42")
                .temporaryBuild(true)
                .project(ProjectMock.newProjectRef())
                .repository(SCMRepositoryMock.newScmRepository())
                .environment(BuildEnvironmentMock.newBuildEnvironment())
                .user(UserMock.newUser())
                .buildConfigurationRevision(BuildConfigurationRevisionMock.newBuildConfigurationRevisionRef(buildConfigurationName))
                .startTime(startTime)
                .endTime(endTime)
                .build();

        BuildStatusChangedEvent event = new DefaultBuildStatusChangedEvent(build, BuildStatus.NEW, build.getStatus());

        NotificationFactory notificationFactory = new DefaultNotificationFactory();

        //when
        Notification notification = notificationFactory.createNotification(event);

        //then
        assertThat(notification.getExceptionMessage()).isNull();
        assertThat(notification.getEventType()).isEqualTo(EventType.BUILD_STATUS_CHANGED);
        assertThat(((BuildChangedPayload) notification.getPayload()).getBuild().getStatus()).isEqualTo(BuildStatus.SUCCESS);
        assertThat(((BuildChangedPayload) notification.getPayload()).getBuild().getBuildConfigurationRevision().getId()).isEqualTo(1);
        assertThat(((BuildChangedPayload) notification.getPayload()).getBuild().getBuildConfigurationRevision().getName()).isEqualTo(buildConfigurationName);
        assertThat(((BuildChangedPayload) notification.getPayload()).getBuild().getStartTime()).isEqualTo(startTime);
        assertThat(((BuildChangedPayload) notification.getPayload()).getBuild().getEndTime()).isEqualTo(endTime);
        assertThat(notification.getPayload()).isNotNull();
        assertThat(notification.getPayload().getId()).isEqualTo(1);
        assertThat(notification.getPayload().getUserId()).isEqualTo(1);
    }
}