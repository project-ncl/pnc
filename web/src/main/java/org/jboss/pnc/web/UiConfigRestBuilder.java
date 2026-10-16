/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
