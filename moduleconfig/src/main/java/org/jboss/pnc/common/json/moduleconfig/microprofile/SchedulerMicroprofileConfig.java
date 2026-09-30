/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig.microprofile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.pnc.common.json.moduleconfig.SchedulerConfig;

public class SchedulerMicroprofileConfig implements ConfigSource {

    public static final String SCHEDULER_URL_KEY = "scheduler-client/mp-rest/url";
    private static final String SCHEDULER_CONNECT_TIMEOUT_KEY = "scheduler-client/mp-rest/connectTimeout";
    private static final String SCHEDULER_READ_TIMEOUT_KEY = "scheduler-client/mp-rest/readTimeout";
    private static final String SCHEDULER_FOLLOW_REDIRECTS_KEY = "scheduler-client/mp-rest/followRedirects";
    private static final String SCHEDULER_MAX_RETRIES_BC_KEY = "org.jboss.pnc.remotecoordinator.builder.RemoteBuildCoordinator/buildConfig/Retry/maxRetries";
    private static final String SCHEDULER_MAX_RETRIES_BCA_KEY = "org.jboss.pnc.remotecoordinator.builder.RemoteBuildCoordinator/buildConfigurationAudited/Retry/maxRetries";
    private static final String SCHEDULER_MAX_RETRIES_BS_KEY = "org.jboss.pnc.remotecoordinator.builder.RemoteBuildCoordinator/buildSet/Retry/maxRetries";

    private final Map<String, String> properties;

    public SchedulerMicroprofileConfig(SchedulerConfig config) {
        this.properties = new HashMap<>();
        if (config.getSchedulerBaseUrl() != null) {
            properties.put(SCHEDULER_URL_KEY, config.getSchedulerBaseUrl());
        }
        if (config.getConnectTimeout() != null) {
            properties.put(SCHEDULER_CONNECT_TIMEOUT_KEY, config.getConnectTimeout());
        }
        if (config.getReadTimeout() != null) {
            properties.put(SCHEDULER_READ_TIMEOUT_KEY, config.getReadTimeout());
        }
        if (config.getFollowRedirects() != null) {
            properties.put(SCHEDULER_FOLLOW_REDIRECTS_KEY, config.getFollowRedirects());
        }
        if (config.getMaxScheduleRetries() != null) {
            properties.put(SCHEDULER_MAX_RETRIES_BC_KEY, config.getMaxScheduleRetries());
            properties.put(SCHEDULER_MAX_RETRIES_BCA_KEY, config.getMaxScheduleRetries());
            properties.put(SCHEDULER_MAX_RETRIES_BS_KEY, config.getMaxScheduleRetries());
        }
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getValue(String key) {
        if (SCHEDULER_URL_KEY.equals(key) && System.getProperty(key) != null) {
            return System.getProperty(key);
        }
        return properties.get(key);
    }

    @Override
    public String getName() {
        return SchedulerConfig.MODULE_NAME;
    }
}
