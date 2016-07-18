/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.keycloak.OAuth2Constants;
import org.keycloak.RSATokenVerifier;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AuthUtils {

    public final static Logger log = Logger.getLogger(AuthUtils.class);

    private static AccessTokenResponse tokenResponse;

    public static boolean authEnabled() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classLoader.getResourceAsStream("auth.properties");
        if (stream != null) {
            Properties properties = new Properties();
            properties.load(stream);
            String value = properties.getProperty("authentication.test.enabled");
            return value.equals("true");
        }
        return false;
    }

    public static String generateToken() throws IOException, ConfigurationParseException {
        if (AuthUtils.authEnabled()) {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration.getModuleConfig(new PncConfigProvider<>(AuthenticationModuleConfig.class));
            AuthenticationProvider authProvider = authenticate(config);
            return authProvider.getTokenString();
        } else {
            return "no-auth";
        }
    }

    public static AuthenticationProvider authenticate(AuthenticationModuleConfig config)
            throws IOException {
        AuthenticationProvider provider = null;
        try {
            provider = new AuthenticationProvider(
                    authenticateToken(
                            authenticateUser(config.getUsername(), config.getPassword(), config.getAuthServerUrl(), config.getRealm(), config.getResource())
                                    .getToken(), config.getPublicRealmKey()), tokenResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return provider;
    }

    private static AccessTokenResponse authenticateUser(String username, String password, String authServerUrl, String realm, String resource)
            throws IOException {
        HttpClientBuilder clientbuilder = new HttpClientBuilder();
        HttpClient client = clientbuilder.disableTrustManager().build();

        log.debug(">>> keycloakDeployment.getAuthServerBaseUrl():" + authServerUrl);
        log.debug(">>> keycloakDeployment.getRealm():" + realm);
        log.debug(">>> keycloakDeployment.getResourceName():" + resource);

        HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(authServerUrl)
                .path(ServiceUrlConstants.TOKEN_PATH).build(realm));
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD));
        formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, resource));
        formparams.add(new BasicNameValuePair("username", username));
        formparams.add(new BasicNameValuePair("password", password));
        UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
        post.setEntity(form);

        HttpResponse response = client.execute(post);
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (status != 200) {
            String json = getContent(entity);
            throw new IOException("Bad status: " + status + " response: " + json);
        }
        if (entity == null) {
            throw new IOException("No Entity");
        }
        String json = getContent(entity);
        tokenResponse = JsonSerialization.readValue(json, AccessTokenResponse.class);
        log.debug(">>> accessTokenResponse:" + tokenResponse.getToken());
        return tokenResponse;
    }

    private static AccessToken authenticateToken(String tokenString, PublicKey key) {
        try {
            AccessToken token = RSATokenVerifier.toAccessToken(tokenString, key);
            return token;
        } catch (VerificationException e) {
            log.error("Failed to verify token", e);
            return null;
        }
    }

    private static String getContent(HttpEntity entity) throws IOException {
        if (entity == null)
            return null;
        InputStream is = entity.getContent();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int c;
            while ((c = is.read()) != -1) {
                os.write(c);
            }
            byte[] bytes = os.toByteArray();
            String data = new String(bytes);
            return data;
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {

            }
        }

    }
}
