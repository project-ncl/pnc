/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
