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

import org.jboss.pnc.common.json.moduleconfig.helper.HttpDestinationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import lombok.Getter;

/**
 * Configuration for DockerEnvironmentDriver
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 *
 */
@Getter
public class OpenshiftEnvironmentDriverModuleConfig extends EnvironmentDriverModuleConfigBase {

    private static final Logger log = LoggerFactory.getLogger(OpenshiftEnvironmentDriverModuleConfig.class);

    public static String MODULE_NAME = "openshift-environment-driver";
    private static final int DEFAULT_BUILDER_POD_MEMORY = 4;

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

    private String creationPodRetry;

    public OpenshiftEnvironmentDriverModuleConfig(@JsonProperty("restEndpointUrl") String restEndpointUrl,
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
                                                  @JsonProperty("builderPodMemory") Integer builderPodMemory) {
        super(imageId, firewallAllowedDestinations, allowedHttpOutgoingDestinations, proxyServer, proxyPort, nonProxyHosts,workingDirectory, disabled);

        this.restEndpointUrl = restEndpointUrl;
        this.buildAgentHost = buildAgentHost;
        this.buildAgentBindPath = buildAgentBindPath;
        this.executorThreadPoolSize = executorThreadPoolSize;
        this.podNamespace = podNamespace;
        this.restAuthToken = restAuthToken;
        this.containerPort = containerPort;
        this.keepBuildAgentInstance = keepBuildAgentInstance != null ? keepBuildAgentInstance: false;
        this.exposeBuildAgentOnPublicUrl = exposeBuildAgentOnPublicUrl != null ? exposeBuildAgentOnPublicUrl: false;
        this.creationPodRetry = creationPodRetry;
        this.builderPodMemory = builderPodMemory == null ? DEFAULT_BUILDER_POD_MEMORY : builderPodMemory;

        log.debug("Created new instance {}", toString());
    }

    public String getPncNamespace() {
        return podNamespace;
    }

    public boolean getKeepBuildAgentInstance() {
        return keepBuildAgentInstance;
    }

    public boolean getExposeBuildAgentOnPublicUrl() {
        return exposeBuildAgentOnPublicUrl;
    }

    @Override
    public String toString() {
        return "OpenshiftEnvironmentDriverModuleConfig{" +
                "restEndpointUrl='" + restEndpointUrl + '\'' +
                ", imageId='" + imageId + '\'' +
                ", firewallAllowedDestinations='" + firewallAllowedDestinations + '\'' +
                ", allowedHttpOutgoingDestinations='" + allowedHttpOutgoingDestinations + '\'' +
                ", proxyServer='" + proxyServer + '\'' +
                ", proxyPort='" + proxyPort + '\'' +
                ", nonProxyHosts='" + nonProxyHosts + '\'' +
                ", podNamespace='" + podNamespace + '\'' +
                ", buildAgentHost='" + buildAgentHost + '\'' +
                ", buildAgentBindPath='" + buildAgentBindPath + '\'' +
                ", executorThreadPoolSize='" + executorThreadPoolSize + '\'' +
                ", restAuthToken= HIDDEN " +
                ", containerPort='" + containerPort + '\'' +
                ", disabled='" + disabled + '\'' +
                ", keepBuildAgentInstance='" + keepBuildAgentInstance + '\'' +
                ", exposeBuildAgentOnPublicUrl='" + exposeBuildAgentOnPublicUrl + '\'' +
                ", creationPodRetry='" + creationPodRetry + '\'' +
                '}';
    }

}
