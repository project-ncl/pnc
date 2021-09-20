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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.MessageCallback;
import org.jboss.pnc.spi.notifications.Notifier;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

public class SessionBasedAttachedClient implements AttachedClient {

    private final Session session;
    private Notifier notifier;

    private final JacksonProvider mapperProvider = new JacksonProvider();
    private List<Subscription> subscriptions = new ArrayList();

    public SessionBasedAttachedClient(Session session, Notifier notifier) {
        this.session = session;
        this.notifier = notifier;
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
        try {
            message = mapperProvider.getMapper().writeValueAsString(messageBody);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert object to JSON", e);
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
    public void subscribe(String topic, String messagesId) {
        subscriptions.add(new Subscription(topic, messagesId));
        if (topic.equals(Notifier.Topic.COMPONENT_BUILD.getId())) {
            notifier.onBuildStatusUpdatesSubscribe(this, messagesId);
        }
    }

    @Override
    public void unsubscribe(String topic, String messagesId) {
        subscriptions.remove(new Subscription(topic, messagesId));
    }

    /**
     * @return True if topic match and optional qualifier on subscription match or is empty
     */
    @Override
    public boolean isSubscribed(String topic, String qualifier) {
        for (Subscription subscription : subscriptions) {
            if (topic.equals(subscription.getTopic())
                    && (subscription.getQualifier().equals("") || subscription.getQualifier().equals(qualifier))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SessionBasedAttachedClient that = (SessionBasedAttachedClient) o;

        if (session != null ? !session.equals(that.session) : that.session != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = session != null ? session.hashCode() : 0;
        return result;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    private class Subscription {
        String topic;
        String qualifier;
    }
}
