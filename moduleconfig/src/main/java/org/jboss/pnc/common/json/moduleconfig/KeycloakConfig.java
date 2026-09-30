/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
