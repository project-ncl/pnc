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

import org.jboss.pnc.common.json.AbstractModuleConfig;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class EnvironmentDriverModuleConfigBase extends AbstractModuleConfig {

    /**
     * Image to use for build container
     */
    @Deprecated //moving to BuildConfiguration
    protected String imageId;

    /**
     * List of allowed destinations by firewall in Docker container. <br /> Format: \<IPv4>:\<Port>(,\<IPv4>:\<Port>)+
     * You can set it to "all" and network isolation will be skipped, in case of not setting it up at all
     * all network traffic will be dropped
     */
    protected String firewallAllowedDestinations;

    /**
     * Persistent http proxy hostname
     */
    protected String proxyServer;

    /**
     * Persistent http proxy port
     */
    protected String proxyPort;

    /**
     * List of hosts that are not proxied.
     */
    protected String nonProxyHosts;

    /**
     * Working directory on the remote environment
     */
    private String workingDirectory;
    protected boolean disabled;

    /**
     * Time how long to wait until all services are fully up and running (in seconds)
     */
    private final int buildEnvironmentReadyTimeoutSeconds;

    /**
     * Interval between two checks if the services are fully up and running (in second)
     */
    private final int buildEnvironmentReadyCheckIntervalSeconds;

    public EnvironmentDriverModuleConfigBase(
            String imageId,
            String firewallAllowedDestinations,
            String proxyServer,
            String proxyPort,
            String nonProxyHosts,
            String workingDirectory,
            boolean disabled,
            String buildEnvironmentReadyTimeoutSeconds,
            String buildEnvironmentReadyCheckIntervalSeconds) {

        this.imageId = imageId;
        this.firewallAllowedDestinations = firewallAllowedDestinations;
        this.proxyServer = proxyServer;
        this.proxyPort = proxyPort;
        this.nonProxyHosts = nonProxyHosts;
        this.workingDirectory = workingDirectory;
        this.disabled = disabled;
        this.buildEnvironmentReadyTimeoutSeconds = Integer.valueOf(buildEnvironmentReadyTimeoutSeconds);
        this.buildEnvironmentReadyCheckIntervalSeconds = Integer.valueOf(buildEnvironmentReadyCheckIntervalSeconds);
    }

    public String getImageId() {
        return imageId;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public String getNonProxyHosts() {
        return nonProxyHosts;
    }
    public String getFirewallAllowedDestinations() {
        return firewallAllowedDestinations;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public int getBuildEnvironmentReadyTimeoutSeconds() {
        return buildEnvironmentReadyTimeoutSeconds;
    }

    public int getBuildEnvironmentReadyCheckIntervalSeconds() {
        return buildEnvironmentReadyCheckIntervalSeconds;
    }
}
