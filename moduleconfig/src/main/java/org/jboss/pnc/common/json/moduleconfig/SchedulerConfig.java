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
package org.jboss.pnc.common.json.moduleconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jboss.pnc.common.json.AbstractModuleConfig;

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
