/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.json.moduleprovider.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Pavel Slegr
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
public class ConfigurationJSONParser {

    public final static Logger log = LoggerFactory.getLogger(ConfigurationJSONParser.class);

    /**
     * Loads JSON configuration to the module configuration object
     *
     * @param <T> module config
     * @param configContent Configuration in JSON
     * @param provider configuration provider of given module config type
     * @return Loaded configuration
     * @throws ConfigurationParseException Thrown if configuration string is malformed
     */
    public <T extends AbstractModuleConfig> T parseJSONConfig(
            String configContent, ConfigProvider<T> provider) throws ConfigurationParseException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            provider.registerProvider(mapper);
            
            ModuleConfigJson jsonConfig = mapper.readValue(configContent, ModuleConfigJson.class);

            for (AbstractModuleConfig config : jsonConfig.getConfigs()) {
                if (config.getClass().isAssignableFrom(provider.getType())) {
                    return (T) config;
                }
            }
            throw new ConfigurationParseException("Config could not be parsed");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }
}
