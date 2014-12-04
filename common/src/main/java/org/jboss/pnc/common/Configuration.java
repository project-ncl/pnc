package org.jboss.pnc.common;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-02.
 */
@ApplicationScoped
public class Configuration {

    private Properties properties;

    public Configuration() throws IOException {
        //FIXME: FileNotFoundException thrown, see BuildTest#shouldTriggerBuildAndFinishWithoutProblems
        //readConfigurationFile();
    }

    public Configuration(final Properties properties) {
        this.properties = properties;
    }

    //TODO return only part containing config for requested module
    public Properties getModuleConfig(final String moduleTag) {
        return properties;
    }


    private void readConfigurationFile() throws IOException {
        String configFileName = System.getProperty("pnc-config-file");

        File file = null;
        if (configFileName == null) {
            configFileName = "pnc-config.ini"; //TODO use json instead
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

        properties = new Properties();
        properties.load(new FileReader(file));

    }

}
