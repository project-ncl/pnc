package org.jboss.pnc.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author pslegr
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */

public class ConfigurationJSONParser<T extends AbstractModuleConfig> {

    /**
     * Loads JSON configuration to the module configuration object
     * 
     * @param configContent Configuration in JSON
     * @param classType Class for the requested configuration
     * @return Loaded configuration
     * @throws ConfigurationParseException Thrown if configuration string is malformed
     */
    public T parseJSONConfig(String configContent, Class<T> classType) throws ConfigurationParseException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ModuleConfigJson jsonConfig = mapper.readValue(configContent, ModuleConfigJson.class);

            for (AbstractModuleConfig config : jsonConfig.getConfigs()) {
                if (config.getClass().isAssignableFrom(classType)) {
                    return (T) config;
                }
            }
            throw new ConfigurationParseException("Config could not be parsed");
        } catch (Exception e) {
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }
}
