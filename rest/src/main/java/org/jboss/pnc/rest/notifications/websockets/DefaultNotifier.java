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
package org.jboss.pnc.rest.notifications.websockets;

import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.coordinator.builder.bpm.BpmBuildScheduler;
import org.jboss.pnc.rest.restmodel.bpm.BpmNotificationRest;
import org.jboss.pnc.rest.restmodel.bpm.ProcessProgressUpdate;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.utils.JsonOutputConverterMapper;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.MessageCallback;
import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.pnc.spi.notifications.model.NotificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Notification mechanism for Web Sockets. All implementation details should be placed in AttachedClient.
 */
@ApplicationScoped
public class DefaultNotifier implements Notifier {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Set<AttachedClient> attachedClients = Collections.synchronizedSet(new HashSet<>());

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
    private NotificationFactory notificationFactory;

    Optional<BpmManager> bpmManager;

    @Inject
    Instance<BpmManager> bpmManagerInstance;

    @Inject
    Configuration configuration;

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::cleanUp, 1, 1, TimeUnit.HOURS);

        String buildSchedulerId;
        try {
            SystemConfig systemConfig = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class));
            buildSchedulerId = systemConfig.getBuildSchedulerId();
        } catch (ConfigurationParseException e) {
            logger.warn("Cannot read system config buildSchedulerId");
            buildSchedulerId = "does-not-match";
        }

        if (BpmBuildScheduler.schedulerId.equals(buildSchedulerId) &&
                !bpmManagerInstance.isUnsatisfied() && !bpmManagerInstance.isAmbiguous()) {
            bpmManager = Optional.of(bpmManagerInstance.get());
            logger.debug("Subscribing listener for new tasks.");
            bpmManagerInstance.get().subscribeToNewTasks(task -> onNewTaskCreated(task));
        } else {
            bpmManager = Optional.empty();
        }
    }

    @Override
    public void attachClient(AttachedClient attachedClient) {
        synchronized (attachedClients) {
            attachedClients.add(attachedClient);
        }
    }

    @Override
    public void detachClient(AttachedClient attachedClient) {
        try {
            synchronized (attachedClients) {
                attachedClients.remove(attachedClient);
            }
        } catch (ConcurrentModificationException cme) {
            logger.error("Error while removing attached client: ", cme);
        }
    }

    @Override
    public int getAttachedClientsCount() {
        return attachedClients.size();
    }

    @Override
    public Optional<AttachedClient> getAttachedClient(String sessionId) {
        return attachedClients.stream()
                .filter(client -> client.getSessionId().equals(sessionId))
                .findAny();
    }

    @Override
    public MessageCallback getCallback() {
        return messageCallback;
    }

    @Override
    public void sendMessage(Object message) {
        try {
            for (Iterator<AttachedClient> attachedClientIterator = attachedClients.iterator(); attachedClientIterator
                    .hasNext();) {
                AttachedClient client = attachedClientIterator.next();
                if (client.isEnabled()) {
                    try {
                        client.sendMessage(message, messageCallback);
                    } catch (Exception e) {
                        logger.error("Unable to send message, detaching client.", e);
                        detachClient(client);
                    }
                }
            }
        } catch (ConcurrentModificationException cme) {
            logger.warn("Error while removing attached client: ", cme);
        }
    }

    @Override
    public void sendToSubscribers(Object message, String topic, String qualifier) {
        try {
            for (Iterator<AttachedClient> attachedClientIterator = attachedClients.iterator(); attachedClientIterator
                    .hasNext();) {
                AttachedClient client = attachedClientIterator.next();
                if (client.isEnabled()) {
                    if (client.isSubscribed(topic, qualifier))
                        try {
                            client.sendMessage(message, messageCallback);
                        } catch (Exception e) {
                            logger.error("Unable to send message, detaching client.", e);
                            detachClient(client);
                        }
                }
            }
        } catch (ConcurrentModificationException cme) {
            logger.warn("Error while removing attached client: ", cme);
        }
    }

    @Override
    public void onClientSubscribe(AttachedClient client, String messagesId) {
        if (bpmManager.isPresent()) {
            Optional<BpmTask> maybeTask = BpmBuildTask.getBpmTaskByBuildTaskId(bpmManager.get(), Integer.valueOf(messagesId));
            if (maybeTask.isPresent()) {
                BpmTask bpmTask = maybeTask.get();
                Optional<BpmNotificationRest> maybeLastEvent = bpmTask.getEvents().stream().reduce((first, second) -> second);
                if (maybeLastEvent.isPresent()) {
                    BpmNotificationRest lastBpmNotificationRest = maybeLastEvent.get();
                    client.sendMessage(lastBpmNotificationRest, messageCallback);
                } else {
                    String statusCode = Integer.toString(Response.Status.NO_CONTENT.getStatusCode());
                    String errorMessage = "No events for id: " + messagesId;
                    String error = JsonOutputConverterMapper.apply(new ErrorResponseRest(statusCode, errorMessage));
                    client.sendMessage(JsonOutputConverterMapper.apply(error), messageCallback);
                }
            } else {
                String statusCode = Integer.toString(Response.Status.NO_CONTENT.getStatusCode());
                String errorMessage = "No process for id: " + messagesId;
                String error = JsonOutputConverterMapper.apply(new ErrorResponseRest(statusCode, errorMessage));
                client.sendMessage(JsonOutputConverterMapper.apply(error), messageCallback);
            }
        }
    }

    public void cleanUp() {
        synchronized (attachedClients) {
            for (Iterator<AttachedClient> attachedClientIterator = attachedClients.iterator(); attachedClientIterator
                    .hasNext();) {
                AttachedClient client = attachedClientIterator.next();
                if (!client.isEnabled()) {
                    attachedClientIterator.remove();
                }
            }
        }
    }

    private void onNewTaskCreated(BpmTask bpmTask) {
        // subscribe WS clients to BpmBuildTask notifications
        if (bpmTask instanceof BpmBuildTask) {
            logger.debug("Adding listener for PROCESS_PROGRESS_UPDATEs to bpmTask {}.", bpmTask.getTaskId());
            BpmBuildTask bpmBuildTask = (BpmBuildTask)bpmTask;
            bpmTask.<ProcessProgressUpdate>addListener(BpmEventType.PROCESS_PROGRESS_UPDATE,
                    (processProgressUpdate) -> {
                        String buildTaskId = Integer.toString(bpmBuildTask.getBuildTask().getId());
                        notifySubscribers(buildTaskId, processProgressUpdate);
                    });
        }
    }

    private void notifySubscribers(String buildTaskId, ProcessProgressUpdate processProgressUpdate) {
        logger.trace("Sending update for buildTaskId: {}. processProgressUpdate: {}.", buildTaskId, processProgressUpdate.toString());
        sendToSubscribers(processProgressUpdate, "component-build", buildTaskId);
    }


    public void collectBuildStatusChangedEvent(@Observes BuildCoordinationStatusChangedEvent buildStatusChangedEvent) {
        logger.trace("Observed new status changed event {}.", buildStatusChangedEvent);
        sendMessage(notificationFactory.createNotification(buildStatusChangedEvent));
        logger.trace("Status changed event processed {}.", buildStatusChangedEvent);
    }

    public void collectBuildSetStatusChangedEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        logger.trace("Observed new set status changed event {}.", buildSetStatusChangedEvent);
        sendMessage(notificationFactory.createNotification(buildSetStatusChangedEvent));
        logger.trace("Set status changed event processed {}.", buildSetStatusChangedEvent);
    }
}
