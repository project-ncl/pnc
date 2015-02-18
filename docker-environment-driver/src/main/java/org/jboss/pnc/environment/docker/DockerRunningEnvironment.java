package org.jboss.pnc.environment.docker;

import org.jboss.pnc.spi.environment.RunningEnvironment;

/**
 * Implementation of Docker environment used by DockerEnvironmentDriver
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class DockerRunningEnvironment implements RunningEnvironment {

    /**
     * ID of environment
     */
    private final String id;

    /**
     * @return Port to connect to Jenkins UI
     */
    private final int jenkinsPort;

    /**
     * Port to SSH to running environment
     */
    private final int sshPort;

    public DockerRunningEnvironment(String id, int jenkinsPort, int sshPort) {
        super();
        this.id = id;
        this.jenkinsPort = jenkinsPort;
        this.sshPort = sshPort;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getJenkinsPort() {
        return jenkinsPort;
    }

    /**
     * SSH port on which is container accessible
     * @return
     */
    public int getSshPort() {
        return sshPort;
    }

}
