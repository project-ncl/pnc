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
public class MavenRepoDriverModuleConfigTest extends AbstractModuleConfigTest {
    
    @Test
    public void loadMavenRepoDriverConfigTest() throws ConfigurationParseException {
            Configuration configuration = new Configuration();
            
            MavenRepoDriverModuleConfig mavenConfig = 
                    configuration.getModuleConfig(MavenRepoDriverModuleConfig.class);
            
            assertNotNull(mavenConfig);
            assertEquals("1.1.1.1", mavenConfig.getBaseUrl());
    }
    
}
