/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
