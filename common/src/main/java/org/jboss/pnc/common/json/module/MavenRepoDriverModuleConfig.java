package org.jboss.pnc.common.json.module;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;


public class MavenRepoDriverModuleConfig extends AbstractModuleConfig{
    public MavenRepoDriverModuleConfig(@JsonProperty("base-url") String baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    @Override
    public String toString() {
        return "MavenRepoDriverModuleConfig [baseUrl=" + baseUrl + "]";
    }
}
