package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.junit.Assert;
import org.junit.Test;

public class MavenRepoDriverModuleConfigTest {

    @Test
    public void test() {
        try {
            Configuration configuration = new Configuration();
            MavenRepoDriverModuleConfig mavenConfig = 
                    configuration.getModuleConfig(MavenRepoDriverModuleConfig.class);
            Assert.assertNotNull(mavenConfig);
            System.out.println(mavenConfig);
        } catch (ConfigurationParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
