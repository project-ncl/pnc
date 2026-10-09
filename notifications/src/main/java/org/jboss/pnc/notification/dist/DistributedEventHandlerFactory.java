/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification.dist;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

/**
 * Create DistributedEventHandler e.g. Infinispan events, Kafka messages, ...
 */
public interface DistributedEventHandlerFactory {
    DistributedEventHandler createDistributedEventHandler(SystemConfig config);
}
