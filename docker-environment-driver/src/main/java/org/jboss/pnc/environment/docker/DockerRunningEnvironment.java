package org.jboss.pnc.environment.docker;

import java.io.InputStream;

import javax.inject.Inject;

import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;

/**
 * Implementation of Docker environment used by DockerEnvironmentDriver
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class DockerRunningEnvironment implements RunningEnvironment {

    private DockerEnvironmentDriver dockerEnvDriver;

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

    public DockerRunningEnvironment(DockerEnvironmentDriver dockerEnvDriver, String id, int jenkinsPort,
            int sshPort) {
        this.dockerEnvDriver = dockerEnvDriver;
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
     * 
     * @return
     */
    public int getSshPort() {
        return sshPort;
    }

    @Override
    public void transferDataToEnvironment(String pathOnHost, InputStream stream)
            throws EnvironmentDriverException {
        dockerEnvDriver.copyFileToContainer(this.sshPort, pathOnHost, null, stream);

    }

    @Override
    public void transferDataToEnvironment(String pathOnHost, String data) throws EnvironmentDriverException {
        dockerEnvDriver.copyFileToContainer(this.sshPort, pathOnHost, data, null);
    }

    @Override
    public void destroyEnvironment() throws EnvironmentDriverException {
        dockerEnvDriver.destroyEnvironment(this.id);
    }

}
