package org.jboss.pnc.environment.docker;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.docker.DockerApi;
import org.jclouds.docker.domain.Config;
import org.jclouds.docker.domain.HostConfig;
import org.jclouds.docker.features.RemoteApi;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.http.handlers.BackoffLimitedRetryHandler;
import org.jclouds.io.Payloads;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.SshjSshClient;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HostAndPort;
import com.google.inject.Module;
import com.jcraft.jsch.agentproxy.Connector;

/**
 * Implementation of environment driver, which uses Docker to run environments
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Stateless
public class DockerEnvironmentDriver implements EnvironmentDriver {

    private static final Logger logger =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    /** User in running environment to which we can connect with SSH */
    private static final String CONTAINER_USER = "root";

    /** Password of user to which we can connect with SSH */
    private static final String CONTAINER_PSSWD = "changeme";

    /** ID of docker image */
    private static final String IMAGE_ID = "jbartece/isshd-jenkins";

    /** Connection URL to Docker control port */
    private static final String DOCKER_ENDPOINT = "http://10.3.8.102:2375";

    @Inject
    private Generator generator;

    @Inject
    private ConfigurationBuilder configBuilder;

    private ComputeServiceContext dockerContext;

    private RemoteApi dockerClient;

    /**
     * Prepares connection to Docker daemon
     */
    @PostConstruct
    private void init() {
        dockerContext = ContextBuilder.newBuilder("docker")
                .endpoint(DOCKER_ENDPOINT)
                .credentials(CONTAINER_USER, CONTAINER_PSSWD)
                .modules(ImmutableSet.<Module> of(new SLF4JLoggingModule(),
                        new SshjSshClientModule()))
                .buildView(ComputeServiceContext.class);
        dockerClient = dockerContext.unwrapApi(DockerApi.class).getRemoteApi();
    }

    @Override
    public RunningEnvironment buildEnvironment(Environment buildEnvironment, String dependencyUrl,
            String deployUrl) throws EnvironmentDriverException {
        if (buildEnvironment.getBuildType() != BuildType.DOCKER ||
                buildEnvironment.getOperationalSystem() != OperationalSystem.LINUX)
            throw new UnsupportedOperationException(
                    "DockerEnvironmentDriver currently provides support only for Linux enviroments on Docker.");

        int jenkinsPort = generator.generateJenkinsPort();
        int sshPort = generator.generateSshPort();
        String containerId = generator.generateContainerId();

        try {
            dockerClient.createContainer(
                    containerId,
                    Config.builder()
                            .imageId(IMAGE_ID)
                            .build());

            dockerClient.startContainer(containerId,
                    HostConfig.builder()
                            .portBindings(createPortBinding(jenkinsPort, sshPort))
                            .build());

            copyFileToContainer(sshPort, "/root/.m2/settings.xml",
                    configBuilder.createMavenConfig(dependencyUrl, deployUrl), null);
        } catch (RuntimeException e) {
            throw new EnvironmentDriverException("Cannot create environment.", e);
        }

        logger.info("Created and started Docker container with ID: " + containerId);
        return new DockerRunningEnvironment(containerId, jenkinsPort, sshPort);
    }

    @Override
    public void destroyEnvironment(RunningEnvironment runningEnv) throws EnvironmentDriverException {
        try {
            dockerClient.stopContainer(runningEnv.getId());
            dockerClient.removeContainer(runningEnv.getId());
        } catch (RuntimeException e) {
            throw new EnvironmentDriverException("Cannot destroy environment.", e);
        }
        logger.info("Stopped Docker container with ID: " + runningEnv.getId());
    }

    @Override
    public boolean canBuildEnvironment(Environment environment) {
        if (environment.getBuildType() == BuildType.DOCKER ||
                environment.getOperationalSystem() == OperationalSystem.LINUX)
            return true;
        else
            return false;
    }

    @Override
    public void transferDataToEnvironment(RunningEnvironment runningEnvironment,
            String pathOnHost, String data) throws EnvironmentDriverException {
        if (!(runningEnvironment instanceof DockerRunningEnvironment))
            throw new EnvironmentDriverException(
                    "DockerEnvironmentDriver cannot manage non-Docker environments. " + runningEnvironment);

        DockerRunningEnvironment dockerEnv = (DockerRunningEnvironment) runningEnvironment;
        copyFileToContainer(dockerEnv.getSshPort(), pathOnHost, data, null);
    }

    @Override
    public void transferDataToEnvironment(RunningEnvironment runningEnvironment,
            String pathOnHost, InputStream stream) throws EnvironmentDriverException {
        if (!(runningEnvironment instanceof DockerRunningEnvironment))
            throw new EnvironmentDriverException(
                    "DockerEnvironmentDriver cannot manage non-Docker environments. " + runningEnvironment);

        DockerRunningEnvironment dockerEnv = (DockerRunningEnvironment) runningEnvironment;
        copyFileToContainer(dockerEnv.getSshPort(), pathOnHost, null, stream);
    }

    /**
     * Copy file to container using SSH tunnel.
     * Data can be passed using stringData or streamData variable. Exactly one of this variables has to be not null.
     * 
     * @param sshPort Target port on which SSH service is running
     * @param pathOnHost Path in target container, where the data are passed
     * @param stringData Data, which will be transfered to the target container (may be null if streamData are set)
     * @param streamData Data, which will be transfered to the target container (may be null if stringData are set)
     * @throws EnvironmentDriverException Thrown if both stringData and streamData are null
     */
    private void copyFileToContainer(int sshPort, String pathOnHost, String stringData,
            InputStream streamData) throws EnvironmentDriverException {
        SshClient sshClient = new SshjSshClient(new BackoffLimitedRetryHandler() {
        }, // TODO check retryHandler configuration
                HostAndPort.fromParts("10.3.8.102", sshPort),
                LoginCredentials.builder().user(CONTAINER_USER).password(CONTAINER_PSSWD).build(),
                1000, Optional.<Connector> absent());

        try {
            sshClient.connect();
            if (stringData != null)
                sshClient.put(pathOnHost, stringData);
            else {
                if (streamData != null)
                    sshClient.put(pathOnHost, Payloads.newInputStreamPayload(streamData));
                else
                    throw new EnvironmentDriverException(
                            "It is not possible to send null data to the container.");
            }

        } finally {
            if (sshClient != null)
                sshClient.disconnect();
        }

    }

    /**
     * Prepares port binding configuration of a container
     * 
     * @param jenkinsPort Requested jenkinsPort
     * @param sshPort Requested sshPort
     * @return Prepared configuration of port binding
     */
    private Map<String, List<Map<String, String>>> createPortBinding(
            int jenkinsPort, int sshPort) {
        Map<String, List<Map<String, String>>> portBindings = new HashMap<>();

        portBindings.put("8080/tcp", createHostBindings(jenkinsPort));
        portBindings.put("22/tcp", createHostBindings(sshPort));

        return portBindings;
    }

    /**
     * Creates host port binding configuration
     * 
     * @param hostPort Requested port to which will be container port mapped
     * @return Prepared configuration of host binding
     */
    private List<Map<String, String>> createHostBindings(int hostPort) {
        Map<String, String> singleHostBinding = new HashMap<>();
        singleHostBinding.put("HostIp", "0.0.0.0");
        singleHostBinding.put("HostPort", Integer.toString(hostPort));

        List<Map<String, String>> hostBindings = new ArrayList<>();
        hostBindings.add(singleHostBinding);
        return hostBindings;
    }

}
