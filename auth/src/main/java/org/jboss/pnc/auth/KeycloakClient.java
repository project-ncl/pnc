/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.APPLICATION_FORM_URL_ENCODED;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.doPost;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.setSslRequired;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.urlencode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.BasicAuthHelper;
import org.keycloak.util.JsonSerialization;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class KeycloakClient {

    static AccessTokenResponse getAuthTokensBySecret(
            String server,
            String realm,
            String clientId,
            String secret,
            boolean sslRequired) {
        return getAuthTokensBySecret(server, realm, null, null, clientId, secret, sslRequired);
    }

    public static AccessTokenResponse getAuthTokensBySecret(
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
