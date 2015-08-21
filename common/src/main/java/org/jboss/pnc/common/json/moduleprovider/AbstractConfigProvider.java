package org.jboss.pnc.common.json.moduleprovider;

import java.util.List;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

public abstract class AbstractConfigProvider <T extends AbstractModuleConfig> implements ConfigProvider<T>{
    
    List<ProviderNameType<T>> moduleConfigs;
    Class<T> ctype;

    public void registerProvider(ObjectMapper mapper) {
        for (ProviderNameType<T> providerNameType : moduleConfigs) {
            mapper.registerSubtypes(new NamedType(providerNameType.getType(), providerNameType.getTypeName()));
        }
    }

    public List<ProviderNameType<T>> getModuleConfigs() {
        return moduleConfigs;
    }
    
    public void addModuleConfig(ProviderNameType<T> providerNameType) {
        this.moduleConfigs.add(providerNameType);
    }

    public Class<T> getType() {
        return ctype;
    }
    
    
}
