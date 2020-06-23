/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration.notifications;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.mock.dto.BuildMock;
import org.jboss.pnc.rest.endpoints.notifications.NotificationsEndpoint;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.User;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class WebSocketsNotificationTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static NotificationCollector notificationCollector;
    private final JacksonProvider mapperProvider = new JacksonProvider();

    @Inject
    Event<BuildStatusChangedEvent> buildStatusNotificationEvent;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusNotificationEvent;

    @Inject
    Notifier notifier;

    @Deployment(name = "WebSocketsNotificationTest")
    public static EnterpriseArchive deploy() {
        return Deployments.testEarForInContainerTest(
                Collections.singletonList(NotificationsEndpoint.class.getPackage()),
                Collections.singletonList(BuildMock.class.getPackage()),
                WebSocketsNotificationTest.class,
                NotificationCollector.class);
    }

    @Test
    @InSequence(1)
    public void setUp() throws Exception {
        notificationCollector = new NotificationCollector();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://localhost:8080/pnc-rest-new/" + NotificationsEndpoint.ENDPOINT_PATH;
        container.connectToServer(notificationCollector, URI.create(uri));
        waitForWSClientConnection();
        logger.info("Connected to notification client.");
        notificationCollector.clear();
    }

    @Test
    @InSequence(2)
    public void shouldReceiveBuildStatusChangeNotification() throws Exception {
        // given
        Build build = BuildMock.newBuild(BuildStatus.SUCCESS, "Build1");

        BuildStatusChangedEvent buildStatusChangedEvent = new DefaultBuildStatusChangedEvent(
                build,
                BuildStatus.NEW,
                build.getStatus());

        String buildString = mapperProvider.getMapper().writeValueAsString(build);
        String expectedJsonResponse = "{\"oldStatus\":\"NEW\",\"build\":" + buildString
                + ",\"job\":\"BUILD\",\"notificationType\":\"BUILD_STATUS_CHANGED\",\"progress\":\"FINISHED\",\"oldProgress\":\"PENDING\",\"message\":null}";

        // when
        buildStatusNotificationEvent.fire(buildStatusChangedEvent);

        // then
        Wait.forCondition(() -> isReceived(expectedJsonResponse), 15, ChronoUnit.SECONDS);
    }

    @Test
    @InSequence(3)
    public void shouldReceiveBuildSetStatusChangeNotification() throws Exception {
        // given
        GroupBuild groupBuild = GroupBuild.builder()
                .id("1")
                .groupConfig(GroupConfigurationRef.refBuilder().id("1").name("BuildSet1").build())
                .startTime(Instant.ofEpochMilli(1453118400000L))
                .endTime(Instant.ofEpochMilli(1453122000000L))
                .user(User.builder().id("1").username("user1").build())
                .status(BuildStatus.SUCCESS)
                .build();

        BuildSetStatusChangedEvent buildStatusChangedEvent = new DefaultBuildSetStatusChangedEvent(
                BuildSetStatus.NEW,
                BuildSetStatus.DONE,
                groupBuild,
                "description");
        String groupBuildString = mapperProvider.getMapper().writeValueAsString(groupBuild);
        String expectedJsonResponse = "{\"groupBuild\":" + groupBuildString
                + ",\"job\":\"GROUP_BUILD\",\"notificationType\":\"GROUP_BUILD_STATUS_CHANGED\",\"progress\":\"FINISHED\",\"oldProgress\":\"IN_PROGRESS\",\"message\":null}";

        // when
        buildSetStatusNotificationEvent.fire(buildStatusChangedEvent);

        // then
        Wait.forCondition(() -> isReceived(expectedJsonResponse), 15, ChronoUnit.SECONDS);
    }

    private boolean isReceived(String expectedJsonResponse) {
        logger.debug("notificationCollector: {}.", notificationCollector);
        List<String> messages = notificationCollector.getMessages();
        logger.debug("Current messages: {}.", messages.stream().collect(Collectors.joining()));
        return messages.contains(expectedJsonResponse);
    }

    private void waitForWSClientConnection() {
        awaitFor(() -> notifier.getAttachedClientsCount() > 0, 60_000);
    }

    private void awaitFor(Supplier<Boolean> condition, int timeMs) {
        long waitUntil = System.currentTimeMillis() + timeMs;
        while (System.currentTimeMillis() < waitUntil) {
            if (condition.get()) {
                return;
            }
            Thread.currentThread().yield();
        }
        throw new AssertionError("Timeout when waiting for condition");
    }
}
