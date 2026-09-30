/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.authz.Attributes;
import org.wildfly.security.http.oidc.AccessToken;
import org.wildfly.security.http.oidc.OidcSecurityContext;

public class ElytronLoggedInUser implements LoggedInUser {

    private final static Logger log = LoggerFactory.getLogger(ElytronLoggedInUser.class);

    public final static String MSG = "Authentication could not be enabled";

    private UserInfo userInfo;

    private static class UserInfo {
        String username;
        String firstName;
        String lastName;
        String email;
        Set<String> roles = new HashSet<>();
    }

    public ElytronLoggedInUser(HttpServletRequest httpServletRequest) {
        try {
            OidcSecurityContext oidcSecurityContext = (OidcSecurityContext) httpServletRequest
                    .getAttribute(OidcSecurityContext.class.getName());
            SecurityIdentity identity = SecurityDomain.getCurrent().getCurrentSecurityIdentity();
            if (oidcSecurityContext == null) {
                // if not using OIDC: either using LDAP or anonymous user
                userInfo = createUserInfoFromAttributes(identity);
            } else {
                AccessToken accessToken = oidcSecurityContext.getToken();
                userInfo = createUserInforFromOidcAccessToken(accessToken);
            }

            log.info("User {} logged", userInfo.username);
        } catch (NoClassDefFoundError ncdfe) {
            handleAuthenticationProblem(ncdfe.getMessage(), ncdfe);
        }
    }

    @Override
    public String getEmail() {
        return userInfo.email;
    }

    @Override
    public String getUserName() {
        return userInfo.username;
    }

    @Override
    public String getFirstName() {
        return userInfo.firstName;
    }

    @Override
    public String getLastName() {
        return userInfo.lastName;
    }

    @Override
    public Set<String> getRole() {
        return userInfo.roles;
    }

    @Override
    public boolean isUserInRole(String role) {
        return userInfo.roles.contains(role);
    }

    @Override
    public String getTokenString() {
        return "";
    }

    private void handleAuthenticationProblem(String warning, Throwable cause) {
        log.warn(MSG + ": " + warning, cause);
        throw new AuthenticationException(MSG + ": " + warning, cause);
    }

    /**
     * The attributes are directly from the LDAP output for a user. Update as necessary if the LDAP keys change For
     * anonymous user, everything should be null
     *
     * @param identity the identity of the user
     * @return UserInfo DTO
     */
    private UserInfo createUserInfoFromAttributes(SecurityIdentity identity) {
        UserInfo userInfo = new UserInfo();
        Attributes attrs = identity.getAttributes();

        if (attrs.containsKey("username")) {
            userInfo.username = attrs.getFirst("username");
        }
        if (attrs.containsKey("firstName")) {
            userInfo.firstName = attrs.getFirst("firstName");
        }
        if (attrs.containsKey("lastName")) {
            userInfo.lastName = attrs.getFirst("lastName");
        }
        if (attrs.containsKey("email")) {
            userInfo.email = attrs.getFirst("email");
        }
        identity.getRoles().forEach(userInfo.roles::add);

        return userInfo;
    }

    /**
     * Return a UserInfo based on the data in the access token. If the fields change with changing OIDC server, we'll
     * need to adjust the claims here
     * 
     * @param accessToken access token from OIDC
     * @return UserInfo DTO
     */
    private UserInfo createUserInforFromOidcAccessToken(AccessToken accessToken) {
        UserInfo userInfo = new UserInfo();

        userInfo.username = accessToken.getClaimValueAsString("preferred_username");
        userInfo.firstName = accessToken.getClaimValueAsString("firstName");
        userInfo.lastName = accessToken.getClaimValueAsString("lastName");
        userInfo.email = accessToken.getClaimValueAsString("email");
        if (accessToken.getRealmAccessClaim() != null && accessToken.getRealmAccessClaim().getRoles() != null) {
            userInfo.roles = new HashSet<>(accessToken.getRealmAccessClaim().getRoles());
        } else {
            // for the other SSO server at the other company
            // preferred_username for the other SSO is just a random id, sub contains the email address which is
            // more readable. Could also have used 'emailAddress'.
            userInfo.username = accessToken.getClaimValueAsString("sub");
            userInfo.roles = new HashSet<>(accessToken.getStringListClaimValue("groups"));
        }

        return userInfo;
    }
}
