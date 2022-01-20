/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.common;

import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.ConfigurationJSONParser;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleprovider.ConfigProvider;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@ApplicationScoped
public class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public static final String CONFIG_SYSPROP = "pnc-config-file";

    private final String CONFIG_STRING;

    private Map<Class<?>, AbstractModuleConfig> configCache = new HashMap<>();

    private GlobalModuleGroup globalConfig;

    private ConfigurationJSONParser configurationJsonParser = new ConfigurationJSONParser();

    /**
     *
     * @throws ConfigurationParseException Thrown if configuration file couldn't be loaded or parsed
     */
    public Configuration() throws ConfigurationParseException {

        try (InputStream configStream = this.getConfigStream()) {
            log.debug("Initializing configuration.");
            this.CONFIG_STRING = StringUtils.replaceEnv(IoUtils.readStreamAsString(configStream));
            log.debug("Config string with replaced environment variables: {}", this.CONFIG_STRING);
        } catch (IOException e) {
            throw new ConfigurationParseException("Config could not be parsed.", e);
        }
    }

    /**
     * Reads configuration for module
     *
     * @param provider configuration provider of given module config type
     * @param <T> module config
     * @return Loaded configuration
     * @throws ConfigurationParseException Thrown if configuration file couldn't be loaded or parsed
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractModuleConfig> T getModuleConfig(ConfigProvider<T> provider)
            throws ConfigurationParseException {
        Class<T> moduleClass = provider.getType();
        if (configCache.containsKey(moduleClass))
            return (T) configCache.get(moduleClass);

        synchronized (this) {
            if (configCache.containsKey(moduleClass)) {
                return (T) configCache.get(moduleClass);
            }

            T config = configurationJsonParser.parseJSONPNCConfig(CONFIG_STRING, provider);
            configCache.put(moduleClass, config);
            return config;
        }
    }

    public GlobalModuleGroup getGlobalConfig() throws ConfigurationParseException {
        if (globalConfig != null)
            return globalConfig;

        synchronized (this) {
            if (globalConfig != null) {
                return globalConfig;
            }

            globalConfig = configurationJsonParser.parseJSONGlobalConfig(CONFIG_STRING);
            return globalConfig;
        }
    }

    private InputStream getConfigStream() throws IOException {
        String configFileName = System.getProperty(CONFIG_SYSPROP);
        if (configFileName == null)
            configFileName = "pnc-config.json";
        log.info("Loading configuration from file: " + configFileName);

        // Try to open stream from full path
        File file = new File(configFileName);
        if (file.exists())
            return new FileInputStream(file);

        // Try to open stream using classloader
        final InputStream inStream = getClass().getClassLoader().getResourceAsStream(configFileName);
        if (inStream != null)
            return inStream;

        throw new FileNotFoundException("Missing project config file.");
    }
}
