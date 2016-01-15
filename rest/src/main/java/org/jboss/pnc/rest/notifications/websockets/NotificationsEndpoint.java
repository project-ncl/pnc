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

import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
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
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.lang.invoke.MethodHandles;

/**
 * Web Sockets notification implementation.
 */
@ApplicationScoped
@ServerEndpoint(NotificationsEndpoint.ENDPOINT_PATH)
public class NotificationsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String ENDPOINT_PATH = "/ws/build-records/notifications";

    @Inject
    private OutputConverter outputConverter;

    @Inject
    private Notifier notifier;

    @Inject
    private NotificationFactory notificationFactory;

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

    public void collectBuildStatusChangedEvent(@Observes BuildCoordinationStatusChangedEvent buildStatusChangedEvent) {
        notifier.sendMessage(notificationFactory.createNotification(buildStatusChangedEvent));
    }

    public void collectBuildSetStatusChangedEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        notifier.sendMessage(notificationFactory.createNotification(buildSetStatusChangedEvent));
    }
}
