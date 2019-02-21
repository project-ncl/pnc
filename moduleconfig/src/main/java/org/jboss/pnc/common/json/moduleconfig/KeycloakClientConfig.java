/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import javax.ws.rs.DefaultValue;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
public class KeycloakClientConfig {
    private final String realm;
    private final String realmPublicKey;
    private final String authServerUrl;
    private final Boolean sslRequired;
    private final String resource;
    private final Map<String, String> credentials;

    @JsonCreator
    public KeycloakClientConfig(
            @JsonProperty("realm") String realm,
            @JsonProperty("realm-public-key") String realmPublicKey,
            @JsonProperty("auth-server-url") String authServerUrl,
            @JsonProperty("ssl-required") Boolean sslRequired,
            @JsonProperty("resource") String resource,
            @JsonProperty("credentials") @DefaultValue("{}") Map<String, String> credentials) {
        this.realm = realm;
        this.realmPublicKey = realmPublicKey;
        this.authServerUrl = authServerUrl;
        this.sslRequired = sslRequired;
        this.resource = resource;
        this.credentials = credentials;
    }

    @JsonIgnore
    public String getSecret() {
        return credentials.get("secret");
    }
}
