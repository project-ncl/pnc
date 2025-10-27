/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.notification.dist;

import org.jboss.pnc.notification.Local;
import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.MessageCallback;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

/**
 * Variant of a Notifier that distributes messages meant for WS subscribers through all instances so that all are
 * notified. The distribution work is done by DistributedEventHandler. This bean registers a Consumer for distributed
 * events and distributes messages sent to the local instance.
 *
 * Most of the work regarding registering WS subscribers is delegated to the LocalNotifier which actually sends the
 * messages and handles local client sessions.
 */
@Default // Default injected instance
@Distributed
@ApplicationScoped
public class DistributedNotifier implements Notifier, EventConsumer {
    private static final Logger log = LoggerFactory.getLogger(DistributedNotifier.class);

    private final Notifier delegate;

    private final DistributedEventHandler distributor;

    @Inject
    public DistributedNotifier(@Local Notifier delegate, DistributedEventHandler distributor) {
        this.delegate = delegate;
        this.distributor = distributor;
        registerConsumer();
    }

    /**
     * Registers consumer in distributor to send and consume messages coming from other instances.
     */
    public void registerConsumer() {
        distributor.registerSubscriber(this);
    }

    @Override
    public void sendMessage(Object message) {
        distributor.sendEvent(message);
    }

    // region Delegated methods to Local Notifier
    @Override
    public void consume(Object event) {
        delegate.sendMessage(event);
    }

    @Override
    public void attachClient(AttachedClient attachedClient) {
        delegate.attachClient(attachedClient);
    }

    @Override
    public void detachClient(AttachedClient attachedClient) {
        delegate.detachClient(attachedClient);
    }

    @Override
    public int getAttachedClientsCount() {
        return delegate.getAttachedClientsCount();
    }

    @Override
    public MessageCallback getCallback() {
        return delegate.getCallback();
    }
    // endregion
}
