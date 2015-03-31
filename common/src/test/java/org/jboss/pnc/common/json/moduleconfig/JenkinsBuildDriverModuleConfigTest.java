package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.junit.Assert;
import org.junit.Test;

public class JenkinsBuildDriverModuleConfigTest {

    @Test
    public void test() {
        try {
            Configuration configuration = new Configuration();
            JenkinsBuildDriverModuleConfig jenConfig = 
                    configuration.getModuleConfig(JenkinsBuildDriverModuleConfig.class);
            Assert.assertNotNull(jenConfig);
            System.out.println(jenConfig);
        } catch (ConfigurationParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
