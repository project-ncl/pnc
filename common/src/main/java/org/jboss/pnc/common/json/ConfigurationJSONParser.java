package org.jboss.pnc.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Pavel Slegr
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
public class ConfigurationJSONParser {

    /**
     * Loads JSON configuration to the module configuration object
     *
     * @param configContent Configuration in JSON
     * @param classType Class for the requested configuration
     * @return Loaded configuration
     * @throws ConfigurationParseException Thrown if configuration string is malformed
     */
    public <T extends AbstractModuleConfig> T parseJSONConfig(
            String configContent, Class<T> classType) throws ConfigurationParseException {
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
            e.printStackTrace();
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }
}
