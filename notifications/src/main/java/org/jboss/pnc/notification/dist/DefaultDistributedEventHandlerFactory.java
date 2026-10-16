/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification.dist;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create DistributedEventHandler e.g. Infinispan events, Kafka messages, ...
 */
@ApplicationScoped
public class DefaultDistributedEventHandlerFactory implements DistributedEventHandlerFactory {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDistributedEventHandlerFactory.class);

    @Produces
    @ApplicationScoped
    public DistributedEventHandler createDistributedEventHandler(SystemConfig config) {
        AbstractDistributedEventHandler handler;
        if ("kafka".equalsIgnoreCase(config.getDistributedEventType())) {
            handler = new KafkaDistributedEventHandler(config);
        } else if ("infinispan".equalsIgnoreCase(config.getDistributedEventType())) {
            handler = new InfinispanDistributedEventHandler(config);
        } else {
            handler = new LocalEventHandler();
        }
        handler.start();
        return handler;
    }

    public void closeDistributedEventHandler(@Disposes DistributedEventHandler handler) {
        try {
            handler.close();
        } catch (Exception e) {
            logger.warn("Error closing distributed event handler", e);
        }
    }
}
