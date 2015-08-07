package org.jboss.pnc.common.json.moduleprovider;

import java.util.List;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ConfigProvider<T extends AbstractModuleConfig> {

    void registerProvider(ObjectMapper mapper);

    List<ProviderNameType<T>> getModuleConfigs();

    void addModuleConfig(ProviderNameType<T> providerNameType);
    
    public Class<T> getType();

}