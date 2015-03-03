package org.jboss.pnc.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.enterprise.context.Dependent;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.ConfigurationJSONParser;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.common.util.StringUtils;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-02.
 */
@Dependent
public class Configuration<T extends AbstractModuleConfig> {

    private static final Logger log = Logger.getLogger(Configuration.class);
    
    public static final String CONFIG_SYSPROP = "pnc-config-file";
    

    /**
     * Reads configuration for module
     * 
     * @param moduleClass Requested class with configuration
     * @return Loaded configuration
     * @throws ConfigurationParseException Thrown if configuration file couldn't be loaded or parsed
     */
    public T getModuleConfig(Class<T> moduleClass) throws ConfigurationParseException {
        try {
            File configFile = this.getConfigFile();
            String configString = StringUtils.replaceEnv(IoUtils.readFileAsString(configFile));
            log.info("Loading configuration for class: " + moduleClass 
                    + " from file: " + configFile.getAbsolutePath());

            T config = new ConfigurationJSONParser<T>().parseJSONConfig(configString, moduleClass);
            if (config != null) {
                return config;
            }
            throw new ConfigurationParseException("Config could not be parsed");
        } catch (IOException e) {
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }

    private File getConfigFile() throws IOException {
        String configFileName = System.getProperty("pnc-config-file");

        File file = null;
        if (configFileName == null) {
            configFileName = "pnc-config.json";
        }

        file = new File(configFileName); // try full path

        if (!file.exists()) {
            final URL url = getClass().getClassLoader().getResource(configFileName);
            if (url != null) {
                file = new File(url.getFile());
            }
        }

        if (!file.exists()) {
            throw new FileNotFoundException("Missing project config file.");
        }

        return file;

    }
}
