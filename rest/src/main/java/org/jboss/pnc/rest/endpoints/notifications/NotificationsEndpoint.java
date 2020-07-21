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
package org.jboss.pnc.rest.endpoints.notifications;

import org.jboss.pnc.notification.Action;
import org.jboss.pnc.notification.MessageType;
import org.jboss.pnc.notification.ProgressUpdatesRequest;
import org.jboss.pnc.notification.RequestParser;
import org.jboss.pnc.notification.SessionBasedAttachedClient;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.MessageCallback;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.jboss.pnc.dto.response.ErrorResponse;

/**
 * Web Sockets notification implementation.
 */
@ServerEndpoint(NotificationsEndpoint.ENDPOINT_PATH)
public class NotificationsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** broadcasting endpoint, all events are sent to all subscribed users */
    public static final String ENDPOINT_PATH = "/notifications";

    @Inject
    Notifier notifier;

    private final MessageCallback messageCallback = new MessageCallback() {

        @Override
        public void successful(AttachedClient attachedClient) {
            // logger.debug("Successfully sent message to client ", attachedClient);
        }

        @Override
        public void failed(AttachedClient attachedClient, Throwable throwable) {
            logger.error("Notification client threw an error, removing it. ", throwable);
            notifier.detachClient(attachedClient);
        }
    };

    @OnOpen
    public void attach(Session attachedSession) {
        logger.debug("Opened new session id: {}, uri: {}.", attachedSession.getId(), attachedSession.getRequestURI());
        notifier.attachClient(new SessionBasedAttachedClient(attachedSession, notifier));
    }

    @OnClose
    public void detach(Session detachedSession) {
        notifier.detachClient(new SessionBasedAttachedClient(detachedSession, notifier));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        logger.warn("An error occurred in client: " + session + ". Removing it", t);
        notifier.detachClient(new SessionBasedAttachedClient(session, notifier));
    }

    /**
     * Expected message format: { messageType: 'process-updates', message: { _typed_body_ } }
     *
     * Example: { messageType: 'process-updates', message: { action: 'subscribe|unsubscribe', topic: 'component-build',
     * id: 123 } }
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {

        RequestParser parser = new RequestParser();
        try {
            if (!parser.parseRequest(message)) {
                respondWithErrorMessage(parser.getErrorMessage(), parser.getFailedStatus(), session);
                return;
            }
        } catch (IOException e) {
            respondWithErrorMessage(
                    parser.getErrorMessage() + " " + e.getMessage(),
                    parser.getFailedStatus(),
                    session,
                    e);
            return;
        }

        MessageType messageType = parser.getMessageType();
        if (MessageType.PROCESS_UPDATES.equals(messageType)) {
            ProgressUpdatesRequest progressUpdatesRequest = parser.<ProgressUpdatesRequest> getData();
            onProgressUpdateRequest(progressUpdatesRequest, session);
        }
    }

    private void respondWithErrorMessage(String errorMessage, Response.Status status, Session session) {
        respondWithErrorMessage(errorMessage, status, session, null);
    }

    private void respondWithErrorMessage(String errorMessage, Response.Status status, Session session, Exception e) {
        String statusCode = Integer.toString(status.getStatusCode());
        String error = JsonOutputConverterMapper.apply(new ErrorResponse(statusCode, errorMessage));
        if (e != null) {
            logger.warn(errorMessage, e);
        } else {
            logger.warn(errorMessage);
        }
        session.getAsyncRemote().sendText(error);
    }

    private void onProgressUpdateRequest(ProgressUpdatesRequest progressUpdatesRequest, Session session) {
        Optional<AttachedClient> attachedClient = notifier.getAttachedClient(session.getId());
        AttachedClient client;
        if (attachedClient.isPresent()) {
            client = attachedClient.get();
        } else {
            logger.error("Something went wrong, the client should be attached.");
            return;
        }

        String topic = progressUpdatesRequest.getTopic();
        String messagesId = progressUpdatesRequest.getId();

        if (Action.SUBSCRIBE.equals(progressUpdatesRequest.getAction())) {
            logger.debug("Subscribing new updates listener for topic: {} and messageId: {}.", topic, messagesId);
            client.subscribe(topic, messagesId);
        } else if (Action.UNSUBSCRIBE.equals(progressUpdatesRequest.getAction())) {
            client.unsubscribe(topic, messagesId);
        } else {
            String statusCode = Integer.toString(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            String errorMessage = "Invalid action: " + progressUpdatesRequest.getAction()
                    + ". Supported actions are: {}." + Action.values();
            String error = JsonOutputConverterMapper.apply(new ErrorResponse(statusCode, errorMessage));
            logger.warn(errorMessage);
            session.getAsyncRemote().sendText(error);
        }
    }

}
