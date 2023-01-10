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
package org.jboss.pnc.common.json.moduleprovider;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SchedulerConfig;
import org.jboss.pnc.common.json.moduleconfig.microprofile.SchedulerMicroprofileConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * This class registers Microprofile ConfigSources programmatically instead of having to specify each as META-INF
 * service.
 */
@Slf4j
public class MicroprofileConfigRegistrar implements ConfigSourceProvider {

    private final Configuration configuration;

    public MicroprofileConfigRegistrar() {
        try {
            this.configuration = new Configuration();
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
