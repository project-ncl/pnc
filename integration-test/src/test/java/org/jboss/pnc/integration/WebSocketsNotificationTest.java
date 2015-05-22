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
import org.jboss.pnc.core.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.websockets.NotificationCollector;
import org.jboss.pnc.rest.notifications.Notifier;
import org.jboss.pnc.rest.notifications.websockets.NotificationsEndpoint;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
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
import javax.websocket.WebSocketContainer;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class WebSocketsNotificationTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final AtomicBoolean initialized = new AtomicBoolean();

    NotificationCollector notificationCollector;

    @Inject
    Event<BuildStatusChangedEvent> notificationEvent;

    @Inject
    Notifier notifier;

    @Deployment(name="WebSocketsNotificationTest")
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        restWar.addClass(WebSocketsNotificationTest.class);
        restWar.addClass(NotificationCollector.class);
        restWar.addPackage(NotificationsEndpoint.class.getPackage());
        restWar.addPackage(Notifier.class.getPackage());
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() throws Exception {
        if(!initialized.getAndSet(true)) {
            notificationCollector = new NotificationCollector();
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://localhost:8080/pnc-rest/" + NotificationsEndpoint.ENDPOINT_PATH;
            container.connectToServer(notificationCollector, URI.create(uri));
            waitForWSClientConnection();
        }
        notificationCollector.clear();
    }

    @Test
    public void shouldConnectToWebSockets() throws Exception {
        //given
        BuildStatusChangedEvent buildStatusChangedEvent = new DefaultBuildStatusChangedEvent(BuildStatus.NEW, BuildStatus.BUILD_WAITING, 1, null);
        String expectedJsonResponse = "{\"oldStatus\":\"NEW\",\"newStatus\":\"BUILD_WAITING\",\"buildTaskId\":1,\"buildExecution\":null}";

        //when
        notificationEvent.fire(buildStatusChangedEvent);
        waitForMessages();

        //then
        assertThat(notificationCollector.getMessages().get(0)).isEqualTo(expectedJsonResponse);
    }

    private void waitForMessages() {
        awaitFor(() -> notificationCollector.getMessages().size() > 0, 10_000);
    }

    private void waitForWSClientConnection() {
        awaitFor(() -> notifier.getAttachedClientsCount() > 0, 10_000);
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
