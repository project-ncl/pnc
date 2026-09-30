/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.KeycloakClientConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.keycloak.representations.AccessTokenResponse;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class DefaultKeycloakServiceClient implements KeycloakServiceClient {

    private KeycloakClientConfig keycloakServiceAccountConfig;
    private long serviceTokenRefreshIfExpiresInSeconds;

    private AccessTokenResponse keycloakToken;

    private Instant expiresAt;

    @Deprecated // CDI workaround
    public DefaultKeycloakServiceClient() {
    }

    @Inject
    public DefaultKeycloakServiceClient(SystemConfig systemConfig) throws ConfigurationParseException {
        keycloakServiceAccountConfig = systemConfig.getKeycloakServiceAccountConfig();
        serviceTokenRefreshIfExpiresInSeconds = systemConfig.getServiceTokenRefreshIfExpiresInSeconds();
    }

    @Override
    public String getAuthToken() {
        if (keycloakToken == null || refreshRequired()) {
            keycloakToken = KeycloakClient.getAuthTokensBySecret(
                    keycloakServiceAccountConfig.getAuthServerUrl(),
                    keycloakServiceAccountConfig.getRealm(),
                    keycloakServiceAccountConfig.getResource(),
                    keycloakServiceAccountConfig.getSecret(),
                    keycloakServiceAccountConfig.getSslRequired());
            expiresAt = Instant.now().plus(keycloakToken.getExpiresIn(), ChronoUnit.SECONDS);
        }
        return keycloakToken.getToken();
    }

    private boolean refreshRequired() {

        if (expiresAt == null) {
            // if we accidentally call this method before expiresAt is set, then we obviously need to get a new token
            return true;
        }
        // make sure the token is still valid 'serviceTokenRefreshIfExpiresInSeconds' seconds from now, which is the
        // max 'supported' duration of a build. We need that token to be valid for actions done at the end of the build
        return expiresAt.isBefore(Instant.now().plus(serviceTokenRefreshIfExpiresInSeconds, ChronoUnit.SECONDS));
    }
}
