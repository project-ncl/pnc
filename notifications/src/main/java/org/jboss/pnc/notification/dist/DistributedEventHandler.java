/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification.dist;

/**
 * Distributes events across cluster. To consume the distributed messages coming from other instances, you must
 * subscribe by using registerSubscriber() method.
 */
public interface DistributedEventHandler extends AutoCloseable {
    void start();

    void registerSubscriber(EventConsumer consumer);

    void sendEvent(Object event);
}
