package org.jboss.pnc.rest.notifications.websockets;

import org.jboss.pnc.rest.notifications.Notifier;
import org.jboss.pnc.rest.notifications.OutputConverter;
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

    @OnOpen
    public void attach(Session attachedSession) {
        notifier.attachClient(new SessionBasedAttachedClient(attachedSession, outputConverter));
    }

    @OnClose
    public void detach(Session detachedSession) {
        notifier.detachClient(new SessionBasedAttachedClient(detachedSession, outputConverter));
    }

    public void collectEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        notifier.sendMessage(buildStatusChangedEvent);
    }
}
