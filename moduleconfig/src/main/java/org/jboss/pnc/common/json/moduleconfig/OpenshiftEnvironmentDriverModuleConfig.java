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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for DockerEnvironmentDriver
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 *
 */
public class OpenshiftEnvironmentDriverModuleConfig extends EnvironmentDriverModuleConfigBase {

    private static final Logger log = LoggerFactory.getLogger(OpenshiftEnvironmentDriverModuleConfig.class);

    public static String MODULE_NAME = "openshift-environment-driver";

    private String restEndpointUrl;
    private String buildAgentBindPath;

    private String podNamespace;
    private String restAuthToken;
    private String containerPort;

    public OpenshiftEnvironmentDriverModuleConfig(@JsonProperty("restEndpointUrl") String restEndpointUrl,
                                                  @JsonProperty("imageId") String imageId,
                                                  @JsonProperty("firewallAllowedDestinations") String firewallAllowedDestinations,
                                                  @JsonProperty("proxyServer") String proxyServer,
                                                  @JsonProperty("proxyPort") String proxyPort,
                                                  @JsonProperty("podNamespace") String podNamespace,
                                                  @JsonProperty("buildAgentBindPath") String buildAgentBindPath,
                                                  @JsonProperty("restAuthToken") String restAuthToken,
                                                  @JsonProperty("containerPort") String containerPort,
                                                  @JsonProperty("workingDirectory") String workingDirectory,
                                                  @JsonProperty("disabled") Boolean disabled) {
        super(imageId, firewallAllowedDestinations, proxyServer, proxyPort, workingDirectory, disabled);

        this.restEndpointUrl = restEndpointUrl;
        this.buildAgentBindPath = buildAgentBindPath;
        this.podNamespace = podNamespace;
        this.restAuthToken = restAuthToken;
        this.containerPort = containerPort;

        log.debug("Created new instance {}", toString());
    }

    public String getRestEndpointUrl() {
        return restEndpointUrl;
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

    @Override
    public String toString() {
        return "OpenshiftEnvironmentDriverModuleConfig{" +
                "restEndpointUrl='" + restEndpointUrl + '\'' +
                ", imageId='" + imageId + '\'' +
                ", firewallAllowedDestinations='" + firewallAllowedDestinations + '\'' +
                ", proxyServer='" + proxyServer + '\'' +
                ", proxyPort='" + proxyPort + '\'' +
                ", podNamespace='" + podNamespace + '\'' +
                ", buildAgentBindPath='" + buildAgentBindPath + '\'' +
                ", restAuthToken= HIDDEN " +
                ", containerPort='" + containerPort + '\'' +
                ", disabled='" + disabled + '\'' +
                '}';
    }
}
