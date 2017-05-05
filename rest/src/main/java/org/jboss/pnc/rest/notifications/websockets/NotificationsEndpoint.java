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
import org.jboss.pnc.rest.restmodel.bpm.BpmNotificationRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.utils.JsonOutputConverterMapper;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.MessageCallback;
import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.pnc.spi.notifications.OutputConverter;
import org.jboss.pnc.spi.notifications.model.NotificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

/**
 * Web Sockets notification implementation.
 */
@ApplicationScoped
@ServerEndpoint(NotificationsEndpoint.ENDPOINT_PATH)
public class NotificationsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** broadcasting endpoint, all events are sent to all subscribed users */
    public static final String ENDPOINT_PATH = "/ws/build-records/notifications"; //TODO rename endpoint

    @Inject
    private OutputConverter outputConverter;

    @Inject
    private Notifier notifier;

    @Inject
    private NotificationFactory notificationFactory;

    @Inject
    BpmManager bpmManager;

    private final MessageCallback messageCallback = new MessageCallback() {

        @Override
        public void successful(AttachedClient attachedClient) {
            // logger.debug("Successfully sent message to client ", attachedClient);
        }

        @Override
        public void failed(AttachedClient attachedClient, Throwable throwable) {
            logger.error("Notification client threw an error, removing it. ", throwable);
            //TODO detach(attachedClient);
        }
    };

    @OnOpen
    public void attach(Session attachedSession) {
        notifier.attachClient(new SessionBasedAttachedClient(attachedSession, outputConverter));
    }

    @OnClose
    public void detach(Session detachedSession) {
        notifier.detachClient(new SessionBasedAttachedClient(detachedSession, outputConverter));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        logger.warn("An error occurred in client: " + session + ". Removing it", t);
        notifier.detachClient(new SessionBasedAttachedClient(session, outputConverter));
    }

    /**
     * Expected message format:
     * {
     *   messageType: 'process-updates',
     *   message: {
     *     _typed_body_
     *   }
     * }
     *
     * Example:
     * {
     *   messageType: 'process-updates',
     *   message: {
     *     action: 'subscribe|unsubscribe',
     *     topic: 'component-build',
     *     id: 123
     *   }
     * }
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            TypedMessage typedMessage = JsonOutputConverterMapper.readValue(message, TypedMessage.class);
            MessageType messageType = typedMessage.getMessageType();
            if (messageType.equals(MessageType.PROCESS_UPDATES)) {
                ProgressUpdatesRequest progressUpdatesRequest = ((TypedMessage<ProgressUpdatesRequest>)typedMessage).get();;
                onProgressUpdateRequest(progressUpdatesRequest, session);
            } else {
                String statusCode = Integer.toString(Response.Status.NOT_ACCEPTABLE.getStatusCode());
                String errorMessage = "Invalid message-type: " + typedMessage.getMessageType() + ". Supported types are: " + MessageType.PROCESS_UPDATES;
                String error = JsonOutputConverterMapper.apply(new ErrorResponseRest(statusCode, errorMessage));
                logger.warn(errorMessage);
                session.getAsyncRemote().sendText(error);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onProgressUpdateRequest(ProgressUpdatesRequest progressUpdatesRequest, Session session) {
        Optional<AttachedClient> attachedClient = notifier.getAttachedClient(session.getId());
        AttachedClient client = attachedClient.get(); //TODO check, should be attached with onOpen

        String topic = progressUpdatesRequest.getTopic();
        String messagesId = progressUpdatesRequest.getId();

        if (progressUpdatesRequest.getAction().equals("subscribe")) {
            client.subscribe(topic, messagesId);

            Optional<BpmTask> maybeTask = bpmManager.getTaskById(Integer.parseInt(messagesId));
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
        } else if (progressUpdatesRequest.getAction().equals("unsubscribe")) {
            client.unsubscribe(topic, messagesId);
        } else {
            String statusCode = Integer.toString(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            String errorMessage = "Invalid action: " + progressUpdatesRequest.getAction() + ". Supported actions are: subscribe, unsubscribe.";
            String error = JsonOutputConverterMapper.apply(new ErrorResponseRest(statusCode, errorMessage));
            logger.warn(errorMessage);
            session.getAsyncRemote().sendText(error);
        }
    }

    public void collectBuildStatusChangedEvent(@Observes BuildCoordinationStatusChangedEvent buildStatusChangedEvent) {
        logger.debug("Observed new status changed event {}.", buildStatusChangedEvent);
        if (buildStatusChangedEvent.getNewStatus().equals(BuildCoordinationStatus.BUILDING)) {
            Integer buildTaskId = buildStatusChangedEvent.getBuildTaskId();
            Optional<BpmTask> maybeTask = bpmManager.getTaskById(buildTaskId);
            BpmTask bpmTask = maybeTask.get();//TODO check

            bpmTask.addListener(BpmEventType.PROCESS_PROGRESS_UPDATE, (processProgressUpdate) -> {
                String messagesId = Integer.toString(buildTaskId);
                notifier.sendToSubscribers(processProgressUpdate, "component-build", messagesId);
            });
        }
        notifier.sendMessage(notificationFactory.createNotification(buildStatusChangedEvent));
        logger.debug("Status changed event processed {}.", buildStatusChangedEvent);
    }

    public void collectBuildSetStatusChangedEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        notifier.sendMessage(notificationFactory.createNotification(buildSetStatusChangedEvent));
    }
}
