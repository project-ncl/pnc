/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import java.util.List;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:pslegr@redhat.com">pslegr</a> on Aug 21, 2015
 *
 * @param <T> module config
 */
public interface ConfigProvider<T extends AbstractModuleConfig> {

    void registerProvider(ObjectMapper mapper);

    List<ProviderNameType<T>> getModuleConfigs();

    void addModuleConfig(ProviderNameType<T> providerNameType);

    public Class<T> getType();

}