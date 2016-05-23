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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.javafx.collections.MappingChange;
import org.jboss.pnc.common.json.AbstractModuleConfig;

import java.util.Map;

/**
 * @author Alex Creasy
 */
public class UIModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "ui";

    private final String pncUrl;
    private final String pncNotificationsUrl;
    private final String daUrl;
    private final String daReportsUrl;
    private final Keycloak keycloak;

    public UIModuleConfig(
            @JsonProperty("pnc-url") String pncUrl,
            @JsonProperty("pnc-notifications-url") String pncNotificationsUrl,
            @JsonProperty("da-url") String daUrl,
            @JsonProperty("da-reports-url") String daReportsUrl,
            @JsonProperty("keycloak") Keycloak keycloak) {
        this.pncUrl = pncUrl;
        this.pncNotificationsUrl = pncNotificationsUrl;
        this.daUrl = daUrl;
        this.daReportsUrl = daReportsUrl;
        this.keycloak = keycloak;
    }

    public String getPncUrl() {
        return pncUrl;
    }

    public String getPncNotificationsUrl() {
        return pncNotificationsUrl;
    }

    public String getDaUrl() {
        return daUrl;
    }

    public String getDaReportsUrl() {
        return daReportsUrl;
    }

    public Keycloak getKeycloak() {
        return keycloak;
    }

    /**
     *
     */
    static class Keycloak {
        private final String realm;
        private final String realmPublicKey;
        private final String authServerUrl;
        private final String sslRequired;
        private final String resource;
        private final boolean isResourceRoleMappings;
        private final boolean bearerOnly;
        private final boolean enableBasicAuth;
        private final boolean exposeToken;
        private final Map<String, String> credentials;

        public Keycloak(
                @JsonProperty("realm") String realm,
                @JsonProperty("realm-public-key") String realmPublicKey,
                @JsonProperty("auth-server-url") String authServerUrl,
                @JsonProperty("ssl-required") String sslRequired,
                @JsonProperty("resource") String resource,
                @JsonProperty("use-resource-role-mappings") boolean isResourceRoleMappings,
                @JsonProperty("bearer-only") boolean bearerOnly,
                @JsonProperty("enable-basic-auth") boolean enableBasicAuth,
                @JsonProperty("expose-token") boolean exposeToken,
                @JsonProperty("credentials") Map<String, String> credentials) {
            this.realm = realm;
            this.realmPublicKey = realmPublicKey;
            this.authServerUrl = authServerUrl;
            this.sslRequired = sslRequired;
            this.resource = resource;
            this.isResourceRoleMappings = isResourceRoleMappings;
            this.bearerOnly = bearerOnly;
            this.enableBasicAuth = enableBasicAuth;
            this.exposeToken = exposeToken;
            this.credentials = credentials;
        }

        public String getRealm() {
            return realm;
        }

        public String getRealmPublicKey() {
            return realmPublicKey;
        }

        public String getAuthServerUrl() {
            return authServerUrl;
        }

        public String getSslRequired() {
            return sslRequired;
        }

        public String getResource() {
            return resource;
        }

        public boolean isResourceRoleMappings() {
            return isResourceRoleMappings;
        }

        public boolean isBearerOnly() {
            return bearerOnly;
        }

        public boolean isEnableBasicAuth() {
            return enableBasicAuth;
        }

        public boolean isExposeToken() {
            return exposeToken;
        }

        public Map<String, String> getCredentials() {
            return credentials;
        }
    }
}
