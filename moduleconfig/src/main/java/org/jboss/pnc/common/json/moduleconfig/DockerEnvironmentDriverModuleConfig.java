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

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Configuration for DockerEnvironmentDriver
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class DockerEnvironmentDriverModuleConfig extends EnvironmentDriverModuleConfigBase {

    private static final Logger log = LoggerFactory.getLogger(DockerEnvironmentDriverModuleConfig.class);

    public static String MODULE_NAME = "docker-environment-driver";

    private String ip;

    private String inContainerUser;

    private String inContainerUserPassword;

    private static final Path workingDirectory = FileSystems.getDefault().getPath("/tmp");

    private String firewallAllowedDestinations;
    
    private String proxyServer;
    
    private String proxyPort;

    private String nonProxyHosts;

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

    public DockerEnvironmentDriverModuleConfig(@JsonProperty("ip") String ip,
            @JsonProperty("inContainerUser") String inContainerUser,
            @JsonProperty("inContainerUserPassword") String inContainerUserPassword,
            @JsonProperty("dockerImageId") String dockerImageId,
            @JsonProperty("firewallAllowedDestinations") String firewallAllowedDestinations, 
            @JsonProperty("proxyServer") String proxyServer, 
            @JsonProperty("proxyPort") String proxyPort,
            @JsonProperty("nonProxyHosts") String nonProxyHosts,
            @JsonProperty("disabled") boolean disabled) {

        super(dockerImageId, firewallAllowedDestinations, proxyServer, proxyPort, workingDirectory.toString(), disabled);

        this.ip = ip;
        this.inContainerUser = inContainerUser;
        this.inContainerUserPassword = inContainerUserPassword;
        this.imageId = dockerImageId;
        this.proxyServer = proxyServer;
        this.proxyPort = proxyPort;
        this.nonProxyHosts = nonProxyHosts;
        this.firewallAllowedDestinations = firewallAllowedDestinations;

        log.debug("Created new instance {}", toString());
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getInContainerUser() {
        return inContainerUser;
    }

    public String getInContainerUserPassword() {
        return inContainerUserPassword;
    }

    @Override
    public String toString() {
        return "DockerEnvironmentDriverModuleConfig ["
                + (ip != null ? "ip=" + ip + ", " : "")
                + (inContainerUser != null ? "inContainerUser=" + inContainerUser + ", " : "")
                + (imageId != null ? "dockerImageId=" + imageId + ", " : "")
                + (firewallAllowedDestinations != null ? "firewallAllowedDestinations="
                        + firewallAllowedDestinations + ", " : "")
                + (proxyServer != null ? "proxyServer=" + proxyServer + ", " : "")
                + (proxyPort != null ? "proxyPort=" + proxyPort + ", " : "")
                + (proxyPort != null ? "nonProxyHosts=" + nonProxyHosts + ", " : "")
                + "inContainerUserPassword=HIDDEN]";
    }

}
