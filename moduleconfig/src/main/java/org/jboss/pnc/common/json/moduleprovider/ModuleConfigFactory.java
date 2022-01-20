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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.AlignmentConfig;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.DemoDataConfig;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftBuildAgentConfig;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleconfig.TermdBuildDriverModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class ModuleConfigFactory {

    private final Logger logger = LoggerFactory.getLogger(ModuleConfigFactory.class);

    private Configuration configuration;

    @Inject
    public ModuleConfigFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Produces
    @Dependent
    public SystemConfig createSystemConfig() throws ConfigurationParseException {
        return configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class));
    }

    @Produces
    @Dependent
    public AlignmentConfig createAlignmentConfig() throws ConfigurationParseException {
        return configuration.getModuleConfig(new PncConfigProvider<>(AlignmentConfig.class));
    }

    @Produces
    @Dependent
    public DemoDataConfig createDemoDataConfig() throws ConfigurationParseException {
        return configuration.getModuleConfig(new PncConfigProvider<>(DemoDataConfig.class));
    }

    @Produces
    @Dependent
    public BpmModuleConfig createBpmModuleConfig() throws ConfigurationParseException {
        return configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));
    }

    @Produces
    @Dependent
    IndyRepoDriverModuleConfig createMavenRepoDriverModuleConfig() throws ConfigurationParseException {
        return configuration.getModuleConfig(new PncConfigProvider<>(IndyRepoDriverModuleConfig.class));
    }

    @Produces
    @Dependent
    TermdBuildDriverModuleConfig createTermdBuildDriverModuleConfig() throws ConfigurationParseException {
        return configuration.getModuleConfig(new PncConfigProvider<>(TermdBuildDriverModuleConfig.class));
    }

    @Produces
    @Dependent
    OpenshiftEnvironmentDriverModuleConfig createOpenshiftEnvironmentDriverModuleConfig()
            throws ConfigurationParseException {
        return configuration.getModuleConfig(new PncConfigProvider<>(OpenshiftEnvironmentDriverModuleConfig.class));
    }

    @Produces
    @Dependent
    OpenshiftBuildAgentConfig createOpenshiftBuildAgentConfig() throws ConfigurationParseException {
        try {
            return configuration.getModuleConfig(new PncConfigProvider<>(OpenshiftBuildAgentConfig.class));
        } catch (ConfigurationParseException e) {
            logger.warn("OpenshiftBuildAgentConfig is not provided or is broken. Using the default built-in config.");
            return null;
        }
    }

    @Produces
    @Dependent
    GlobalModuleGroup createGlobalModuleGroup() {
        try {
            return configuration.getGlobalConfig();
        } catch (ConfigurationParseException e) {
            logger.warn("GlobalModuleGroup is not provided or is broken.");
            return null;
        }
    }

}
