package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;


public class JenkinsBuildDriverModuleConfig extends AbstractModuleConfig{
    private String url;
    private String username;
    private String password;

    public JenkinsBuildDriverModuleConfig(@JsonProperty("url") String url, 
            @JsonProperty("username") String username, @JsonProperty("password")String password) {
        super();
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public String toString() {
        return "JenkinsBuildDriverModuleConfig [url=" + url + ", username=" + username + ", password=" + password + "]";
    }
}
