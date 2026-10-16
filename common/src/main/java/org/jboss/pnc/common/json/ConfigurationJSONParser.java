/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json;

import java.io.IOException;

import org.jboss.pnc.common.json.moduleprovider.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Pavel Slegr
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 * @author Honza Brazdil &lt;jbrazdil@redhat.com&gt;
 */
public class ConfigurationJSONParser {

    public final static Logger log = LoggerFactory.getLogger(ConfigurationJSONParser.class);

    /**
     * Loads JSON configuration to the module configuration object
     *
     * @param <T> module config
     * @param configContent Configuration in JSON
     * @param provider configuration provider of given module config type
     * @return Loaded configuration
     * @throws ConfigurationParseException Thrown if configuration string is malformed
     */
    public <T extends AbstractModuleConfig> T parseJSONPNCConfig(String configContent, ConfigProvider<T> provider)
            throws ConfigurationParseException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            provider.registerProvider(mapper);

            PNCModuleGroup pncGroup = getModuleGroup(mapper, configContent, PNCModuleGroup.class);

            for (AbstractModuleConfig config : pncGroup.getConfigs()) {
                if (config.getClass().isAssignableFrom(provider.getType())) {
                    return (T) config;
                }
            }
            throw new ConfigurationParseException(
                    "Did not find config for provider " + provider.getType().getSimpleName() + ".");
        } catch (IOException | RuntimeException e) {
            log.error(e.getMessage());
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }

    /**
     * Loads JSON configuration to the module configuration object
     *
     * @param <T> module config
     * @param configContent Configuration in JSON
     * @param provider configuration provider of given module config type
     * @return Loaded configuration
     * @throws ConfigurationParseException Thrown if configuration string is malformed
     */
    public <T extends AbstractModuleConfig> T parseJSONSlsaConfig(String configContent, ConfigProvider<T> provider)
            throws ConfigurationParseException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            provider.registerProvider(mapper);

            SlsaModuleGroup slsaGroup = getModuleGroup(mapper, configContent, SlsaModuleGroup.class);

            for (AbstractModuleConfig config : slsaGroup.getConfigs()) {
                if (config.getClass().isAssignableFrom(provider.getType())) {
                    return (T) config;
                }
            }
            throw new ConfigurationParseException(
                    "Did not find config for provider " + provider.getType().getSimpleName() + ".");
        } catch (IOException | RuntimeException e) {
            log.error(e.getMessage());
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }

    public GlobalModuleGroup parseJSONGlobalConfig(String configContent) throws ConfigurationParseException {
        try {
            return getModuleGroup(new ObjectMapper(), configContent, GlobalModuleGroup.class);
        } catch (IOException | RuntimeException e) {
            throw new ConfigurationParseException("Config could not be parsed", e);
        }
    }

    private <T> T getModuleGroup(ObjectMapper mapper, String configContent, Class<T> type)
            throws IOException, ConfigurationParseException {

        mapper.registerSubtypes(type);
        ModuleConfigJson jsonConfig = mapper.readValue(configContent, ModuleConfigJson.class);

        for (AbstractModuleGroup group : jsonConfig.getConfigs()) {
            if (type.isAssignableFrom(group.getClass())) {
                return type.cast(group);
            }
        }
        throw new ConfigurationParseException("Config group " + type.getSimpleName() + " could not be parsed");
    }

}
