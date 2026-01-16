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

import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.slsa.BuilderConfig;

/**
 * @author <a href="mailto:andrea.vibelli@gmail.com">Andrea Vibelli</a>
 *
 * @param <T> module config
 */
public class SlsaConfigProvider<T extends AbstractModuleConfig> extends AbstractConfigProvider<T>
        implements ConfigProvider<T> {

    public SlsaConfigProvider(Class<T> type) {
        setType(type);

        addModuleConfig(new ProviderNameType(BuilderConfig.class, BuilderConfig.MODULE_NAME));

    }
}
