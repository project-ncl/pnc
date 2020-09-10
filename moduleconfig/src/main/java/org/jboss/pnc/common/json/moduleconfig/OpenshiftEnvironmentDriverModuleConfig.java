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
package org.jboss.pnc.common.json.moduleconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jboss.pnc.common.json.moduleconfig.helper.HttpDestinationConfig;
import org.jboss.pnc.common.monitor.PollingMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Configuration for DockerEnvironmentDriver
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 *
 */
@Getter
public class OpenshiftEnvironmentDriverModuleConfig extends EnvironmentDriverModuleConfigBase {

    private static final Logger log = LoggerFactory.getLogger(OpenshiftEnvironmentDriverModuleConfig.class);

    public static final String MODULE_NAME = "openshift-environment-driver";
    private static final int DEFAULT_BUILDER_POD_MEMORY = 4;
    private static final int DEFAULT_CREATION_POD_RETRY = 1;

    private String restEndpointUrl;
    private String buildAgentHost;
    private String buildAgentBindPath;
    private String executorThreadPoolSize;

    private String podNamespace;
    private String restAuthToken;
    private String containerPort;
    private boolean keepBuildAgentInstance;
    private boolean exposeBuildAgentOnPublicUrl;
    private final int builderPodMemory;

    /** How many retries to attempt to get all services fully up and running */
    private int creationPodRetry;
    /** Time how long to wait until all services are fully up and running (in seconds) */
    private int pollingMonitorTimeout;
    /** Interval to wait betweeen subsequent checks of the condition in the PollingMonitor (in seconds) */
    private int pollingMonitorCheckInterval;

    public OpenshiftEnvironmentDriverModuleConfig(
            @JsonProperty("restEndpointUrl") String restEndpointUrl,
            @JsonProperty("buildAgentHost") String buildAgentHost,
            @JsonProperty("imageId") String imageId,
            @JsonProperty("firewallAllowedDestinations") String firewallAllowedDestinations,
            @JsonProperty("allowedHttpOutgoingDestinations") List<HttpDestinationConfig> allowedHttpOutgoingDestinations,
            @JsonProperty("proxyServer") String proxyServer,
            @JsonProperty("proxyPort") String proxyPort,
            @JsonProperty("nonProxyHosts") String nonProxyHosts,
            @JsonProperty("podNamespace") String podNamespace,
            @JsonProperty("buildAgentBindPath") String buildAgentBindPath,
            @JsonProperty("executorThreadPoolSize") String executorThreadPoolSize,
            @JsonProperty("restAuthToken") String restAuthToken,
            @JsonProperty("containerPort") String containerPort,
            @JsonProperty("workingDirectory") String workingDirectory,
            @JsonProperty("disabled") Boolean disabled,
            @JsonProperty("keepBuildAgentInstance") Boolean keepBuildAgentInstance,
            @JsonProperty("exposeBuildAgentOnPublicUrl") Boolean exposeBuildAgentOnPublicUrl,
            @JsonProperty("creationPodRetry") String creationPodRetry,
            @JsonProperty("builderPodMemory") Integer builderPodMemory,
            @JsonProperty("pollingMonitorTimeout") String pollingMonitorTimeout,
            @JsonProperty("pollingMonitorCheckInterval") String pollingMonitorCheckInterval) {
        super(
                imageId,
                firewallAllowedDestinations,
                allowedHttpOutgoingDestinations,
                proxyServer,
                proxyPort,
                nonProxyHosts,
                workingDirectory,
                disabled);

        this.restEndpointUrl = restEndpointUrl;
        this.buildAgentHost = buildAgentHost;
        this.buildAgentBindPath = buildAgentBindPath;
        this.executorThreadPoolSize = executorThreadPoolSize;
        this.podNamespace = podNamespace;
        this.restAuthToken = restAuthToken;
        this.containerPort = containerPort;
        this.keepBuildAgentInstance = keepBuildAgentInstance != null ? keepBuildAgentInstance : false;
        this.exposeBuildAgentOnPublicUrl = exposeBuildAgentOnPublicUrl != null ? exposeBuildAgentOnPublicUrl : false;
        this.builderPodMemory = builderPodMemory == null ? DEFAULT_BUILDER_POD_MEMORY : builderPodMemory;

        this.creationPodRetry = DEFAULT_CREATION_POD_RETRY;
        if (creationPodRetry != null) {
            try {
                this.creationPodRetry = Integer.parseInt(creationPodRetry);
            } catch (NumberFormatException e) {
                log.error(
                        "Couldn't parse the value of creation pod retry from the configuration. Using default ({} retries)",
                        DEFAULT_CREATION_POD_RETRY);
            }
        }

        this.pollingMonitorTimeout = PollingMonitor.DEFAULT_TIMEOUT;
        if (pollingMonitorTimeout != null) {
            try {
                this.pollingMonitorTimeout = Integer.parseInt(pollingMonitorTimeout);
            } catch (NumberFormatException e) {
                log.error(
                        "Couldn't parse the value of polling monitor timeout. Using default ({} seconds)",
                        PollingMonitor.DEFAULT_TIMEOUT);
            }
        }

        this.pollingMonitorCheckInterval = PollingMonitor.DEFAULT_CHECK_INTERVAL;
        if (pollingMonitorCheckInterval != null) {
            try {
                this.pollingMonitorCheckInterval = Integer.parseInt(pollingMonitorCheckInterval);
            } catch (NumberFormatException e) {
                log.error(
                        "Couldn't parse the value of polling monitor check interval. Using default ({} seconds)",
                        PollingMonitor.DEFAULT_CHECK_INTERVAL);
            }
        }

        log.debug("Created new instance {}", toString());
    }

    public String getRestEndpointUrl() {
        return restEndpointUrl;
    }

    public String getBuildAgentHost() {
        return buildAgentHost;
    }

    public String getPncNamespace() {
        return podNamespace;
    }

    public String getRestAuthToken() {
        return restAuthToken;
    }

    public String getContainerPort() {
        return containerPort;
    }

    public String getBuildAgentBindPath() {
        return buildAgentBindPath;
    }

    public String getExecutorThreadPoolSize() {
        return executorThreadPoolSize;
    }

    public boolean getKeepBuildAgentInstance() {
        return keepBuildAgentInstance;
    }

    public boolean getExposeBuildAgentOnPublicUrl() {
        return exposeBuildAgentOnPublicUrl;
    }

    public int getCreationPodRetry() {
        return creationPodRetry;
    }

    public int getPollingMonitorTimeout() {
        return pollingMonitorTimeout;
    }

    public int getPollingMonitorCheckInterval() {
        return pollingMonitorCheckInterval;
    }

    @Override
    public String toString() {
        return "OpenshiftEnvironmentDriverModuleConfig{" + "restEndpointUrl='" + restEndpointUrl + '\'' + ", imageId='"
                + imageId + '\'' + ", firewallAllowedDestinations='" + firewallAllowedDestinations + '\''
                + ", allowedHttpOutgoingDestinations='" + allowedHttpOutgoingDestinations + '\'' + ", proxyServer='"
                + proxyServer + '\'' + ", proxyPort='" + proxyPort + '\'' + ", nonProxyHosts='" + nonProxyHosts + '\''
                + ", podNamespace='" + podNamespace + '\'' + ", buildAgentHost='" + buildAgentHost + '\''
                + ", buildAgentBindPath='" + buildAgentBindPath + '\'' + ", executorThreadPoolSize='"
                + executorThreadPoolSize + '\'' + ", restAuthToken= HIDDEN " + ", containerPort='" + containerPort
                + '\'' + ", disabled='" + disabled + '\'' + ", keepBuildAgentInstance='" + keepBuildAgentInstance + '\''
                + ", exposeBuildAgentOnPublicUrl='" + exposeBuildAgentOnPublicUrl + '\'' + ", creationPodRetry="
                + creationPodRetry + ", pollingMonitorTimeout=" + pollingMonitorTimeout
                + ", pollingMonitorCheckInterval=" + pollingMonitorCheckInterval + '}';
    }

}
