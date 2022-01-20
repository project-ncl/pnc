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

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.BasicAuthHelper;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.APPLICATION_FORM_URL_ENCODED;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.doPost;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.setSslRequired;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.urlencode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class KeycloakClient {

    static AccessTokenResponse getAuthTokensBySecret(
            String server,
            String realm,
            String clientId,
            String secret,
            boolean sslRequired) {
        return getAuthTokensBySecret(server, realm, null, null, clientId, secret, sslRequired);
    }

    static AccessTokenResponse getAuthTokensBySecret(
            String server,
            String realm,
            String user,
            String password,
            String clientId,
            String secret,
            boolean sslRequired) {
        StringBuilder body = new StringBuilder();
        try {
            if (user != null) {
                if (password == null) {
                    throw new RuntimeException("No password specified");
                }

                body.append("client_id=")
                        .append(urlencode(clientId))
                        .append("&grant_type=password")
                        .append("&username=")
                        .append(urlencode(user))
                        .append("&password=")
                        .append(urlencode(password));
            } else {
                body.append("grant_type=client_credentials");
            }

            setSslRequired(sslRequired);
            InputStream result = doPost(
                    server + "/realms/" + realm + "/protocol/openid-connect/token",
                    APPLICATION_FORM_URL_ENCODED,
                    APPLICATION_JSON,
                    body.toString(),
                    BasicAuthHelper.createHeader(clientId, secret));
            return JsonSerialization.readValue(result, AccessTokenResponse.class);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected error: ", e);
        } catch (IOException e) {
            throw new RuntimeException("Error receiving response: ", e);
        }
    }
}
