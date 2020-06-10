/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.web;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.UIModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class UiConfigRestBuilder {

    public static UiConfigRest build(Configuration configuration) throws ConfigurationParseException {
        GlobalModuleGroup globalConfig = configuration.getGlobalConfig();
        UIModuleConfig uiModuleConfig = configuration.getModuleConfig(new PncConfigProvider<>(UIModuleConfig.class));
        ScmModuleConfig scmModuleConfig = configuration.getModuleConfig(new PncConfigProvider<>(ScmModuleConfig.class));
        return new UiConfigRest(globalConfig, uiModuleConfig, scmModuleConfig.getInternalScmAuthority());
    }
}
