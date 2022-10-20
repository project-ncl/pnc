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
package org.jboss.pnc.integration.notifications;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.mock.dto.BuildMock;
import org.jboss.pnc.notification.Action;
import org.jboss.pnc.notification.MessageType;
import org.jboss.pnc.rest.endpoints.notifications.NotificationsEndpoint;
import org.jboss.pnc.notification.ProgressUpdatesRequest;
import org.jboss.pnc.notification.TypedMessage;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;

import static org.jboss.pnc.integration.setup.RestClientConfiguration.NOTIFICATION_PATH;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProcessProgressNotificationTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    NotificationCollector notificationCollector;

    @Inject
    Event<BuildStatusChangedEvent> buildStatusNotificationEvent;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusNotificationEvent;

    @Inject
    Notifier notifier;

    RemoteEndpoint.Async asyncRemote;

    @Deployment(name = "WebSocketsNotificationTest")
    public static EnterpriseArchive deploy() {
        EnterpriseArchive ear = Deployments.testEarForInContainerTest(
                Collections.singletonList(NotificationsEndpoint.class.getPackage()),
                Arrays.asList(BuildMock.class.getPackage()),
                ProcessProgressNotificationTest.class,
                NotificationCollector.class);
        logger.info("Deployment:" + ear.toString(true));
        return ear;
    }

    @Before
    public void before() throws Exception {
        notificationCollector = new NotificationCollector();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://localhost:8080" + NOTIFICATION_PATH;
        Session session = container.connectToServer(notificationCollector, URI.create(uri));
        waitForWSClientConnection();
        notificationCollector.clear();
        asyncRemote = session.getAsyncRemote();
    }

    @Test
    public void shouldSubscribeToProcessUpdatesNotification() throws Exception {

        // given
        Integer taskId = 1;

        Build build = BuildMock.newBuild(taskId, BuildStatus.SUCCESS, "Build1");

        BuildStatusChangedEvent buildStatusChangedEvent = new DefaultBuildStatusChangedEvent(
                build,
                BuildStatus.NEW,
                build.getStatus());

        // when
        buildStatusNotificationEvent.fire(buildStatusChangedEvent);
        ProgressUpdatesRequest progressUpdatesRequest = new ProgressUpdatesRequest(
                Action.SUBSCRIBE,
                "component-build",
                taskId.toString());
        String text = JsonOutputConverterMapper
                .apply(new TypedMessage<>(MessageType.PROCESS_UPDATES, progressUpdatesRequest));
        logger.info("Sending test message:" + text);
        asyncRemote.sendText(text);
        waitForMessages(1);

        // then
        logger.info("Received: " + notificationCollector.getMessages().get(0));

        assertTrue(
                notificationCollector.getMessages()
                        .get(0)
                        .startsWith(
                                "{\"oldStatus\":\"NEW\",\"build\":{\"id\":\"1\",\"submitTime\":null,\"startTime\":null,\"endTime\":null,\"progress\":null,\"status\":\"SUCCESS\","));
    }

    private void waitForMessages(int numberOfMessages) {
        awaitFor(() -> notificationCollector.getMessages().size() >= numberOfMessages, 60_000);
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
