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
package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.websockets.NotificationCollector;
import org.jboss.pnc.rest.notifications.websockets.NotificationsEndpoint;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class WebSocketsNotificationTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static NotificationCollector notificationCollector;

    @Inject
    Event<BuildCoordinationStatusChangedEvent> buildStatusNotificationEvent;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusNotificationEvent;

    @Inject
    Notifier notifier;

    @Deployment(name="WebSocketsNotificationTest")
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        restWar.addClass(WebSocketsNotificationTest.class);
        restWar.addClass(NotificationCollector.class);
        restWar.addPackage(NotificationsEndpoint.class.getPackage());
        restWar.addPackage(Notifier.class.getPackage());
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(1)
    public void setUp() throws Exception {
        notificationCollector = new NotificationCollector();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://localhost:8080/pnc-rest/" + NotificationsEndpoint.ENDPOINT_PATH;
        container.connectToServer(notificationCollector, URI.create(uri));
        waitForWSClientConnection();
        logger.info("Connected to notification client.");
        notificationCollector.clear();
    }

    @Test
    @InSequence(2)
    public void shouldReceiveBuildStatusChangeNotification() throws Exception {
        // given
        BuildCoordinationStatusChangedEvent buildStatusChangedEvent = new DefaultBuildStatusChangedEvent(BuildCoordinationStatus.NEW,
                BuildCoordinationStatus.DONE, 1, 1, "Build1", new Date(1453118400000L), new Date(1453122000000L), 1);
        String expectedJsonResponse = "{\"eventType\":\"BUILD_STATUS_CHANGED\",\"payload\":{\"id\":1,\"buildCoordinationStatus\":\"DONE\",\"userId\":1,\"buildConfigurationId\":1,\"buildConfigurationName\":\"Build1\",\"buildStartTime\":1453118400000,\"buildEndTime\":1453122000000}}";

        //when
        buildStatusNotificationEvent.fire(buildStatusChangedEvent);

        //then
        Wait.forCondition(() -> isReceived(expectedJsonResponse), 15, ChronoUnit.SECONDS);
    }

    @Test
    @InSequence(3)
    public void shouldReceiveBuildSetStatusChangeNotification() throws Exception {
        // given
        BuildSetStatusChangedEvent buildStatusChangedEvent = new DefaultBuildSetStatusChangedEvent(BuildSetStatus.NEW,
                BuildSetStatus.DONE, 1, 1, "BuildSet1", new Date(1453118400000L), new Date(1453122000000L), 1);
        String expectedJsonResponse = "{\"eventType\":\"BUILD_SET_STATUS_CHANGED\",\"payload\":{\"id\":1,\"buildStatus\":\"DONE\",\"userId\":1,\"buildSetConfigurationId\":1,\"buildSetConfigurationName\":\"BuildSet1\",\"buildSetStartTime\":1453118400000,\"buildSetEndTime\":1453122000000}}";

        //when
        buildSetStatusNotificationEvent.fire(buildStatusChangedEvent);

        //then
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
        while(System.currentTimeMillis() < waitUntil) {
            if(condition.get()) {
                return;
            }
            Thread.currentThread().yield();
        }
        throw new AssertionError("Timeout when waiting for condition");
    }
}
