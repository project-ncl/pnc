/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig.microprofile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

public class RepoDriverClientMicroprofileConfig implements ConfigSource {

    public static final String REPO_DRIVER_URL_KEY = "repository-driver/mp-rest/url";
    private static final String REPO_DRIVER_CONNECT_TIMEOUT_KEY = "repository-driver/mp-rest/connectTimeout";
    private static final String REPO_DRIVER_READ_TIMEOUT_KEY = "repository-driver/mp-rest/readTimeout";
    private static final String REPO_DRIVER_FOLLOW_REDIRECTS_KEY = "repository-driver/mp-rest/followRedirects";

    private final Map<String, String> properties;

    public RepoDriverClientMicroprofileConfig(GlobalModuleGroup globalConfig, SystemConfig config) {
        this.properties = new HashMap<>();
        if (globalConfig.getExternalRepositoryDriverUrl() != null) {
            properties.put(REPO_DRIVER_URL_KEY, globalConfig.getExternalRepositoryDriverUrl());
        }
        if (config.getRepoDriverClientConnectTimeout() != null) {
            properties.put(REPO_DRIVER_CONNECT_TIMEOUT_KEY, config.getRepoDriverClientConnectTimeout());
        }
        if (config.getRepoDriverClientReadTimeout() != null) {
            properties.put(REPO_DRIVER_READ_TIMEOUT_KEY, config.getRepoDriverClientReadTimeout());
        }
        if (config.getRepoDriverClientFollowRedirects() != null) {
            properties.put(REPO_DRIVER_FOLLOW_REDIRECTS_KEY, config.getRepoDriverClientFollowRedirects());
        }
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getValue(String key) {
        if (REPO_DRIVER_URL_KEY.equals(key) && System.getProperty(key) != null) {
            return System.getProperty(key);
        }
        return properties.get(key);
    }

    @Override
    public String getName() {
        return "repository-driver-client-config";
    }
}
