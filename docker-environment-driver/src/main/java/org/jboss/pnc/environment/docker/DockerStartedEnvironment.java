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

import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Represents environments during initialization process
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class DockerStartedEnvironment implements StartedEnvironment {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());


    /** Time how long the driver waits until all services are fully up and running (in seconds) */
    private static final int MAX_CONTAINER_LOADING_TIME = 60;

    /** Interval between two checks if the services are fully up and running (in second) */
    private static final int CHECK_INTERVAL = 3;

    private final DockerRunningEnvironment preparedRunningEnvironment;
    
    private final String id;
    private final PullingMonitor pullingMonitor;

    public DockerStartedEnvironment(DockerEnvironmentDriver dockerEnvDriver, PullingMonitor pullingMonitor,
            RepositorySession repositorySession, String id, int jenkinsPort, int sshPort, String containerUrl,
            Path workingDirectory) {
        this.pullingMonitor = pullingMonitor;
        this.preparedRunningEnvironment = new DockerRunningEnvironment(dockerEnvDriver, repositorySession,
                id, jenkinsPort, sshPort, containerUrl, workingDirectory);
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

        pullingMonitor.monitor(
                onEnvironmentInitComplete,
                onEnvironmentInitError,
                () -> checkServices(preparedRunningEnvironment.getBuildAgentUrl()),
                CHECK_INTERVAL, MAX_CONTAINER_LOADING_TIME, TimeUnit.SECONDS);
        
        logger.info("Waiting to init services in docker container. ID: " + preparedRunningEnvironment.getId());
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Wait until all services in container are fully up and running
     *
     * @param jenkinsUrl to jenkins server in container
     * @throws EnvironmentDriverException Thrown if the services are not initialized in time specified by variable
     *         MAX_CONTAINER_LOADING_TIME
     * @return True if all services are fully up and running otherwise false
     */
    private boolean checkServices(String jenkinsUrl) {
        try {
            HttpUtils.testResponseHttpCode(200, jenkinsUrl);
            return true;
        } catch (Exception e) {
            // Jenkins is not fully up
            logger.debug("Container services are not fully up and running. Waiting ...");
            return false;
        }
    }


}
