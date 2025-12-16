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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.JaasAuthenticationProvider;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.OperationsManager;
import org.jboss.pnc.integration.notifications.auth.JaasAuthenticationProviderMock;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.mock.dto.BuildMock;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.notification.Local;
import org.jboss.pnc.rest.endpoints.notifications.NotificationsEndpoint;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.events.OperationChangedEvent;
import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.jboss.pnc.integration.setup.RestClientConfiguration.NOTIFICATION_PATH;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class WebSocketsNotificationTest {

    public static final Logger logger = LoggerFactory.getLogger(WebSocketsNotificationTest.class);

    private static NotificationCollector notificationCollector;
    private final JacksonProvider mapperProvider = new JacksonProvider();
    private static DeliverableAnalyzerOperation OPERATION;
    private static String PRODUCT_MILESTONE_ID;
    private static String MILESTONE_START_DATE;
    private static String MILESTONE_END_DATE;
    private static String MILESTONE_PLANNED_END_DATE;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    @Local
    Notifier notifier;
    @Inject
    Event<BuildStatusChangedEvent> buildStatusNotificationEvent;
    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusNotificationEvent;
    @Inject
    OperationsManager operationsManager;
    @Inject
    Event<OperationChangedEvent> operationChangedEventEvent;
    @Inject
    ProductMilestoneRepository productMilestoneRepository;

    @Deployment(name = "WebSocketsNotificationTest")
    public static EnterpriseArchive deploy() {
        EnterpriseArchive ear = Deployments.testEarForInContainerTest(
                Collections.singletonList(NotificationsEndpoint.class.getPackage()),
                Collections.singletonList(BuildMock.class.getPackage()),
                WebSocketsNotificationTest.class,
                NotificationCollector.class);
        replaceRealJaasByFake(ear);
        logger.info("Deployment:" + ear.toString(true));
        return ear;
    }

    private static void replaceRealJaasByFake(EnterpriseArchive ear) {
        JavaArchive authJar = ear.getAsType(JavaArchive.class, "/auth.jar");
        authJar.deleteClass(JaasAuthenticationProvider.class);
        authJar.addClass(JaasAuthenticationProviderMock.class);
    }

    @Test
    @InSequence(1)
    public void setUp() throws Exception {
        notificationCollector = new NotificationCollector();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://localhost:8080" + NOTIFICATION_PATH;
        container.connectToServer(notificationCollector, URI.create(uri));
        waitForWSClientConnection();
        logger.info("Connected to notification client.");
        notificationCollector.clear();

        ProductMilestone productMilestone = productMilestoneRepository.queryAll().get(0);
        PRODUCT_MILESTONE_ID = productMilestone.getId().toString();
        MILESTONE_START_DATE = getIso8601FormatFromDate(productMilestone.getStartingDate());
        MILESTONE_END_DATE = getIso8601FormatFromDate(productMilestone.getEndDate());
        MILESTONE_PLANNED_END_DATE = getIso8601FormatFromDate(productMilestone.getPlannedEndDate());
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
                BuildStatus.NEW,
                BuildStatus.SUCCESS,
                groupBuild,
                "description");
        String groupBuildString = mapperProvider.getMapper().writeValueAsString(groupBuild);
        String expectedJsonResponse = "{\"groupBuild\":" + groupBuildString
                + ",\"job\":\"GROUP_BUILD\",\"notificationType\":\"GROUP_BUILD_STATUS_CHANGED\",\"progress\":\"FINISHED\",\"oldProgress\":\"PENDING\",\"message\":null}";

        // when
        buildSetStatusNotificationEvent.fire(buildStatusChangedEvent);

        // then
        Wait.forCondition(() -> isReceived(expectedJsonResponse), 15, ChronoUnit.SECONDS);
    }

    @Test
    @InSequence(2)
    public void shouldReceiveOperationChangedNotification() throws Exception {
        // when
        OPERATION = operationsManager.newDeliverableAnalyzerOperation(PRODUCT_MILESTONE_ID, Collections.emptyMap());
        String expectedJsonResponse = "{\"notificationType\":\"DELIVERABLES_ANALYSIS\",\"operationId\":\""
                + OPERATION.getId() + "\",\"progress\":\"PENDING\",\"oldProgress\":null,\"message\":null,"
                + "\"result\":null,\"operation\":{\"id\":\"" + OPERATION.getId() + "\",\"submitTime\":\""
                + getIso8601FormatFromDate(OPERATION.getSubmitTime())
                + "\",\"startTime\":null,\"endTime\":null,\"progressStatus\":\"NEW\",\"result\":null,\"outcome\":"
                + "{\"result\":null,\"reason\":null,\"proposal\":null},\"user\":{\"id\":\"100\",\"username\":"
                + "\"demo-user\"},\"parameters\":{},\"productMilestone\":{\"id\":\"100\",\"version\":\"1.0.0.Build1\","
                + "\"endDate\":" + asJsonValue(MILESTONE_END_DATE) + ",\"startingDate\":"
                + asJsonValue(MILESTONE_START_DATE) + ",\"plannedEndDate\":" + asJsonValue(MILESTONE_PLANNED_END_DATE)
                + "}},\"job\":\"OPERATION\"}";
        System.out.println("expected json: " + expectedJsonResponse);

        /*
         * Hello, reader! If you are here and wondering why the test is failing, I suggest you: 1. sleep for 10 seconds
         * 2. Read the list in notificationCollector to figure out what's the expected JSON output. 3. Perhaps that test
         * could be improved if we compare map objects instead of string?
         */
        // then
        Wait.forCondition(() -> isReceived(expectedJsonResponse), 15, ChronoUnit.SECONDS);
    }

    private boolean isReceived(String expectedJsonResponse) {
        logger.debug("notificationCollector: {}.", notificationCollector);
        List<String> messages = notificationCollector.getMessages();
        logger.debug("Current messages: {}.", messages.stream().collect(Collectors.joining()));

        try {
            JsonNode expectedJsonNode = OBJECT_MAPPER.readTree(expectedJsonResponse);

            for (String message : messages) {
                if (OBJECT_MAPPER.readTree(message).equals(expectedJsonNode)) {
                    return true;
                }
            }
        } catch (JsonProcessingException e) {
            logger.warn("Couldn't parse string to JsonNode", e);
        }

        // if we're here, nothing matched
        return false;
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

    private static String getIso8601FormatFromDate(Date date) {
        if (date == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    private String asJsonValue(String json) {
        if (json == null) {
            return "null";
        }
        return "\"" + json + "\"";
    }
}
