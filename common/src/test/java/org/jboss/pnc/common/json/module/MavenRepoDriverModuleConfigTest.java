package org.jboss.pnc.common.json.module;

import java.io.IOException;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.module.JenkinsBuildDriverModuleConfig;
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
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConfigurationParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
