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
import java.net.SocketException;
import java.util.function.Supplier;

import org.apache.http.ConnectionClosedException;
import org.apache.http.NoHttpResponseException;
import org.jboss.pnc.auth.keycloakutil.util.HttpUtil;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.BasicAuthHelper;
import org.keycloak.util.JsonSerialization;

import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Slf4j
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
            String url = server + "/realms/" + realm + "/protocol/openid-connect/token";
            InputStream result = retryOnceOnIoError(
                    url,
                    () -> doPost(
                            url,
                            APPLICATION_FORM_URL_ENCODED,
                            APPLICATION_JSON,
                            body.toString(),
                            BasicAuthHelper.createHeader(clientId, secret)));
            return JsonSerialization.readValue(result, AccessTokenResponse.class);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected error: ", e);
        } catch (IOException e) {
            throw new RuntimeException("Error receiving response: ", e);
        }
    }

    /** Before sending a token request again: the server or a load balancer may be closing connections. */
    static final long RETRY_DELAY_MILLIS = 200;

    /**
     * A token request has no side effect: it is sent again once when the connection was dropped (reset or closed
     * without an answer, e.g. a pooled connection the server had closed). Errors answered by the server, timeouts and
     * TLS errors are not retried.
     */
    static <T> T retryOnceOnIoError(String url, Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpUtil.RequestFailedException e) {
            if (!connectionDropped(e.getCause())) {
                throw e;
            }
            log.warn(
                    "Token request to {} failed ({}: {}), retrying once",
                    url,
                    e.getCause().getClass().getSimpleName(),
                    e.getCause().getMessage());
            try {
                Thread.sleep(RETRY_DELAY_MILLIS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw e;
            }
            return request.get();
        }
    }

    static boolean connectionDropped(Throwable cause) {
        return cause instanceof SocketException || cause instanceof NoHttpResponseException
                || cause instanceof ConnectionClosedException;
    }
}
