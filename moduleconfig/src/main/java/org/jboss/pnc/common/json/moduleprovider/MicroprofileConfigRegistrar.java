/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleprovider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SchedulerConfig;
import org.jboss.pnc.common.json.moduleconfig.microprofile.SchedulerMicroprofileConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * This class registers Microprofile ConfigSources programmatically instead of having to specify each as META-INF
 * service.
 */
@Slf4j
public class MicroprofileConfigRegistrar implements ConfigSourceProvider {

    private static Configuration configuration;

    public MicroprofileConfigRegistrar() {
        try {
            synchronized (Object.class) {
                if (configuration == null) {
                    configuration = new Configuration();
                }
            }
        } catch (ConfigurationParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader classLoader) {
        List<ConfigSource> microprofileConfig = new ArrayList<>();

        try {
            microprofileConfig.add(new SchedulerMicroprofileConfig(getModuleConfig(SchedulerConfig.class)));
        } catch (ConfigurationParseException ignored) {
            log.error("SchedulerConfig haven't been found and microprofile wrapper may be missing.");
        }

        return microprofileConfig;
    }

    private <T extends AbstractModuleConfig> T getModuleConfig(Class<T> configClass)
            throws ConfigurationParseException {
        return configuration.getModuleConfig(new PncConfigProvider<>(configClass));
    }
}
