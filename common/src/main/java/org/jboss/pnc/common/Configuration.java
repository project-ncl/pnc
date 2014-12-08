package org.jboss.pnc.common;

import org.jboss.pnc.common.util.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        String configFileName = System.getProperty("pnc-config-file");

        File file = null;
        if (configFileName == null) {
            configFileName = "pnc-config.ini"; //TODO use json instead
        }

        file = new File(configFileName); //try full path

        String configString;
        if (file.exists()) {
            try {
                byte[] encoded;
                encoded = Files.readAllBytes(Paths.get(file.getPath()));
                configString = new String(encoded, Charset.defaultCharset());
            } catch (IOException e) {
                throw new IOException("Cannot load config file " + file.getAbsoluteFile() + ".", e);
            }

            configString = StringUtils.replaceEnv(configString);

            properties = new Properties();
            properties.load(new StringReader(configString));


        } else {
            throw new FileNotFoundException("Missing properties file " + file.getAbsolutePath() + ".");
        }
    }

}
