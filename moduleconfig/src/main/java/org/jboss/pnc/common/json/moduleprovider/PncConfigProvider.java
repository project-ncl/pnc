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
import java.util.List;

import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.BuildDriverRouterModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.DockerEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.JenkinsBuildDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.TermdBuildDriverModuleConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

public class PncConfigProvider <T extends AbstractModuleConfig> implements ConfigProvider<T> {
    
    private List<ProviderNameType<T>> moduleConfigs;
    private Class<T> type;
    
    public PncConfigProvider(Class<T> type) {
      this.type = type;  
      moduleConfigs = new ArrayList<>();  
      moduleConfigs.add(new ProviderNameType(JenkinsBuildDriverModuleConfig.class,"jenkins-build-driver"));
      moduleConfigs.add(new ProviderNameType(TermdBuildDriverModuleConfig.class,"termd-build-driver"));
      moduleConfigs.add(new ProviderNameType(BuildDriverRouterModuleConfig.class,"build-driver-router"));
      moduleConfigs.add(new ProviderNameType(MavenRepoDriverModuleConfig.class,"maven-repo-driver"));
      moduleConfigs.add(new ProviderNameType(DockerEnvironmentDriverModuleConfig.class,"docker-environment-driver"));
      moduleConfigs.add(new ProviderNameType(AuthenticationModuleConfig.class,"authentication-config"));
      moduleConfigs.add(new ProviderNameType(BpmModuleConfig.class,"bpm-config"));
    }
    
    /* (non-Javadoc)
     * @see org.jboss.pnc.common.json.moduleprovider.ConfigProviderN#registerProvider(com.fasterxml.jackson.databind.ObjectMapper)
     */
    @Override
    public void registerProvider(ObjectMapper mapper) {
        for (ProviderNameType<T> providerNameType : moduleConfigs) {
            mapper.registerSubtypes(new NamedType(providerNameType.getType(), providerNameType.getTypeName()));
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.pnc.common.json.moduleprovider.ConfigProviderN#getModuleConfigs()
     */
    @Override
    public List<ProviderNameType<T>> getModuleConfigs() {
        return moduleConfigs;
    }
    
    /* (non-Javadoc)
     * @see org.jboss.pnc.common.json.moduleprovider.ConfigProviderN#addModuleConfig(org.jboss.pnc.common.json.moduleprovider.ProviderNameType)
     */
    @Override
    public void addModuleConfig(ProviderNameType<T> providerNameType) {
        this.moduleConfigs.add(providerNameType);
    }

    public Class<T> getType() {
        return type;
    }
    
}
