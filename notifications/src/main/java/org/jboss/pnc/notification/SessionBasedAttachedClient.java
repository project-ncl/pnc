/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification;

import java.util.Objects;

import javax.websocket.Session;

import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.MessageCallback;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SessionBasedAttachedClient implements AttachedClient {

    private final Session session;

    private final JacksonProvider mapperProvider = new JacksonProvider();

    public SessionBasedAttachedClient(Session session) {
        this.session = session;
    }

    @Override
    public boolean isEnabled() {
        return session.isOpen();
    }

    @Override
    public String getSessionId() {
        return session.getId();
    }

    @Override
    public void sendMessage(Object messageBody, MessageCallback callback) {

        String message;
        if (messageBody instanceof String) {
            message = (String) messageBody;
        } else {
            try {
                message = mapperProvider.getMapper().writeValueAsString(messageBody);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Could not convert object to JSON", e);
            }
        }

        session.getAsyncRemote().sendText(message, sendResult -> {
            if (!sendResult.isOK()) {
                callback.failed(SessionBasedAttachedClient.this, sendResult.getException());
            } else {
                callback.successful(SessionBasedAttachedClient.this);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SessionBasedAttachedClient that = (SessionBasedAttachedClient) o;

        return Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        return session != null ? session.hashCode() : 0;
    }
}
