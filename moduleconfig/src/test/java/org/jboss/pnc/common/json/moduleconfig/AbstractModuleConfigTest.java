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

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class AbstractModuleConfigTest {

    private static String backupConfigPath;

    @BeforeClass
    public static void setUpTestConfigPath() {
        backupConfigPath = System.getProperty("pnc-config-file");
        System.setProperty("pnc-config-file", "testConfig.json");
    }

    @AfterClass
    public static void restoreConfigPath() {
        if (backupConfigPath != null)
            System.setProperty("pnc-config-file", backupConfigPath);
        else
            System.getProperties().remove("pnc-config-file");
    }
}
