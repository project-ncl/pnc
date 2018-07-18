/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.junit.Test;

import java.util.List;

/**
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class IndyRepoDriverModuleConfigTest extends AbstractModuleConfigTest {

    @Test
    public void loadIndyRepoDriverConfigTest() throws ConfigurationParseException {
            Configuration configuration = new Configuration();

            IndyRepoDriverModuleConfig mavenConfig = configuration
                    .getModuleConfig(new PncConfigProvider<>(IndyRepoDriverModuleConfig.class));

            assertNotNull(mavenConfig);
            assertEquals("1.1.1.1", mavenConfig.getBaseUrl());
            assertEquals(100, mavenConfig.getDefaultRequestTimeout().intValue());
            assertEquals(true, mavenConfig.getBuildRepositoryAllowSnapshots().booleanValue());
            assertEquals(0, mavenConfig.getIgnoredPathSuffixes().getNpm().size());
            List<String> ignoredPathSuffixesMaven = mavenConfig.getIgnoredPathSuffixes().getMaven();
            assertNotNull(ignoredPathSuffixesMaven);
            assertEquals(2, ignoredPathSuffixesMaven.size());
            assertTrue(ignoredPathSuffixesMaven.contains("/maven-metadata.xml"));
            assertTrue(ignoredPathSuffixesMaven.contains(".sha1"));
    }

    @Test
    public void checkDefaultValuesLoadedProperly() throws ConfigurationParseException {
            String backupConfigPath = System.getProperty("pnc-config-file");
            System.setProperty("pnc-config-file", "testConfigWithoutDefaults.json");

            Configuration configuration = new Configuration();
            IndyRepoDriverModuleConfig mavenConfig = configuration
                    .getModuleConfig(new PncConfigProvider<>(IndyRepoDriverModuleConfig.class));

            if (backupConfigPath != null) {
                System.setProperty("pnc-config-file", backupConfigPath);
            } else {
                System.getProperties().remove("pnc-config-file");
            }

            assertNotNull(mavenConfig);
            assertEquals("1.1.1.1", mavenConfig.getBaseUrl());
            assertEquals(600, mavenConfig.getDefaultRequestTimeout().intValue());
            assertEquals(false, mavenConfig.getBuildRepositoryAllowSnapshots().booleanValue());
    }


}
