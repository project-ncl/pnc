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
package org.jboss.pnc.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.jboss.pnc.common.logging.MDCUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Jakub Bartecek
 */
@Data
@AllArgsConstructor
@Builder
public class Configuration {

    private final String host;

    private final Integer port;

    private final BasicAuth basicAuth;

    private final String bearerToken;

    /**
     * Allows to provide a supplied method, which can be repeatedly used by client to generate new bearer token. If a
     * request fails with 401 Unauthorized, client tries to generate new token and re-run the requests.
     *
     * This is very useful when using long running applications. Users doesn't need to care about token expiration.
     *
     */
    private final Supplier<String> bearerTokenSupplier;

    private final String protocol;

    /**
     * Fetch page size
     */
    private final int pageSize;

    /**
     * Define which values from the logging MDC are added as headers to the request. A key is a MDC key. A value is a
     * header name
     */
    private Map<String, String> mdcToHeadersMappings;

    @Getter
    public static class BasicAuth {
        private String username;
        private String password;

        public BasicAuth(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getBase64Credentials() {
            return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        }
    }

    public static final class ConfigurationBuilder {

        private Map<String, String> mdcToHeadersMappings = new HashMap<>();

        public ConfigurationBuilder addDefaultMdcToHeadersMappings() {
            this.mdcToHeadersMappings = new HashMap<>(MDCUtils.HEADER_KEY_MAPPING);
            return this;
        }
    }
}
