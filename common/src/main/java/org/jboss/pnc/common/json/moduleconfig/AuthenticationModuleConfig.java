package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;


public class AuthenticationModuleConfig extends AbstractModuleConfig{
    private String username;
    private String password;
    private String baseAuthUrl;

    public AuthenticationModuleConfig(@JsonProperty("username") String username, 
            @JsonProperty("password")String password, @JsonProperty("baseAuthUrl")String baseAuthUrl) {
        super();
        this.username = username;
        this.password = password;
        this.baseAuthUrl = baseAuthUrl;
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

    public String getBaseAuthUrl() {
        return baseAuthUrl;
    }

    public void setBaseAuthUrl(String baseAuthUrl) {
        this.baseAuthUrl = baseAuthUrl;
    }
    
    @Override
    public String toString() {
        return "AuthenticationModuleConfig [username=HIDDEN, password=" + password + ", baseAuthUrl=" + baseAuthUrl +"]";
    }
    
}
