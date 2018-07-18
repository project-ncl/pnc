/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleconfig.TermdBuildDriverModuleConfig;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class ModuleConfigFactory {

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
}
