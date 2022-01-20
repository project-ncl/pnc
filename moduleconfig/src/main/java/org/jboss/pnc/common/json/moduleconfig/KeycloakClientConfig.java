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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.common.util.IoUtils;

import javax.ws.rs.DefaultValue;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@Slf4j
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
        String secretPath = credentials.get("secretFileLocation");
        try {
            return IoUtils.readFileAsString(new File(secretPath)).trim();
        } catch (IOException e) {
            log.error("Error while getting secret token", e);
            throw new RuntimeException(e);
        }
    }
}
