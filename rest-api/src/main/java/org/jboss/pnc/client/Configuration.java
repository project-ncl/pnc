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
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.jboss.pnc.common.logging.MDCUtils;

import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Data
@AllArgsConstructor
@Builder
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

        private Map<String, String> mdcToHeadersMappings;

        public ConfigurationBuilder addDefaultMdcToHeadersMappings() {
            this.mdcToHeadersMappings = MDCUtils.getMDCToHeaderMappings();
            return this;
        }
    }
}
