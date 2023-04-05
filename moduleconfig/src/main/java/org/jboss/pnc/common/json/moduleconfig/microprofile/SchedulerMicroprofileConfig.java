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
package org.jboss.pnc.common.json.moduleconfig.microprofile;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.pnc.common.json.moduleconfig.SchedulerConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        if (System.getProperty(SCHEDULER_URL_KEY) != null) {
            return System.getProperty(SCHEDULER_URL_KEY);
        } else {
            return properties.get(key);
        }
    }

    @Override
    public String getName() {
        return SchedulerConfig.MODULE_NAME;
    }
}
