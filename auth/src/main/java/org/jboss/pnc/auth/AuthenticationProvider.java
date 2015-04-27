package org.jboss.pnc.auth;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;



/**
 * This class provides access to authenticated user info. In case no authentication
 * is configured or there are problems with authentication the default demo-user is
 * returned instead
 * 
 * @author pslegr
 *
 */
public class AuthenticationProvider {
    public final static Logger log = Logger.getLogger(AuthenticationProvider.class);
    public final static String MSG = "Authentication could not be enabled";
    
    private AccessToken auth;
    private AccessTokenResponse atr;
    
    
    public AuthenticationProvider(HttpServletRequest req){
        try {
            KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
            if(req == null || keycloakSecurityContext == null) { 
                throw new SecurityContextNotAvailable(SecurityContextNotAvailable.MSG);
            }
            this.auth = keycloakSecurityContext.getToken();
        }
        catch (NoClassDefFoundError ncdfe) {
            log.warn(MSG + ": " + ncdfe.getMessage());
            log.warn("using " + DemoUser.username + " instead");
        }
        catch (SecurityContextNotAvailable scnae) {
            log.warn(MSG + ": " + scnae.getMessage());
            log.warn("using " + DemoUser.username + " instead");
        }
    }
    
    public AuthenticationProvider(HttpRequest req){
        try {
            KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
            if(req == null || keycloakSecurityContext == null) { 
                throw new SecurityContextNotAvailable(SecurityContextNotAvailable.MSG);
            }
            this.auth = keycloakSecurityContext.getToken();
        }
        catch (NoClassDefFoundError ncdfe) {
            log.warn(MSG + ": " + ncdfe.getMessage());
            log.warn("using " + DemoUser.username + " instead");
        }
        catch (SecurityContextNotAvailable scnae) {
            log.warn(MSG + ": " + scnae.getMessage());
            log.warn("using " + DemoUser.username + " instead");
        }
    }
    
    public AuthenticationProvider(SecurityContext securityContext){
        try {
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
        catch (NoClassDefFoundError ncdfe) {
            log.warn(MSG + ": " + ncdfe.getMessage());
            log.warn("using " + DemoUser.username + " instead");
        }
        catch (SecurityContextNotAvailable scnae) {
            log.warn(MSG + ": " + scnae.getMessage());
            log.warn("using " + DemoUser.username + " instead");
        }
    }
    
    public AuthenticationProvider(AccessToken accessToken, AccessTokenResponse atr){
        try {
            if(accessToken == null || atr == null) {
                throw new SecurityContextNotAvailable(SecurityContextNotAvailable.MSG);
            } 
            this.auth = accessToken;
            this.atr = atr;
        }
        catch (NoClassDefFoundError ncdfe) {
            log.warn(MSG + ": " + ncdfe.getMessage());
            log.warn("using " + DemoUser.username + " instead");
        }
        catch (SecurityContextNotAvailable scnae) {
            log.warn(MSG + ": " + scnae.getMessage());
            log.warn("using " + DemoUser.username + " instead");
        }
    }
    
    public String getEmail() {
       if(auth == null) {
           return DemoUser.email;
       } 
       return auth.getEmail();
    }

    public String getUserName() {
        if(auth == null) {
            return DemoUser.username;
        } 
        return this.auth.getPreferredUsername();
    }

    public String getFirstName() {
        if(auth == null) {
            return DemoUser.firstname;
        } 
        return this.auth.getGivenName();
    }
    
    public String getLastName() {
        if(auth == null) {
            return DemoUser.lastname;
        } 
        return this.auth.getFamilyName();
    }

    
    public Set<String> getRole() {
        if(auth == null) {
            return DemoUser.roles;
        } 
        return this.auth.getRealmAccess().getRoles();
    }
    
    public boolean isUserInRole(String role) {
        if(auth == null) {
            return DemoUser.hasRole(role);
        } 
        return this.auth.getRealmAccess().isUserInRole(role);
    }
    
    public AccessToken getAccessToken() {
        return auth;
    }
    
    public String getTokenString() {
        if(atr != null) {
            return atr.getToken();
        }
        return DemoUser.token;
    }
    
    private final static class DemoUser {
        static String token = "no-token";
        static String username = "demo-user";
        static String firstname = "Demo First Name";
        static String lastname = "Demo Last Name";
        static String email = "demo-user@pnc.com";
        static Set<String> roles = new HashSet<String>();
        static {
            roles.add("user");
        }
        public final static boolean hasRole(String role) {
            return role.contains(role);
        }
    }
    
    @Override
    public String toString() {
        return "AuthenticationProvider [auth=" + auth + ", atr=" + atr + ", getEmail()=" + getEmail() + ", getUserName()="
                + getUserName() + ", getFirstName()=" + getFirstName() + ", getLastName()=" + getLastName() + ", getRole()="
                + getRole() + ", getAccessToken()=" + getAccessToken() + ", getTokenString()=" + getTokenString() + "]";
    }
}
