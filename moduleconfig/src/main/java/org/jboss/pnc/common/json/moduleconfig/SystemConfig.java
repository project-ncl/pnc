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
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "system-config";

    private final static Logger log = LoggerFactory.getLogger(SystemConfig.class);

    private String buildDriverId;

    private String buildSchedulerId;

    private final String authenticationProviderId;

    /**
     * Number of threads that are used to run executor operations (setting up the repos, configuring the build,
     * triggering the build, collecting the results)
     */
    private String executorThreadPoolSize;

    /**
     * Number of threads that are used to run the build and listen for completion.
     */
    private String builderThreadPoolSize;

    /**
     * number of threads that are taking a build task to be build and starting the building process (their job finishes
     * at starting bpm build, then they go back to grab the next build task)
     */
    private int coordinatorThreadPoolSize;

    private String brewTagPattern;

    /**
     * maximum number of build tasks processed at a time (build tasks that are in progress, regardless of whether they
     * are starting bpm process, being build by executor, etc)
     */
    private int coordinatorMaxConcurrentBuilds;

    private KeycloakClientConfig keycloakServiceAccountConfig;

    private long serviceTokenRefreshIfExpiresInSeconds;

    /**
     * Temporary builds life span set as number of days. After the expiration the temporary builds gets deleted.
     * Defaults to 14 days.
     */
    private int temporaryBuildsLifeSpan;

    private String messageSenderId;

    private int messagingInternalQueueSize;

    public SystemConfig(
            @JsonProperty("buildDriverId") String buildDriverId,
            @JsonProperty("buildSchedulerId") String buildSchedulerId,
            @JsonProperty("authenticationProviderId") String authenticationProviderId,
            @JsonProperty("executorThreadPoolSize") String executorThreadPoolSize,
            @JsonProperty("builderThreadPoolSize") String builderThreadPoolSize,
            @JsonProperty("coordinatorThreadPoolSize") String coordinatorThreadPoolSize,
            @JsonProperty("brewTagPattern") String brewTagPattern,
            @JsonProperty("coordinatorMaxConcurrentBuilds") String coordinatorMaxConcurrentBuilds,
            @JsonProperty("keycloakServiceAccountConfig") KeycloakClientConfig keycloakServiceAccountConfig,
            @JsonProperty("serviceTokenRefreshIfExpiresInSeconds") String serviceTokenRefreshIfExpiresInSeconds,
            @JsonProperty("temporaryBuildsLifeSpan") String temporaryBuildsLifeSpan,
            @JsonProperty("messageSenderId") String messageSenderId,
            @JsonProperty("messagingInternalQueueSize") String messagingInternalQueueSize) {
        this.buildDriverId = buildDriverId;
        this.buildSchedulerId = buildSchedulerId;
        this.authenticationProviderId = authenticationProviderId;
        this.executorThreadPoolSize = executorThreadPoolSize;
        this.builderThreadPoolSize = builderThreadPoolSize;
        this.coordinatorThreadPoolSize = toIntWithDefault("coordinatorThreadPoolSize", coordinatorThreadPoolSize, 1);
        this.coordinatorMaxConcurrentBuilds = toIntWithDefault(
                "coordinatorMaxConcurrentBuilds",
                coordinatorMaxConcurrentBuilds,
                10);
        this.brewTagPattern = brewTagPattern;
        this.keycloakServiceAccountConfig = keycloakServiceAccountConfig;
        // 24 hours
        this.serviceTokenRefreshIfExpiresInSeconds = toIntWithDefault(
                "serviceTokenRefreshIfExpiresInSeconds",
                serviceTokenRefreshIfExpiresInSeconds,
                86400);
        this.temporaryBuildsLifeSpan = toIntWithDefault("temporaryBuildsLifeSpan", temporaryBuildsLifeSpan, 14);
        this.messageSenderId = messageSenderId;
        this.messagingInternalQueueSize = toIntWithDefault(
                "messagingInternalQueueSize",
                messagingInternalQueueSize,
                1000);
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

    public String getBuilderThreadPoolSize() {
        return builderThreadPoolSize;
    }

    public void setBuilderThreadPoolSize(String builderThreadPoolSize) {
        this.builderThreadPoolSize = builderThreadPoolSize;
    }

    public int getCoordinatorThreadPoolSize() {
        return coordinatorThreadPoolSize;
    }

    public int getCoordinatorMaxConcurrentBuilds() {
        return coordinatorMaxConcurrentBuilds;
    }

    public String getBrewTagPattern() {
        return brewTagPattern;
    }

    public int getTemporaryBuildsLifeSpan() {
        return temporaryBuildsLifeSpan;
    }

    public KeycloakClientConfig getKeycloakServiceAccountConfig() {
        return keycloakServiceAccountConfig;
    }

    public long getServiceTokenRefreshIfExpiresInSeconds() {
        return serviceTokenRefreshIfExpiresInSeconds;
    }

    public String getMessageSenderId() {
        return messageSenderId;
    }

    public String getAuthenticationProviderId() {
        return authenticationProviderId;
    }

    public int getMessagingInternalQueueSize() {
        return messagingInternalQueueSize;
    }

    private int toIntWithDefault(String fieldName, String numberAsString, int defaultValue) {
        int result = defaultValue;
        if (numberAsString == null) {
            log.warn("Value in field: " + fieldName + " not set. Will use default value: {}", defaultValue);
        } else {
            try {
                result = Integer.parseInt(numberAsString);
            } catch (NumberFormatException nfe) {
                log.warn(
                        "Invalid value in field: " + fieldName
                                + ". Expected an integer, got: {}. Will use default value: {}",
                        numberAsString,
                        defaultValue,
                        nfe);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "SystemConfig [" + (buildDriverId != null ? "buildDriverId=" + buildDriverId + ", " : "")
                + (buildSchedulerId != null ? "buildSchedulerId=" + buildSchedulerId + ", " : "")
                + (authenticationProviderId != null ? "authenticationProviderId=" + authenticationProviderId + ", "
                        : "")
                + (executorThreadPoolSize != null ? "executorThreadPoolSize=" + executorThreadPoolSize + ", " : "")
                + (builderThreadPoolSize != null ? "builderThreadPoolSize=" + builderThreadPoolSize : "") + "]";
    }

}
