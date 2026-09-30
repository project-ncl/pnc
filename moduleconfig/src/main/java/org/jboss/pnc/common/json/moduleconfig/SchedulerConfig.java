/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class SchedulerConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "scheduler-config";

    private final String schedulerBaseUrl;
    private final String connectTimeout;
    private final String readTimeout;
    private final String followRedirects;
    private final String maxScheduleRetries;
    private final String queueNameForBuilds;

    public SchedulerConfig(
            @JsonProperty("schedulerBaseUrl") String schedulerBaseUrl,
            @JsonProperty("connectTimeout") String connectTimeout,
            @JsonProperty("readTimeout") String readTimeout,
            @JsonProperty("followRedirects") String followRedirects,
            @JsonProperty("maxScheduleRetries") String maxScheduleRetries,
            @JsonProperty("queueNameForBuilds") String queueNameForBuilds) {
        this.schedulerBaseUrl = schedulerBaseUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.followRedirects = followRedirects;
        this.maxScheduleRetries = maxScheduleRetries;
        this.queueNameForBuilds = queueNameForBuilds;
    }
}
