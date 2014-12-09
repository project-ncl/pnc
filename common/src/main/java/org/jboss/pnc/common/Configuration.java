package org.jboss.pnc.common;

import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.common.util.StringUtils;

import javax.enterprise.context.ApplicationScoped;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;

import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.ConfigurationJSONParser;
import org.jboss.pnc.common.json.ConfigurationParseException;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-02.
 */
@ApplicationScoped
public class Configuration<T extends AbstractModuleConfig> {

    private Properties properties;

    public Configuration() throws IOException {
        readConfigurationFile();
    }

    public Configuration(final Properties properties) {
        this.properties = properties;
    }

//    //TODO return only part containing config for requested module
//    public Properties getModuleConfig(final String moduleTag) {
//        return properties;
//    }
    
    public T getModuleConfig(Class<T> moduleClass) throws ConfigurationParseException{
        try {
            T config =  new ConfigurationJSONParser<T>().parseJSONConfig(this.getConfigFile(),moduleClass);
            if(config != null) {
                return config;
            }
            throw new ConfigurationParseException("Config could not be parsed");
        } catch (IOException e) {
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }

    private void readConfigurationFile() throws IOException {

        String configString = IoUtils.readFileOrResource("pnc-config-file", "pnc-config.ini", getClass().getClassLoader()); //TODO use json instead
        configString = StringUtils.replaceEnv(configString);

        properties = new Properties();
        properties.load(new StringReader(configString));
    }

    private File getConfigFile() throws IOException {
        String configFileName = System.getProperty("pnc-config-file");

        File file = null;
        if (configFileName == null) {
            configFileName = "pnc-config.json";
        }

        file = new File(configFileName); //try full path

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
