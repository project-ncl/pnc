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
