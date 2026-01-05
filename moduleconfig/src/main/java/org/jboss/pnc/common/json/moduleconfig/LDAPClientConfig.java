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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Represents the JSON config for LDAP client authentication
 */
@Getter
@Slf4j
public class LDAPClientConfig {

    private String username;
    private String secretFileLocation;

    // Read the secret from the secretFileLocation
    @JsonIgnore
    private String secret;

    @JsonCreator
    public LDAPClientConfig(
            @JsonProperty("username") String username,
            @JsonProperty("secretFileLocation") String secretFileLocation) {
        this.username = username;
        this.secretFileLocation = secretFileLocation;
    }

    @JsonIgnore
    public String getSecret() {
        try {

            // memoize the secret
            if (secret == null) {
                secret = IoUtils.readFileAsString(new File(secretFileLocation)).trim();
            }

            return secret;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    public String getBase64UsernameAndPassword() {
        String usernameAndPassword = username + ":" + getSecret();
        return Base64.getEncoder().encodeToString(usernameAndPassword.getBytes(StandardCharsets.UTF_8));
    }
}
