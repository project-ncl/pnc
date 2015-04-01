package org.jboss.pnc.common.json.moduleconfig;

import static org.junit.Assert.*;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.junit.Test;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class JenkinsBuildDriverModuleConfigTest extends AbstractModuleConfigTest {

    @Test
    public void loadJenkinsBuildDriverConfigTest() throws ConfigurationParseException {
            Configuration configuration = new Configuration();
            JenkinsBuildDriverModuleConfig jenkinsConfig = 
                    configuration.getModuleConfig(JenkinsBuildDriverModuleConfig.class);
            
            assertNotNull(jenkinsConfig);
            assertEquals("user", jenkinsConfig.getUsername());
            assertEquals("pass", jenkinsConfig.getPassword());
    }

}
