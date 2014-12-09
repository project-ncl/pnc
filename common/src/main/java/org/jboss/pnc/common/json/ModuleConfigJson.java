package org.jboss.pnc.common.json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@class")
public class ModuleConfigJson {

    public String name;
    public List<AbstractModuleConfig> configs;
    
    @JsonCreator
    public ModuleConfigJson(@JsonProperty("name") String name) {
        this.name = name;
        configs = new ArrayList<AbstractModuleConfig>();
    }

    public void setConfigs(List<AbstractModuleConfig> configs) {
        this.configs = configs;
    }
    
    public void addConfig(AbstractModuleConfig moduleConfig) {
        configs.add(moduleConfig);
    }

    public List<AbstractModuleConfig> getConfigs() {
        return configs;
    }

    @Override
    public String toString() {
        return "ModuleConfigJson [name=" + name + ", configs=" + configs + "]";
    }
    
}
