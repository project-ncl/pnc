/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@class")
public class ModuleConfigJson {

    public String name;
    public List<AbstractModuleGroup> configs;

    @JsonCreator
    public ModuleConfigJson(@JsonProperty("name") String name) {
        this.name = name;
        configs = new ArrayList<>();
    }

    public void setConfigs(List<AbstractModuleGroup> configs) {
        this.configs = configs;
    }

    public void addConfig(AbstractModuleGroup moduleConfig) {
        configs.add(moduleConfig);
    }

    public List<AbstractModuleGroup> getConfigs() {
        return configs;
    }

    @Override
    public String toString() {
        return "ModuleConfigJson [name=" + name + ", configs=" + configs + "]";
    }

}
