/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.auth;

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.KeycloakClientConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.keycloak.representations.AccessTokenResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
