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
package org.jboss.pnc.rest.executor.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.lang.invoke.MethodHandles;

/**
 * Web Sockets notification implementation.
 */
@ServerEndpoint(NotificationsEndpoint.ENDPOINT_PATH)
public class NotificationsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String ENDPOINT_PATH = "/ws/executor/notifications";

    @Inject
    NotificationSender notificationSender;

    @OnOpen
    public void onOpen(Session session) {
        notificationSender.getSessions().put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        notificationSender.getSessions().remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        logger.warn("An error occurred in client: " + session + ". Removing it. ", t);
        notificationSender.getSessions().remove(session.getId());
    }

}
