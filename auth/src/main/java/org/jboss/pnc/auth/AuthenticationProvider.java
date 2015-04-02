package org.jboss.pnc.auth;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;



public class AuthenticationProvider {
    
    private AccessToken auth;
    private AccessTokenResponse atr;
    
    
    public AuthenticationProvider(HttpServletRequest req) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
        this.auth = session.getToken();
    }
    
    public AuthenticationProvider(AccessToken accessToken, AccessTokenResponse atr) {
        this.auth = accessToken;
        this.atr = atr;
    }
    
    public String getEmail() {
        return this.auth.getEmail();
    }

    public String getPrefferedUserName() {
        return this.auth.getPreferredUsername();
    }

    public String getName() {
        return this.auth.getName();
    }
    
    public Set<String> getRole() {
        return this.auth.getRealmAccess().getRoles();
    }
    
    public boolean isUserInRole(String role) {
        return this.auth.getRealmAccess().isUserInRole(role);
    }
    
    public AccessToken getAccessToken() {
        return this.auth;
    }
    
    public String getTokenString() {
        return atr.getToken();
    }
    

    @Override
    public String toString() {
        return "AuthenticationProvider [getEmail()=" + getEmail() + ", getPrefferedUserName()=" + getPrefferedUserName()
                + ", getName()=" + getName() + ", getRole()=" + getRole() + ", getTokenString()=" + getTokenString() + "]";
    }
}
