/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification.dist;

import java.util.ArrayList;
import java.util.List;

import org.jboss.pnc.rest.jackson.JacksonProvider;

import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class AbstractDistributedEventHandler implements DistributedEventHandler {
    private static final JacksonProvider mapperProvider = new JacksonProvider();

    protected List<EventConsumer> eventConsumers = new ArrayList<>();

    protected String toMessage(Object event) {
        try {
            return mapperProvider.getMapper().writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert object to JSON", e);
        }
    }

    public synchronized void registerSubscriber(EventConsumer eventConsumer) {
        eventConsumers.add(eventConsumer);
    }

    protected void sendMessage(Object json) {
        eventConsumers.forEach(consumer -> consumer.consume(json));
    }
}
