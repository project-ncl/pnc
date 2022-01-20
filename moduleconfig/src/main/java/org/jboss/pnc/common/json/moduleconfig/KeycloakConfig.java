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
package org.jboss.pnc.common.json.moduleconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Web UI configuration parameters for Keycloak JavaScript adapter.
 *
 * @author Alex Creasy
 * @see <a href="http://keycloak.github.io/docs/userguide/keycloak-server/html/ch08.html#javascript-adapter">Keycloak JS
 *      Adapter Documentation</a>
 */
class KeycloakConfig {

    private final String url;
    private final String realm;
    private final String clientId;

    public KeycloakConfig(
            @JsonProperty("url") String url,
            @JsonProperty("realm") String realm,
            @JsonProperty("clientId") String clientId) {
        this.url = url;
        this.realm = realm;
        this.clientId = clientId;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("realm")
    public String getRealm() {
        return realm;
    }

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "Keycloak{" + "url='" + url + '\'' + ", realm='" + realm + '\'' + ", clientId='" + clientId + '\'' + '}';
    }
}
