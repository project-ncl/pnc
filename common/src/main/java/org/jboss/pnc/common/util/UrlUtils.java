/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.common.util;

import org.jboss.pnc.common.net.GitSCPUrl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public final class UrlUtils {

    private UrlUtils() {
    }

    public static String buildUrl(final String baseUrl, final String... parts) throws MalformedURLException {
        return buildUrl(baseUrl, null, parts);
    }

    public static String buildUrl(final String baseUrl, final Map<String, String> params, final String... parts)
            throws MalformedURLException {
        if (baseUrl == null) {
            throw new InvalidParameterException("Base URL is null. Base URL must be specified!");
        }

        if (parts == null || parts.length < 1) {
            return baseUrl;
        }

        final StringBuilder urlBuilder = new StringBuilder();

        if (parts[0] == null || !parts[0].startsWith(baseUrl)) {
            urlBuilder.append(baseUrl);
        }

        for (String part : parts) {
            if (part == null || part.trim().length() < 1) {
                continue;
            }

            if (part.startsWith("/")) {
                part = part.substring(1);
            }

            if (urlBuilder.length() > 0 && urlBuilder.charAt(urlBuilder.length() - 1) != '/') {
                urlBuilder.append("/");
            }

            urlBuilder.append(part);
        }

        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            boolean first = true;
            for (final Map.Entry<String, String> param : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    urlBuilder.append("&");
                }

                urlBuilder.append(param.getKey()).append("=").append(param.getValue());
            }
        }

        return new URL(urlBuilder.toString()).toExternalForm();
    }

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    public static String keepHostAndPathOnly(String url) {
        // workaround to properly parse url. Without schema and available port, URI.create fails to parse
        if (!url.contains("://")) {
            url = "http://" + url;
        }

        // if the url ends with slash, delete it
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        URI uri = URI.create(url);
        String host = uri.getHost();
        String path = uri.getPath();

        // URI cannot parse git's scp-like syntax (git@github.com:project-ncl/pnc.com). Instead of throwing an
        // exception, it will incorrectly have host equaling null. We have to use custom parser to support these types
        // of urls. (NCL-5990)
        if (host == null) {
            try {
                return GitSCPUrl.parse(StringUtils.stripProtocol(url)).getHostWithPath();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(
                        "Supplied URL:" + url + " is neither regular URI nor in Git SCP-style format.",
                        e);
            }
        }

        return host + (path == null ? "" : path);
    }

    public static String stripProtocolAndPort(String url) {
        // workaround to properly parse url. Without schema and available port, URI.create fails to parse
        if (!url.contains("://")) {
            url = "http://" + url;
        }

        URI uri = URI.create(url);

        String host = uri.getHost();
        String path = uri.getPath();

        String query = uri.getQuery();
        String queryAppend = "";
        if (query != null) {
            queryAppend = "?" + query;
        }

        String fragment = uri.getFragment();
        String fragmentAppend = "";
        if (fragment != null) {
            fragmentAppend = "#" + fragment;
        }

        return (host == null ? "" : host) + (path == null ? "" : path) + queryAppend + fragmentAppend;
    }

    public static String stripProtocol(String url) {
        // workaround to properly parse url. Without schema and available port, URI.create fails to parse
        if (!url.contains("://")) {
            url = "http://" + url;
        }

        URI uri = URI.create(url);

        String userInfo = uri.getUserInfo();

        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();

        String query = uri.getQuery();
        String queryAppend = "";
        if (query != null) {
            queryAppend = "?" + query;
        }

        String fragment = uri.getFragment();
        String fragmentAppend = "";
        if (fragment != null) {
            fragmentAppend = "#" + fragment;
        }

        return (userInfo == null ? "" : userInfo + "@") + (host == null ? "" : host) + (port == -1 ? "" : ":" + port)
                + (path == null ? "" : path) + queryAppend + fragmentAppend;
    }
}
