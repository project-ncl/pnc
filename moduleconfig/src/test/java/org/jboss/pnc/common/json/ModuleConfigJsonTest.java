/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.common.json;

import static org.junit.Assert.*;

import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.JenkinsBuildDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
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
        PncConfigProvider<AuthenticationModuleConfig> pncProvider = new PncConfigProvider<AuthenticationModuleConfig>(AuthenticationModuleConfig.class);
        pncProvider.registerProvider(mapper);
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        mapper.writeValue(byteOutStream, moduleConfigJson);

        assertEquals(loadConfig("testConfigNoSpaces.json"), byteOutStream.toString());
    }

    @Test
    public void deserializationTest() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        PncConfigProvider<AuthenticationModuleConfig> pncProvider = new PncConfigProvider<AuthenticationModuleConfig>(AuthenticationModuleConfig.class);
        pncProvider.registerProvider(mapper);
        ModuleConfigJson config = mapper.readValue(loadConfig("testConfigNoSpaces.json"), ModuleConfigJson.class);

        assertNotNull(config);
        assertEquals(2, config.getConfigs().size());
    }

    private String loadConfig(String name) throws IOException {
        return IoUtils.readStreamAsString(getClass().getClassLoader().getResourceAsStream(name));
    }

}
