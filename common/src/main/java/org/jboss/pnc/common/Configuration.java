package org.jboss.pnc.common;

import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.common.util.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-02.
 */
@ApplicationScoped
public class Configuration {

    private Properties properties;

    public Configuration() throws IOException {
        readConfigurationFile();
    }

    public Configuration(final Properties properties) {
        this.properties = properties;
    }

    //TODO return only part containing config for requested module
    public Properties getModuleConfig(final String moduleTag) {
        return properties;
    }


    private void readConfigurationFile() throws IOException {

        String configString = IoUtils.readFileOrResource("pnc-config-file", "pnc-config.ini", getClass().getClassLoader()); //TODO use json instead
        configString = StringUtils.replaceEnv(configString);

        properties = new Properties();
        properties.load(new StringReader(configString));

    }

}
