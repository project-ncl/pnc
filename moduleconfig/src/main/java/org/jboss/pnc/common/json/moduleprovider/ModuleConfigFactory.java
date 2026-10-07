/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleprovider;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.AlignmentConfig;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.DemoDataConfig;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.SchedulerConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    SchedulerConfig createSchedulerConfig() throws ConfigurationParseException {
        return configuration.getModuleConfig(new PncConfigProvider<>(SchedulerConfig.class));
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
