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
package org.jboss.pnc.environment.docker;

import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import java.nio.file.Path;

/**
 * Implementation of Docker environment used by DockerEnvironmentDriver
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class DockerRunningEnvironment implements RunningEnvironment {

    private DockerEnvironmentDriver dockerEnvDriver;

    /**
     * ID of environment
     */
    private final String id;

    /**
     * Port to connect to Jenkins UI
     */
    private final int jenkinsPort;

    /**
     * Port to SSH to running environment
     */
    private final int sshPort;

    private final String containerUrl;

    private final RepositorySession repositorySession;

    private final Path workingDirectory;

    public DockerRunningEnvironment(DockerEnvironmentDriver dockerEnvDriver, RepositorySession repositorySession, String id, int jenkinsPort, int sshPort, String containerUrl,
            Path workingDirectory) {
        this.repositorySession = repositorySession;
        this.dockerEnvDriver = dockerEnvDriver;
        this.id = id;
        this.jenkinsPort = jenkinsPort;
        this.sshPort = sshPort;
        this.containerUrl = containerUrl;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getJenkinsPort() {
        return jenkinsPort;
    }

    @Override
    public String getJenkinsUrl() {
        return containerUrl + ":" + jenkinsPort;
    }

    @Override
    public String getInternalBuildAgentUrl() {
        return getJenkinsUrl();
    }

    @Override
    public RepositorySession getRepositorySession() {
        return repositorySession;
    }

    @Override
    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * SSH port on which is container accessible
     * 
     * @return Opened container SSH port
     */
    public int getSshPort() {
        return sshPort;
    }

    @Override
    public void destroyEnvironment() throws EnvironmentDriverException {
        dockerEnvDriver.destroyEnvironment(this.id);
    }

}
