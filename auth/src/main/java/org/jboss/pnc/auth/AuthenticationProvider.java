package org.jboss.pnc.auth;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;



public class AuthenticationProvider {
    public final static Logger log = Logger.getLogger(AuthenticationProvider.class);
    
    private AccessToken auth;
    private AccessTokenResponse atr;
    
    
    public AuthenticationProvider(HttpServletRequest req) throws SecurityContextNotAvailable{
        KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
        if(req == null || keycloakSecurityContext == null) { 
            throw new SecurityContextNotAvailable(SecurityContextNotAvailable.MSG);
        }
        this.auth = keycloakSecurityContext.getToken();
    }
    
    public AuthenticationProvider(HttpRequest req) throws SecurityContextNotAvailable{
        KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
        if(req == null || keycloakSecurityContext == null) { 
            throw new SecurityContextNotAvailable(SecurityContextNotAvailable.MSG);
        }
        this.auth = keycloakSecurityContext.getToken();
    }
    
    public AuthenticationProvider(SecurityContext securityContext) throws SecurityContextNotAvailable{
        KeycloakPrincipal principal =
                (KeycloakPrincipal)securityContext.getUserPrincipal();
        if(securityContext == null || principal == null) { 
            throw new SecurityContextNotAvailable(SecurityContextNotAvailable.MSG);
        }
        KeycloakSecurityContext keycloakSecurityContext = principal.getKeycloakSecurityContext();
        if(keycloakSecurityContext == null) { 
            throw new SecurityContextNotAvailable(SecurityContextNotAvailable.MSG);
        }
        this.auth = keycloakSecurityContext.getToken();
    }
    
    public AuthenticationProvider(AccessToken accessToken, AccessTokenResponse atr) throws SecurityContextNotAvailable{
        if(accessToken == null || atr == null) {
            throw new SecurityContextNotAvailable(SecurityContextNotAvailable.MSG);
        } 
        this.auth = accessToken;
        this.atr = atr;
    }
    
    public String getEmail() {
       return auth.getEmail();
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
        return auth;
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
