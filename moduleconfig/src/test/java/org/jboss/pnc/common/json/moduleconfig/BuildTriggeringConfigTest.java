/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Set;

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
        assertFalse(buildTriggeringConfig.isProjectAllowed(BuildCategory.LIGHTWELL_UPSTREAM, "Bouncycastle"));

        // Categories without allowedProjects allow any project
        assertTrue(buildTriggeringConfig.isProjectAllowed(BuildCategory.STANDARD, "anything"));
        assertTrue(buildTriggeringConfig.getCategoryConfig(BuildCategory.STANDARD).getAllowedProjects().isEmpty());

        // LIGHTWELL_NOVEL allows temporary builds of the Bouncycastle project only (see testConfig.json)
        assertTrue(buildTriggeringConfig.isTemporaryBuildingAllowed(BuildCategory.LIGHTWELL_NOVEL));
        assertTrue(buildTriggeringConfig.isProjectAllowed(BuildCategory.LIGHTWELL_NOVEL, "Bouncycastle"));
        assertFalse(buildTriggeringConfig.isProjectAllowed(BuildCategory.LIGHTWELL_NOVEL, "bouncycastle"));
        assertFalse(buildTriggeringConfig.isProjectAllowed(BuildCategory.LIGHTWELL_NOVEL, "termd"));
        assertFalse(buildTriggeringConfig.isProjectAllowed(BuildCategory.LIGHTWELL_NOVEL, null));
    }

    @Test
    public void emptyAllowedProjectsAllowsAllProjects() {
        BuildTriggeringConfig config = new BuildTriggeringConfig();
        config.addCategoryConfig(
                BuildCategory.LIGHTWELL.name(),
                new BuildTriggeringConfig.CategoryConfig(false, true, Collections.emptySet()));

        assertTrue(config.isProjectAllowed(BuildCategory.LIGHTWELL, "Bouncycastle"));
        assertTrue(config.isProjectAllowed(BuildCategory.LIGHTWELL, "termd"));
    }

    @Test
    public void allowedProjectsAppliesToAnyCategory() {
        BuildTriggeringConfig config = new BuildTriggeringConfig();
        config.addCategoryConfig(
                BuildCategory.STANDARD.name(),
                new BuildTriggeringConfig.CategoryConfig(true, true, Set.of("Bouncycastle")));

        assertTrue(config.isProjectAllowed(BuildCategory.STANDARD, "Bouncycastle"));
        assertFalse(config.isProjectAllowed(BuildCategory.STANDARD, "termd"));
    }
}
