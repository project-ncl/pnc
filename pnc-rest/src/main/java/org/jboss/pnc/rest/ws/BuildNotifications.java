package org.jboss.pnc.rest.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is dummy implementation of server side web sockets endpoint.
 */
@Singleton
@ServerEndpoint("/ws/record/notifications")
public class BuildNotifications {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Set<Session> attachedSessions = Collections.synchronizedSet(new HashSet<>());

    @OnMessage
    public void sayHello(String message, Session session) {
        logger.info("Got {} from WS", message);
        session.getAsyncRemote().sendText("Hi!");
    }

    @OnOpen
    public void attach(Session attachedSession) {
        this.attachedSessions.add(attachedSession);
        logger.info("New client attached {}", attachedSession);
    }

    @OnClose
    public void detach(Session detachedSession) {
        this.attachedSessions.remove(detachedSession);
    }

    @Schedule(hour = "*", minute = "*", second = "0")
    public void sayHelloToEveryone() throws IOException {
        for (Session s : attachedSessions) {
            if(s.isOpen()) {
                logger.info("Sending hi from the timer to {}", s);
                s.getBasicRemote().sendText("Hi!");
            }
        }
    }

}
