/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.messaging;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class SysConfigProducer {

    @Produces
    public SystemConfig createSystemConfig() {
        // SystemConfig systemConfig = Mockito.mock(SystemConfig.class);
        // Mockito.when(systemConfig.getMessagingInternalQueueSize()).thenReturn(2);
        // return systemConfig;

        return new SystemConfig(
                null,
                null,
                null,
                "10",
                null,
                null,
                null,
                "",
                "2",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "false",
                null,
                "false",
                null,
                null,
                null,
                null,
                null);
    }
}
