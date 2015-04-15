package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for DockerEnvironmentDriver
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class DockerEnvironmentDriverModuleConfig extends AbstractModuleConfig {

    private String ip;

    private String inContainerUser;

    private String inContainerUserPassword;

    private String dockerImageId;

    private String firewallAllowedDestinations;

    public DockerEnvironmentDriverModuleConfig(@JsonProperty("ip") String ip,
            @JsonProperty("inContainerUser") String inContainerUser,
            @JsonProperty("inContainerUserPassword") String inContainerUserPassword,
            @JsonProperty("dockerImageId") String dockerImageId,
            @JsonProperty("firewallAllowedDestinations") String firewallAllowedDestinations) {
        this.ip = ip;
        this.inContainerUser = inContainerUser;
        this.inContainerUserPassword = inContainerUserPassword;
        this.dockerImageId = dockerImageId;
        this.firewallAllowedDestinations = firewallAllowedDestinations;
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

    public void setInContainerUser(String inContainerUser) {
        this.inContainerUser = inContainerUser;
    }

    public String getInContainerUserPassword() {
        return inContainerUserPassword;
    }

    public void setInContainerUserPassword(String inContainerUserPassword) {
        this.inContainerUserPassword = inContainerUserPassword;
    }

    public String getDockerImageId() {
        return dockerImageId;
    }

    public void setDockerImageId(String dockerImageId) {
        this.dockerImageId = dockerImageId;
    }

    @Override
    public String toString() {
        return "DockerEnvironmentDriverModuleConfig ["
                + (ip != null ? "ip=" + ip + ", " : "")
                + (inContainerUser != null ? "inContainerUser=" + inContainerUser + ", " : "")
                + (dockerImageId != null ? "dockerImageId=" + dockerImageId + ", " : "")
                + (firewallAllowedDestinations != null ? "firewallAllowedDestinations="
                        + firewallAllowedDestinations + ", " : "")
                + "inContainerUserPassword=HIDDEN]";
    }

    public String getFirewallAllowedDestinations() {
        return firewallAllowedDestinations;
    }

    public void setFirewallAllowedDestinations(String firewallAllowedDestinations) {
        this.firewallAllowedDestinations = firewallAllowedDestinations;
    }

}
