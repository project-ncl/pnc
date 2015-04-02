package org.jboss.pnc.common.json;

import static org.junit.Assert.*;

import org.jboss.pnc.common.json.moduleconfig.JenkinsBuildDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.common.util.IoUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModuleConfigJsonTest {

    @Test
    public void serializationTest() throws JsonGenerationException, JsonMappingException, IOException {
        ModuleConfigJson moduleConfigJson = new ModuleConfigJson("pnc-config");
        JenkinsBuildDriverModuleConfig jenkinsBuildDriverModuleConfig =
                new JenkinsBuildDriverModuleConfig("user", "pass");
        MavenRepoDriverModuleConfig mavenRepoDriverModuleConfig =
                new MavenRepoDriverModuleConfig("http://something/base");
        moduleConfigJson.addConfig(jenkinsBuildDriverModuleConfig);
        moduleConfigJson.addConfig(mavenRepoDriverModuleConfig);

        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        mapper.writeValue(byteOutStream, moduleConfigJson);

        assertEquals(loadConfig("testConfigNoSpaces.json"), byteOutStream.toString());
    }

    @Test
    public void deserializationTest() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ModuleConfigJson config = mapper.readValue(loadConfig("testConfigNoSpaces.json"), ModuleConfigJson.class);

        assertNotNull(config);
        assertEquals(2, config.getConfigs().size());
    }

    private String loadConfig(String name) throws IOException {
        return IoUtils.readStreamAsString(getClass().getClassLoader().getResourceAsStream(name));
    }

}
