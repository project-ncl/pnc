/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import static org.junit.Assert.*;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.junit.Test;

/**
 * @author Honza Brazdil &lt;jbrazdil@redhat.com&gt;
 *
 */
public class GlobalConfigTest extends AbstractModuleConfigTest {

    @Test
    public void loadGlobalConfigTest() throws ConfigurationParseException {
        Configuration configuration = new Configuration();
        GlobalModuleGroup globalConfig = configuration.getGlobalConfig();

        assertNotNull(globalConfig);
        assertEquals("http://127.0.0.1:8001", globalConfig.getBpmUrl());
        assertEquals("http://127.0.0.1:8002", globalConfig.getCartographerUrl());
        assertEquals("http://127.0.0.1:8003", globalConfig.getDaUrl());
        assertEquals("http://127.0.0.1:8004", globalConfig.getIndyUrl());
        assertEquals("http://127.0.0.1:8005", globalConfig.getPncUrl());
        assertEquals("http://127.0.0.1:8006", globalConfig.getRepourUrl());
        assertEquals("http://127.0.0.1:8007", globalConfig.getDelAnalUrl());

        assertEquals("http://1.2.3.4", globalConfig.getExternalBifrostUrl());
        assertEquals("http://1.2.3.5", globalConfig.getExternalDaUrl());
        assertEquals("http://1.2.3.6", globalConfig.getExternalCausewayUrl());
        assertEquals("http://1.2.3.7", globalConfig.getExternalIndyUrl());
        assertEquals("http://1.2.3.8", globalConfig.getExternalKafkaStoreUrl());
        assertEquals("http://1.2.3.9", globalConfig.getExternalPncUrl());
        assertEquals("http://1.2.3.10", globalConfig.getExternalRepourUrl());
        assertEquals("http://1.2.3.11", globalConfig.getExternalUiLoggerUrl());
    }

}
