/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.common.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@class")
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
