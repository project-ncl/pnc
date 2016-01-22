/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.common.json.AbstractModuleConfig;

public class SystemConfig extends AbstractModuleConfig {

    private String buildDriverId;
    private String buildSchedulerId;
    private String executorThreadPoolSize;

    public SystemConfig(
            @JsonProperty("buildDriverId") String buildDriverId,
            @JsonProperty("buildSchedulerId") String buildSchedulerId,
            @JsonProperty("executorThreadPoolSize") String executorThreadPoolSize) {
        this.buildDriverId = buildDriverId;
        this.buildSchedulerId = buildSchedulerId;
        this.executorThreadPoolSize = executorThreadPoolSize;
    }

    public String getBuildDriverId() {
        return buildDriverId;
    }

    public String getBuildSchedulerId() {
        return buildSchedulerId;
    }

    public String getExecutorThreadPoolSize() {
        return executorThreadPoolSize;
    }

    @Override
    public String toString() {
        return "SystemConfig{" +
                "buildDriverId='" + buildDriverId + '\'' +
                "buildSchedulerId='" + buildSchedulerId + '\'' +
                "executorThreadPoolSize='" + executorThreadPoolSize + '\'' +
                '}';
    }

}
