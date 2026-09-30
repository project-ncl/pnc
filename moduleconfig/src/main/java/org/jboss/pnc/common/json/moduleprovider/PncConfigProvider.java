/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleprovider;

import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.AlignmentConfig;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.DemoDataConfig;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.SchedulerConfig;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleconfig.UIModuleConfig;

/**
 * @author <a href="mailto:pslegr@redhat.com">pslegr</a> on Aug 21, 2015
 *
 * @param <T> module config
 */
public class PncConfigProvider<T extends AbstractModuleConfig> extends AbstractConfigProvider<T>
        implements ConfigProvider<T> {

    public PncConfigProvider(Class<T> type) {
        setType(type);
        addModuleConfig(new ProviderNameType(SystemConfig.class, SystemConfig.MODULE_NAME));
        addModuleConfig(new ProviderNameType(IndyRepoDriverModuleConfig.class, IndyRepoDriverModuleConfig.MODULE_NAME));
        addModuleConfig(new ProviderNameType(BpmModuleConfig.class, BpmModuleConfig.MODULE_NAME));
        addModuleConfig(new ProviderNameType(UIModuleConfig.class, UIModuleConfig.MODULE_NAME));
        addModuleConfig(new ProviderNameType(DemoDataConfig.class, DemoDataConfig.MODULE_NAME));
        addModuleConfig(new ProviderNameType(AlignmentConfig.class, AlignmentConfig.MODULE_NAME));
        addModuleConfig(new ProviderNameType(ScmModuleConfig.class, ScmModuleConfig.MODULE_NAME));
        addModuleConfig(new ProviderNameType(SchedulerConfig.class, SchedulerConfig.MODULE_NAME));
    }
}
