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

import java.util.ArrayList;

import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.BuildDriverRouterModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.DockerEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.JenkinsBuildDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.TermdBuildDriverModuleConfig;

/**
 * @author <a href="mailto:pslegr@redhat.com">pslegr</a> on Aug 21, 2015
 *
 * @param <T> module config
 */
public class PncConfigProvider <T extends AbstractModuleConfig> 
            extends AbstractConfigProvider<T> implements ConfigProvider<T> {
    
    public PncConfigProvider(Class<T> type) {
      ctype = type;  
      moduleConfigs = new ArrayList<>();  
      moduleConfigs.add(new ProviderNameType(JenkinsBuildDriverModuleConfig.class,"jenkins-build-driver"));
      moduleConfigs.add(new ProviderNameType(TermdBuildDriverModuleConfig.class,"termd-build-driver"));
      moduleConfigs.add(new ProviderNameType(BuildDriverRouterModuleConfig.class,"build-driver-router"));
      moduleConfigs.add(new ProviderNameType(MavenRepoDriverModuleConfig.class,"maven-repo-driver"));
      moduleConfigs.add(new ProviderNameType(DockerEnvironmentDriverModuleConfig.class,"docker-environment-driver"));
      moduleConfigs.add(new ProviderNameType(AuthenticationModuleConfig.class,"authentication-config"));
      moduleConfigs.add(new ProviderNameType(BpmModuleConfig.class,"bpm-config"));
    }
}
