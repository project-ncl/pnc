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

import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.*;

/**
 * @author <a href="mailto:pslegr@redhat.com">pslegr</a> on Aug 21, 2015
 *
 * @param <T> module config
 */
public class PncConfigProvider <T extends AbstractModuleConfig> 
            extends AbstractConfigProvider<T> implements ConfigProvider<T> {
    
    public PncConfigProvider(Class<T> type) {
      setType(type); 
      addModuleConfig(new ProviderNameType(JenkinsBuildDriverModuleConfig.class, "jenkins-build-driver"));
      addModuleConfig(new ProviderNameType(TermdBuildDriverModuleConfig.class, "termd-build-driver"));
      addModuleConfig(new ProviderNameType(SystemConfig.class, "system-config"));
      addModuleConfig(new ProviderNameType(MavenRepoDriverModuleConfig.class, "maven-repo-driver"));
      addModuleConfig(new ProviderNameType(AuthenticationModuleConfig.class, "authentication-config"));
      addModuleConfig(new ProviderNameType(BpmModuleConfig.class, "bpm-config"));
      addModuleConfig(new ProviderNameType(OpenshiftEnvironmentDriverModuleConfig.class, OpenshiftEnvironmentDriverModuleConfig.MODULE_NAME));
        addModuleConfig(new ProviderNameType(UIModuleConfig.class, UIModuleConfig.MODULE_NAME));
    }
}
