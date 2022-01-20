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

import java.util.ArrayList;
import java.util.List;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

/**
 * @author <a href="mailto:pslegr@redhat.com">pslegr</a> on Aug 21, 2015
 *
 * @param <T> module config
 */
public abstract class AbstractConfigProvider<T extends AbstractModuleConfig> implements ConfigProvider<T> {

    private List<ProviderNameType<T>> moduleConfigs = new ArrayList<>();
    private Class<T> ctype;

    public void registerProvider(ObjectMapper mapper) {
        for (ProviderNameType<T> providerNameType : moduleConfigs) {
            mapper.registerSubtypes(new NamedType(providerNameType.getType(), providerNameType.getTypeName()));
        }
    }

    public List<ProviderNameType<T>> getModuleConfigs() {
        return moduleConfigs;
    }

    public void addModuleConfig(ProviderNameType<T> providerNameType) {
        this.moduleConfigs.add(providerNameType);
    }

    public Class<T> getType() {
        return ctype;
    }

    protected void setType(Class<T> ctype) {
        this.ctype = ctype;
    }
}
