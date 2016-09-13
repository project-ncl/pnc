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
package org.jboss.pnc.common.json.moduleconfig;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.pnc.common.json.AbstractModuleConfig;

public class AuthenticationModuleConfig extends AbstractModuleConfig {

    public static String MODULE_NAME = "authentication-config";

    /**
     * Username to be able to authenticate against pnc authentication service provider
     */
    private String username;

    /**
     * Password to be able to authenticate against pnc authentication service provider
     */
    private String password;

    /**
     * Base URL of REST endpoint services to be accessed from external resources
     */
    private String authServerUrl;

    private String realmKey;

    private String realm;

    private String resource;

    public AuthenticationModuleConfig(@JsonProperty("username") String username,
            @JsonProperty("password") String password, @JsonProperty("auth-server-url") String authServerUrl, @JsonProperty("realm-public-key") String realmKey,
            @JsonProperty("realm") String realm, @JsonProperty("resource") String resource) {
        this.username = username;
        this.password = password;
        this.authServerUrl = authServerUrl;
        this.realmKey = realmKey;
        this.realm = realm;
        this.resource = resource;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PublicKey getPublicRealmKey() {
        try {
            byte[] byteKey = Base64.getDecoder().decode(realmKey.getBytes());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString() {
        return "AuthenticationModuleConfig [username=" + username + ", password=HIDDEN, baseAuthUrl=" + authServerUrl +"]";
    }

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public String getRealm() {
        return realm;
    }

    public String getResource() {
        return resource;
    }

}
