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
        assertEquals("127.0.0.1", globalConfig.getAproxUrl());
        assertEquals("1.2.3.4", globalConfig.getBpmUrl());
    }

}
