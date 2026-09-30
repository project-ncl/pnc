/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.common.json.moduleconfig.ServiceAccountClientConfig.Mode;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of the ServiceAccountClient interface.
 *
 * Right now it only returns the OIDC auth value (Bearer xxx) but in the future it could be doing Basic auth (Basic xx)
 * via a switch in our configuration
 */
@ApplicationScoped
@Slf4j
public class DefaultServiceAccountClient implements ServiceAccountClient {

    KeycloakServiceClient keycloakServiceClient;
    LDAPServiceClient ldapServiceClient;

    SystemConfig systemConfig;

    @Deprecated // CDI workaround
    public DefaultServiceAccountClient() {
    }

    @Inject
    public DefaultServiceAccountClient(
            SystemConfig systemConfig,
            KeycloakServiceClient keycloakServiceClient,
            LDAPServiceClient ldapServiceClient) {
        this.systemConfig = systemConfig;
        this.keycloakServiceClient = keycloakServiceClient;
        this.ldapServiceClient = ldapServiceClient;
    }

    @Override
    public String getAuthHeaderValue() {

        Mode authMode = systemConfig.getServiceAccountClientConfig().getMode();
        log.info("Service client mode is: {}", authMode);
        if (authMode == Mode.KEYCLOAK) {
            return oidcHeaderValue();
        } else if (authMode == Mode.LDAP) {
            return ldapHeaderValue();
        } else {
            throw new RuntimeException("serviceAccountClientConfig mode is not supported: " + authMode);
        }
    }

    private String oidcHeaderValue() {
        return "Bearer " + keycloakServiceClient.getAuthToken();
    }

    public String ldapHeaderValue() {
        return ldapServiceClient.getHeader();
    }
}
