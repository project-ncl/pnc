package org.jboss.pnc.common.json;

import java.io.File;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author pslegr
 *
 */

public class ConfigurationJSONParser<T extends AbstractModuleConfig> {
    private static final Logger log = Logger.getLogger(ConfigurationJSONParser.class);

    public T parseJSONConfig(File configFile, Class<T> classType) throws ConfigurationParseException {
        try {
            // read from file, convert it to config module class
            ObjectMapper mapper = new ObjectMapper();
            log.info("Trying to read configuration from config file: " + configFile);
            ModuleConfigJson json_config = mapper.readValue(configFile, ModuleConfigJson.class);

            for (AbstractModuleConfig config : json_config.getConfigs()) {
              if(config.getClass().isAssignableFrom(classType)) {
                  return (T)config;
              }  
            }
            throw new ConfigurationParseException("Config could not be parsed");
        } catch (Exception e) {
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }
}
