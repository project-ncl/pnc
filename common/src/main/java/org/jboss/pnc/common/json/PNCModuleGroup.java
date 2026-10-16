/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(value = "pnc")
public class PNCModuleGroup extends AbstractModuleGroup {
    public List<AbstractModuleConfig> configs = new ArrayList<>();

    public void setConfigs(List<AbstractModuleConfig> configs) {
        this.configs = configs;
    }

    public void addConfig(AbstractModuleConfig moduleConfig) {
        configs.add(moduleConfig);
    }

    public List<AbstractModuleConfig> getConfigs() {
        return configs;
    }

}
