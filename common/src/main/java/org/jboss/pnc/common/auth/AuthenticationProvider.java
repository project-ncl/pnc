package org.jboss.pnc.common.auth;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;



public class AuthenticationProvider {
    
    private HttpServletRequest request;
    
    
    public AuthenticationProvider(HttpServletRequest req) {
        this.request = req;
    }
    
    private AccessToken getAuth() {
        KeycloakSecurityContext session = (KeycloakSecurityContext) this.request.getAttribute(KeycloakSecurityContext.class.getName());
        return session.getToken();
    }

    public String getEmail() {
        return getAuth().getEmail();
    }

    public String getPrefferedUserName() {
        return getAuth().getPreferredUsername();
    }

    public String getName() {
        return getAuth().getName();
    }
    
    public Set<String> getRole() {
        return getAuth().getRealmAccess().getRoles();
    }
    
    public boolean isUserInRole(String role) {
        return getAuth().getRealmAccess().isUserInRole(role);
    }
    
    public AccessToken getAccessToken() {
        return this.getAuth();
    }

    

}
