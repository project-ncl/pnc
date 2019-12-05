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
package org.jboss.pnc.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jboss.pnc.common.logging.MDCUtils;

import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Data
@AllArgsConstructor
public class Configuration {

    private final String host;
    private final Integer port;

    private final BasicAuth basicAuth;

    private final String bearerToken;

    private final String protocol;

    /**
     * Fetch page size
     */
    private final int pageSize;

    /**
     * Define which values from the logging MDC are added as headers to the request.
     * A key is a MDC key.
     * A value is a header name
     */
    private Map<String, String> mdcToHeadersMappings;

    private Configuration(ConfigurationBuilder builder) {
        host = builder.host;
        port = builder.port;
        basicAuth = builder.basicAuth;
        bearerToken = builder.bearerToken;
        protocol = builder.protocol;
        pageSize = builder.pageSize;
        mdcToHeadersMappings = builder.mdcToHeadersMappings;
    }

    public static ConfigurationBuilder builder() {
        return new ConfigurationBuilder();
    }

    @Getter
    public static class BasicAuth {
        private String username;
        private String password;

        public BasicAuth(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static final class ConfigurationBuilder {

        private String host;

        private Integer port;

        private BasicAuth basicAuth;

        private String bearerToken;

        private String protocol;

        private int pageSize;

        private Map<String, String> mdcToHeadersMappings;

        private ConfigurationBuilder() {
        }

        public ConfigurationBuilder host(String host) {
            this.host = host;
            return this;
        }

        public ConfigurationBuilder port(Integer port) {
            this.port = port;
            return this;
        }

        public ConfigurationBuilder basicAuth(BasicAuth basicAuth) {
            this.basicAuth = basicAuth;
            return this;
        }

        public ConfigurationBuilder bearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        public ConfigurationBuilder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public ConfigurationBuilder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public ConfigurationBuilder mdcToHeadersMappings(Map<String, String> mdcToHeadersMappings) {
            this.mdcToHeadersMappings = mdcToHeadersMappings;
            return this;
        }

        public ConfigurationBuilder addDefaultMdcToHeadersMappings() {
            this.mdcToHeadersMappings = MDCUtils.getMDCToHeaderMappings();
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }
}
