package org.jboss.pnc.common;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.ConfigurationJSONParser;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.common.util.StringUtils;

import javax.enterprise.context.ApplicationScoped;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-02.
 * @author Jakub Bartecek <jbartece@redhat.com>
 */
@ApplicationScoped
public class Configuration {

    private static final Logger log = Logger.getLogger(Configuration.class);
    
    private static final String CONFIG_SYSPROP = "pnc-config-file";
    
    private Map<Class<?>, AbstractModuleConfig> configCache = new HashMap<>();
    
    private ConfigurationJSONParser configurationJsonParser = new ConfigurationJSONParser();
    
    /**
     * Reads configuration for module
     * 
     * @param moduleClass Requested class with configuration
     * @return Loaded configuration
     * @throws ConfigurationParseException Thrown if configuration file couldn't be loaded or parsed
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractModuleConfig> T getModuleConfig(Class<T> moduleClass) throws ConfigurationParseException {
        if(configCache.containsKey(moduleClass))
            return (T) configCache.get(moduleClass);
        
        synchronized(this) {
            if(configCache.containsKey(moduleClass))
                return (T) configCache.get(moduleClass);
            
            try (InputStream configStream = this.getConfigStream()) {
                log.info("Loading configuration for class: " + moduleClass);
                String configString = StringUtils.replaceEnv(IoUtils.readStreamAsString(configStream));
                
                T config = configurationJsonParser.parseJSONConfig(configString, moduleClass);
                configCache.put(moduleClass, config);
                return config;
            } catch (IOException e) {
                throw new ConfigurationParseException("Config could not be parsed", e);
            }
        }
    }

    private InputStream getConfigStream() throws IOException {
        String configFileName = System.getProperty(CONFIG_SYSPROP);
        if (configFileName == null) 
            configFileName = "pnc-config.json";
        log.info("Loading configuration from file: " + configFileName);

        //Try to open stream from full path
        File file = new File(configFileName); 
        if (file.exists()) 
            return new FileInputStream(file);
            
        //Try to open stream using classloader
        final InputStream inStream = getClass().getClassLoader().getResourceAsStream(configFileName);
        if (inStream != null) 
            return inStream;

        throw new FileNotFoundException("Missing project config file.");
    }
}
