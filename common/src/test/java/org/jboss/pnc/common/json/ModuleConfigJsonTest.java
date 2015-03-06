package org.jboss.pnc.common.json;

import java.io.IOException;

import org.jboss.pnc.common.json.moduleconfig.JenkinsBuildDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModuleConfigJsonTest {

    @Test
    public void serialize() {
        try {
            ModuleConfigJson moduleConfigJson =  new ModuleConfigJson("pnc-config");
            JenkinsBuildDriverModuleConfig jenkinsBuildDriverModuleConfig = 
                    new JenkinsBuildDriverModuleConfig("pavel", "test");
            MavenRepoDriverModuleConfig mavenRepoDriverModuleConfig = 
                    new MavenRepoDriverModuleConfig("http://something/base");
            moduleConfigJson.addConfig(jenkinsBuildDriverModuleConfig);
            moduleConfigJson.addConfig(mavenRepoDriverModuleConfig);
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(System.out, moduleConfigJson);
            
            //Assert.assertNotNull(jen_config);
            //System.out.println(jen_config);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void deserialize() {
        try {
            String json = "{\"@class\":\"ModuleConfigJson\",\"name\":\"pnc-config\",\"configs\""
                    + ":[{\"@module-config\":\"jenkins-build-driver\""
                    + ",\"username\":\"pavel\",\"password\":\"test\"},{\"@module-config\":\"maven-repo-driver\""
                    + ",\"baseUrl\":\"http://something/base\"}]}";    

            ObjectMapper mapper = new ObjectMapper();
            ModuleConfigJson config = mapper.readValue(json, ModuleConfigJson.class);
            
            Assert.assertNotNull(config);
            System.out.println(">>> deserialize");
            System.out.println(config);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    

    
}
