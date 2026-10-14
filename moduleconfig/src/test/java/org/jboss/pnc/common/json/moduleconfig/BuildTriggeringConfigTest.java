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
package org.jboss.pnc.common.json.moduleconfig;

import static org.junit.Assert.*;

import org.jboss.pnc.api.enums.BuildCategory;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.junit.Test;

public class BuildTriggeringConfigTest extends AbstractModuleConfigTest {

    @Test
    public void loadBuildTriggeringConfigTest() throws ConfigurationParseException {
        Configuration configuration = new Configuration();
        BuildTriggeringConfig buildTriggeringConfig = configuration
                .getModuleConfig(new PncConfigProvider<>(BuildTriggeringConfig.class));

        assertNotNull(buildTriggeringConfig);

        // STANDARD allows both persistent and temporary builds (see testConfig.json)
        assertTrue(buildTriggeringConfig.isPersistentBuildingAllowed(BuildCategory.STANDARD));
        assertTrue(buildTriggeringConfig.isTemporaryBuildingAllowed(BuildCategory.STANDARD));

        // SERVICE allows only temporary builds (see testConfig.json)
        assertFalse(buildTriggeringConfig.isPersistentBuildingAllowed(BuildCategory.SERVICE));
        assertTrue(buildTriggeringConfig.isTemporaryBuildingAllowed(BuildCategory.SERVICE));

        // LIGHTWELL allows neither (see testConfig.json)
        assertFalse(buildTriggeringConfig.isPersistentBuildingAllowed(BuildCategory.LIGHTWELL));
        assertFalse(buildTriggeringConfig.isTemporaryBuildingAllowed(BuildCategory.LIGHTWELL));

        // Unconfigured categories are forbidden by default
        assertNull(buildTriggeringConfig.getCategoryConfig(BuildCategory.LIGHTWELL_UPSTREAM));
        assertFalse(buildTriggeringConfig.isPersistentBuildingAllowed(BuildCategory.LIGHTWELL_UPSTREAM));
        assertFalse(buildTriggeringConfig.isTemporaryBuildingAllowed(BuildCategory.LIGHTWELL_UPSTREAM));
    }
}
