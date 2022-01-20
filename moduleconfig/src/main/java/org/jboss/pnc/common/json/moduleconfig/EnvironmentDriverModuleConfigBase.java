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

import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.helper.HttpDestinationConfig;

import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class EnvironmentDriverModuleConfigBase extends AbstractModuleConfig {

    /**
     * Image to use for build container
     */
    @Deprecated // moving to BuildConfiguration
    protected String imageId;

    /**
     * List of allowed destinations by firewall in Docker container. <br />
     * Format: \<IPv4>:\<Port>(,\<IPv4>:\<Port>)+ You can set it to "all" and network isolation will be skipped, in case
     * of not setting it up at all all network traffic will be dropped
     */
    protected String firewallAllowedDestinations;

    /**
     * List of Http destinations; The Http Destination needs to specify the url, port (optional), and the allowed Http
     * method to allow.
     *
     * If you want to specify all the Http methods, specify the destination in the 'firewallAllowedDestinations' section
     * instead.
     *
     * Format: [{"url": "\<url\>", "allowedMethods": "PUT,POST"}, ...]
     */
    protected List<HttpDestinationConfig> allowedHttpOutgoingDestinations;

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

    public EnvironmentDriverModuleConfigBase(
            String imageId,
            String firewallAllowedDestinations,
            List<HttpDestinationConfig> allowedHttpOutgoingDestinations,
            String proxyServer,
            String proxyPort,
            String nonProxyHosts,
            String workingDirectory,
            boolean disabled) {

        this.imageId = imageId;
        this.firewallAllowedDestinations = firewallAllowedDestinations;
        this.allowedHttpOutgoingDestinations = allowedHttpOutgoingDestinations;
        this.proxyServer = proxyServer;
        this.proxyPort = proxyPort;
        this.nonProxyHosts = nonProxyHosts;
        this.workingDirectory = workingDirectory;
        this.disabled = disabled;
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

    public List<HttpDestinationConfig> getAllowedHttpOutgoingDestinations() {
        return allowedHttpOutgoingDestinations;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isDisabled() {
        return disabled;
    }

}
