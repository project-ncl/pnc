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

import java.nio.file.Path;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class EnvironmentDriverModuleConfigBase extends AbstractModuleConfig {
    protected String imageId;
    protected String firewallAllowedDestinations;
    protected String proxyServer;
    protected String proxyPort;
    protected String nonProxyHosts;
    private String workingDirectory;
    protected boolean disabled;

    public EnvironmentDriverModuleConfigBase(
            String imageId,
            String firewallAllowedDestinations,
            String proxyServer,
            String proxyPort,
            String nonProxyHosts,
            String workingDirectory,
            boolean disabled) {

        this.imageId = imageId;
        this.firewallAllowedDestinations = firewallAllowedDestinations;
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

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isDisabled() {
        return disabled;
    }

}
