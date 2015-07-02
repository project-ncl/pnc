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

import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.pnc.spi.notifications.OutputConverter;
import org.jboss.pnc.spi.notifications.model.NotificationFactory;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Web Sockets notification implementation.
 */
@ApplicationScoped
@ServerEndpoint(NotificationsEndpoint.ENDPOINT_PATH)
public class NotificationsEndpoint {

    public static final String ENDPOINT_PATH = "/ws/build-records/notifications";

    @Inject
    private OutputConverter outputConverter;

    @Inject
    private Notifier notifier;

    @Inject
    private BuildRecordProvider buildRecordProvider;

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

    public void collectEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        if(notificationFactory.isExternal(buildStatusChangedEvent.getNewStatus())) {
            notifier.sendMessage(notificationFactory.createNotification(buildStatusChangedEvent));
        }
    }
}
