package org.jboss.pnc.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.authentication.AuthenticationProvider;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.ConfigurationJSONParser;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.common.util.StringUtils;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-02.
 * @author Jakub Bartecek <jbartece@redhat.com>
 */
@Dependent
public class Configuration<T extends AbstractModuleConfig> {

    private static final Logger log = Logger.getLogger(Configuration.class);
    
    public static final String CONFIG_SYSPROP = "pnc-config-file";
    
    @Inject
    private AuthenticationProvider authenticationProvider;
    
    /**
     * Reads configuration for module
     * 
     * @param moduleClass Requested class with configuration
     * @return Loaded configuration
     * @throws ConfigurationParseException Thrown if configuration file couldn't be loaded or parsed
     */
    public T getModuleConfig(Class<T> moduleClass) throws ConfigurationParseException {
        try (InputStream configStream = this.getConfigStream()) {
            log.info("Loading configuration for class: " + moduleClass);
            String configString = StringUtils.replaceEnv(IoUtils.readStreamAsString(configStream));
            
            return new ConfigurationJSONParser<T>().parseJSONConfig(configString, moduleClass);
        } catch (IOException e) {
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }

    private InputStream getConfigStream() throws IOException {
        String configFileName = System.getProperty("pnc-config-file");
        if (configFileName == null) 
            configFileName = "pnc-config.json";
        log.info("Loading configuration from file: " + configFileName);
        if(authenticationProvider != null) {
            String loggedInUser = authenticationProvider.getLoggedInUser();
            log.info("Authentication Provider: loggedInUser: " + loggedInUser);
        }
        

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
