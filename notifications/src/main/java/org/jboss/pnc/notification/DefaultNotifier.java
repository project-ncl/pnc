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
package org.jboss.pnc.notification;

import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.dto.notification.BuildPushResultNotification;
import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.dto.notification.ProductMilestoneCloseResultNotification;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.MessageCallback;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Notification mechanism for Web Sockets. All implementation details should be placed in AttachedClient.
 */
@ApplicationScoped
public class DefaultNotifier implements Notifier {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Set<AttachedClient> attachedClients = new CopyOnWriteArraySet<>();

    private final ScheduledExecutorService scheduler = MDCExecutors.newScheduledThreadPool(1);

    private final MessageCallback messageCallback = new MessageCallback() {

        @Override
        public void successful(AttachedClient attachedClient) {
            // logger.debug("Successfully sent message to client ", attachedClient);
        }

        @Override
        public void failed(AttachedClient attachedClient, Throwable throwable) {
            logger.error("Notification client threw an error, removing it", throwable);
            detachClient(attachedClient);
        }
    };

    @Inject
    BuildProvider buildProvider;

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::cleanUp, 1, 1, TimeUnit.HOURS);
    }

    @Override
    public void attachClient(AttachedClient attachedClient) {
        attachedClients.add(attachedClient);
    }

    @Override
    public void detachClient(AttachedClient attachedClient) {
        attachedClients.remove(attachedClient);
    }

    @Override
    public int getAttachedClientsCount() {
        return attachedClients.size();
    }

    @Override
    public Optional<AttachedClient> getAttachedClient(String sessionId) {
        return attachedClients.stream().filter(client -> client.getSessionId().equals(sessionId)).findAny();
    }

    @Override
    public MessageCallback getCallback() {
        return messageCallback;
    }

    @Override
    public void sendMessage(Object message) {
        for (AttachedClient client : attachedClients) {
            if (client.isEnabled()) {
                try {
                    client.sendMessage(message, messageCallback);
                } catch (Exception e) {
                    logger.error("Unable to send message, detaching client.", e);
                    detachClient(client);
                }
            }
        }
    }

    /**
     * Used to avoid race condition when subscribed after the task completion as the completion completion event will
     * not be triggered again.
     */
    @Override //TODO do we still need this ?
    public void onBuildStatusUpdatesSubscribe(AttachedClient client, String messagesId) {

        Build build = buildProvider.getSpecific(messagesId);

        if (build != null) {
            // TODO speak to mkelnar and Jan if we can change from BuildResultRest to Build
            client.sendMessage(build, messageCallback);
        } else {
            String statusCode = Integer.toString(Response.Status.NO_CONTENT.getStatusCode());
            String errorMessage = "No build for id: " + messagesId;
            ErrorResponse error = new ErrorResponse(statusCode, errorMessage);
            client.sendMessage(error, messageCallback);
        }
    }

    public void cleanUp() {
        for (AttachedClient client : attachedClients) {
            if (!client.isEnabled()) {
                detachClient(client);
            }
        }
    }

    public void collectBuildPushResultEvent(@Observes BuildPushResult buildPushResult) {
        logger.trace("Observed new BuildPushResult event {}.", buildPushResult);
        sendMessage(new BuildPushResultNotification(buildPushResult));
        logger.trace("BuildPushResult event processed {}.", buildPushResult);
    }

    public void collectBuildStatusChangedEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        logger.trace("Observed new status changed event {}.", buildStatusChangedEvent);
        sendMessage(
                new BuildChangedNotification(
                        buildStatusChangedEvent.getOldStatus(),
                        buildStatusChangedEvent.getBuild()));
        logger.trace("Status changed event processed {}.", buildStatusChangedEvent);
    }

    public void collectBuildSetStatusChangedEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        logger.trace("Observed new set status changed event {}.", buildSetStatusChangedEvent);
        sendMessage(new GroupBuildChangedNotification(buildSetStatusChangedEvent.getGroupBuild()));
        logger.trace("Set status changed event processed {}.", buildSetStatusChangedEvent);
    }

    public void collectProductMilestoneCloseResultEvent(@Observes ProductMilestoneCloseResult milestoneCloseResult) {
        logger.trace("Observed new MilestoneCloseResult event {}.", milestoneCloseResult);
        sendMessage(new ProductMilestoneCloseResultNotification(milestoneCloseResult));
        logger.trace("ProductMilestoneCloseResult event processed {}.", milestoneCloseResult);
    }

}
