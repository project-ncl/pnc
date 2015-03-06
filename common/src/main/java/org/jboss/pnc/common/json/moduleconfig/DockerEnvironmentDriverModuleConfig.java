package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for DockerEnvironmentDriver
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class DockerEnvironmentDriverModuleConfig extends AbstractModuleConfig {

    private String ip;

    private String inContainerUser;

    private String inContainerUserPassword;

    private String dockerImageId;

    public DockerEnvironmentDriverModuleConfig(@JsonProperty("ip") String ip,
            @JsonProperty("inContainerUser") String inContainerUser,
            @JsonProperty("inContainerUserPassword") String inContainerUserPassword,
            @JsonProperty("dockerImageId") String dockerImageId) {
        this.ip = ip;
        this.inContainerUser = inContainerUser;
        this.inContainerUserPassword = inContainerUserPassword;
        this.dockerImageId = dockerImageId;
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
                + "inContainerUserPassword=HIDDEN]";
    }

}
