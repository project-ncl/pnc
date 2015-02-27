package org.jboss.pnc.environment.docker;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mapping of IP and port to unmarschal JSON
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
class HostPortMapping {

    @JsonProperty(value = "HostIp")
    private String hostIp;

    @JsonProperty(value = "HostPort")
    private String hostPort;

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

}