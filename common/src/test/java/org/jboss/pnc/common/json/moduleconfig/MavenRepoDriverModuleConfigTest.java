package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.junit.Assert;
import org.junit.Test;

public class MavenRepoDriverModuleConfigTest {

    @Test
    public void test() {
        try {
            Configuration<MavenRepoDriverModuleConfig> configuration = 
                    new Configuration<MavenRepoDriverModuleConfig>();
            MavenRepoDriverModuleConfig jen_config = 
                    configuration.getModuleConfig(MavenRepoDriverModuleConfig.class);
            Assert.assertNotNull(jen_config);
            System.out.println(jen_config);
        } catch (ConfigurationParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
