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
public class DockerEnvironmentDriverModuleConfigTest extends AbstractModuleConfigTest {

    @Test
    public void loadDockerEnvironmentDriverConfigTest() throws ConfigurationParseException {
            Configuration configuration = new Configuration();
            DockerEnvironmentDriverModuleConfig dockerConfig = 
                    configuration.getModuleConfig(DockerEnvironmentDriverModuleConfig.class);
            
            assertNotNull(dockerConfig);
            assertEquals("2.2.2.2", dockerConfig.getIp());
            assertEquals("contUser", dockerConfig.getInContainerUser());
            assertEquals("contPass", dockerConfig.getInContainerUserPassword());
            assertEquals("imageId", dockerConfig.getDockerImageId());
            assertEquals("3.3.3.3:44", dockerConfig.getFirewallAllowedDestinations());
    }

}
