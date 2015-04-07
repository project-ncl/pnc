package org.jboss.pnc.environment.docker;

import org.jboss.logging.Logger;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import javax.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

/**
 * Represents environments during initialization process
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class DockerStartedEnvironment implements StartedEnvironment {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    @Inject
    private DockerInitializationMonitor dockerInitMonitor;
    
    private DockerRunningEnvironment preparedRunningEnvironment;

    public DockerStartedEnvironment(DockerEnvironmentDriver dockerEnvDriver,
            RepositorySession repositorySession, String id, int jenkinsPort, int sshPort, String containerUrl) {
        this.preparedRunningEnvironment = new DockerRunningEnvironment(dockerEnvDriver, repositorySession,
                id, jenkinsPort, sshPort, containerUrl);
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

}
