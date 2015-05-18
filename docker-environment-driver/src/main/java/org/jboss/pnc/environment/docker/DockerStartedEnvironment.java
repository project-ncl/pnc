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

import org.jboss.logging.Logger;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

/**
 * Represents environments during initialization process
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class DockerStartedEnvironment implements StartedEnvironment {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final DockerInitializationMonitor dockerInitMonitor;
    
    private final DockerRunningEnvironment preparedRunningEnvironment;
    
    private final String id;

    public DockerStartedEnvironment(DockerEnvironmentDriver dockerEnvDriver, DockerInitializationMonitor dockerInitMonitor,
            RepositorySession repositorySession, String id, int jenkinsPort, int sshPort, String containerUrl) {
        this.preparedRunningEnvironment = new DockerRunningEnvironment(dockerEnvDriver, repositorySession,
                id, jenkinsPort, sshPort, containerUrl);
        this.dockerInitMonitor = dockerInitMonitor;
        this.id = id;
    }

    @Override
    public void destroyEnvironment() throws EnvironmentDriverException {
        preparedRunningEnvironment.destroyEnvironment();
    }

    @Override
    public void monitorInitialization(Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError) {
        Runnable onEnvironmentInitComplete = () -> {
            logger.info("Docker container successfully initiated. ID: " + preparedRunningEnvironment.getId());
            onComplete.accept(preparedRunningEnvironment);
        };

        Consumer<Exception> onEnvironmentInitError = (e) -> {
            onError.accept(e);
        };
        
        dockerInitMonitor.monitor(onEnvironmentInitComplete, onEnvironmentInitError, preparedRunningEnvironment.getJenkinsUrl());
        
        logger.info("Waiting to init services in docker container. ID: " + preparedRunningEnvironment.getId());
    }

    @Override
    public String getId() {
        return id;
    }

}
