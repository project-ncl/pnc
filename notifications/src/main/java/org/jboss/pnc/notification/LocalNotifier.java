/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.MessageCallback;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notification mechanism for Web Sockets. All implementation details should be placed in AttachedClient.
 */
@Local
@ApplicationScoped
public class LocalNotifier implements Notifier {

    private static final Logger logger = LoggerFactory.getLogger(LocalNotifier.class);

    private Set<AttachedClient> attachedClients = new CopyOnWriteArraySet<>();

    /**
     * FIXME can't use pnc-common's MDCExecutors because the project won't compile with newer versions of opentelemetry
     * libraries. EAP forces old version.
     */
    private final ScheduledExecutorService scheduler = MDCExecutors.newScheduledThreadPool(1);

    private final MessageCallback messageCallback = new MessageCallback() {

        @Override
        public void successful(AttachedClient attachedClient) {
            // logger.debug("Successfully sent message to client ", attachedClient);
        }

        @Override
        public void failed(AttachedClient attachedClient, Throwable throwable) {
            logger.error("Notification client threw an error, removing it", throwable);
            detachClient(attachedClient);
        }
    };

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::cleanUp, 1, 1, TimeUnit.HOURS);
    }

    @Override
    public void attachClient(AttachedClient attachedClient) {
        attachedClients.add(attachedClient);
    }

    @Override
    public void detachClient(AttachedClient attachedClient) {
        attachedClients.remove(attachedClient);
    }

    @Override
    public int getAttachedClientsCount() {
        return attachedClients.size();
    }

    @Override
    public MessageCallback getCallback() {
        return messageCallback;
    }

    @Override
    public void sendMessage(Object message) {
        for (AttachedClient client : attachedClients) {
            if (client.isEnabled()) {
                try {
                    client.sendMessage(message, messageCallback);
                } catch (Exception e) {
                    logger.error("Unable to send message, detaching client.", e);
                    detachClient(client);
                }
            }
        }
    }

    public void cleanUp() {
        for (AttachedClient client : attachedClients) {
            if (!client.isEnabled()) {
                detachClient(client);
            }
        }
    }

}
