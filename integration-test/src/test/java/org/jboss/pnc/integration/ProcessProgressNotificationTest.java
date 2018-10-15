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
package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.websockets.NotificationCollector;
import org.jboss.pnc.rest.notifications.websockets.Action;
import org.jboss.pnc.rest.notifications.websockets.MessageType;
import org.jboss.pnc.rest.notifications.websockets.NotificationsEndpoint;
import org.jboss.pnc.rest.notifications.websockets.ProgressUpdatesRequest;
import org.jboss.pnc.rest.notifications.websockets.TypedMessage;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
import java.util.Date;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProcessProgressNotificationTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    NotificationCollector notificationCollector;

    @Inject
    Event<BuildCoordinationStatusChangedEvent> buildStatusNotificationEvent;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusNotificationEvent;

    @Inject
    Notifier notifier;

    RemoteEndpoint.Async asyncRemote;

    @Deployment(name="WebSocketsNotificationTest")
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        restWar.addClass(ProcessProgressNotificationTest.class);
        restWar.addClass(NotificationCollector.class);
        restWar.addPackage(NotificationsEndpoint.class.getPackage());
        restWar.addPackage(Notifier.class.getPackage());
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() throws Exception {
        notificationCollector = new NotificationCollector();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://localhost:8080/pnc-rest/" + NotificationsEndpoint.ENDPOINT_PATH;
        Session session = container.connectToServer(notificationCollector, URI.create(uri));
        waitForWSClientConnection();
        notificationCollector.clear();
        asyncRemote = session.getAsyncRemote();
    }

    @Test
    public void shouldSubscribeToProcessUpdatesNotification() throws Exception {

        // given
        Integer taskId = 1;
        BuildCoordinationStatusChangedEvent buildStatusChangedEvent = new DefaultBuildStatusChangedEvent(BuildCoordinationStatus.NEW,
                BuildCoordinationStatus.DONE, taskId, 1, 1, "Build1", new Date(1453118400000L), new Date(1453122000000L), 1);

        //when
        buildStatusNotificationEvent.fire(buildStatusChangedEvent);
        ProgressUpdatesRequest progressUpdatesRequest = new ProgressUpdatesRequest(Action.SUBSCRIBE,
                "component-build",
                taskId.toString());
        String text = JsonOutputConverterMapper.apply(new TypedMessage<ProgressUpdatesRequest>(MessageType.PROCESS_UPDATES,
                progressUpdatesRequest));
        logger.info("Sending test message:" + text);
        asyncRemote.sendText(text);
        waitForMessages(1);

        //then
        logger.info("Received: " + notificationCollector.getMessages().get(0)); //"eventType":"BUILD_STATUS_CHANGED","payload":{"id":1,"buildCoordinationStatus":"DONE
        assertThat(notificationCollector.getMessages().get(0)).startsWith("{\"eventType\":\"BUILD_STATUS_CHANGED\",\"payload\":{\"id\":1,\"buildCoordinationStatus\":\"DONE\"");
    }

    private void waitForMessages(int numberOfMessages) {
        awaitFor(() -> notificationCollector.getMessages().size() >= numberOfMessages, 60_000);
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
