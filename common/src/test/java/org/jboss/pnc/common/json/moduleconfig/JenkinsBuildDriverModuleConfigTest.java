package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.junit.Assert;
import org.junit.Test;

public class JenkinsBuildDriverModuleConfigTest {

    @Test
    public void test() {
        try {
            Configuration<JenkinsBuildDriverModuleConfig> configuration = 
                    new Configuration<JenkinsBuildDriverModuleConfig>();
            JenkinsBuildDriverModuleConfig jen_config = 
                    configuration.getModuleConfig(JenkinsBuildDriverModuleConfig.class);
            Assert.assertNotNull(jen_config);
            System.out.println(jen_config);
        } catch (ConfigurationParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
