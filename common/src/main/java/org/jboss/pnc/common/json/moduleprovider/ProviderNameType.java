package org.jboss.pnc.common.json.moduleprovider;

import org.jboss.pnc.common.json.AbstractModuleConfig;

public class ProviderNameType<T extends AbstractModuleConfig> {
    
    private Class<T> type;
    private String typeName;
    
    public ProviderNameType(Class<T> type, String typeName) {
        super();
        this.type = type;
        this.typeName = typeName;
    }

    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}
