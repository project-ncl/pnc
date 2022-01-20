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
package org.jboss.pnc.common.json.moduleconfig.helper;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HttpDestinationConfig {
    private String url;
    private String allowedMethods;

    public HttpDestinationConfig(
            @JsonProperty("url") String url,
            @JsonProperty("allowedMethods") String allowedMethods) {
        this.url = url;
        this.allowedMethods = allowedMethods;
    }

    public String getUrl() {
        return url;
    }

    public String getAllowedMethods() {
        return allowedMethods;
    }

    @Override
    public String toString() {
        return "HttpDestinationConfig{" + "url='" + url + '\'' + ", allowedMethods='" + allowedMethods + '\'' + '}';
    }
}
