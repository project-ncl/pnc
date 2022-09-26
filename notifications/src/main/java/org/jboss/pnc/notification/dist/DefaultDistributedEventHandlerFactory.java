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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.spi.notifications.Notifier;
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
    public DistributedEventHandler createDistributedEventHandler(SystemConfig config, Notifier notifier) {
        AbstractDistributedEventHandler handler;
        if ("kafka".equalsIgnoreCase(config.getDistributedEventType())) {
            handler = new KafkaDistributedEventHandler(config);
        } else if ("infinispan".equalsIgnoreCase(config.getDistributedEventType())) {
            handler = new InfinispanDistributedEventHandler(config);
        } else {
            handler = new LocalEventHandler();
        }
        handler.notifier = notifier;
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
