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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.DockerEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.docker.DockerApi;
import org.jclouds.docker.domain.Config;
import org.jclouds.docker.domain.Container;
import org.jclouds.docker.domain.HostConfig;
import org.jclouds.docker.features.RemoteApi;
import org.jclouds.docker.options.RemoveContainerOptions;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Implementation of environment driver, which uses Docker to run environments
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@ApplicationScoped
public class DockerEnvironmentDriver implements EnvironmentDriver {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final Path workingDirectory = FileSystems.getDefault().getPath("/tmp");

    @Inject
    private Generator generator;

    @Inject
    private DockerInitializationMonitor dockerInitMonitor;

    private ComputeServiceContext dockerContext;

    private RemoteApi dockerClient;

    /** User in running environment to which we can connect with SSH */
    private String containerUser;

    /** Password of user to which we can connect with SSH */
    private String containerUserPsswd;

    /** Connection URL to Docker control port */
    private String dockerEndpoint;

    /** ID of docker image */
    private String dockerImageId;

    private String dockerIp;

    private String containerFirewallAllowedDestinations;
    
    /** proxy server settings passes as environment variables to docker builder */
    private String proxyServer;
    
    private String proxyPort;
    
    /**
     * States of creating Docker container
     * 
     * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
     *
     */
    private enum BuildContainerState {
        NOT_BUILT,
        BUILT,
        STARTED,
    }

    /**
     * Only workaround for CDI constructor parameter injection
     */
    @Deprecated
    public DockerEnvironmentDriver() {
    }

    /**
     * Loads configuration, prepares connection to Docker daemon
     * 
     * @throws ConfigurationParseException Thrown if configuration cannot be obtained
     */
    @Inject
    public DockerEnvironmentDriver(Configuration configuration) throws ConfigurationParseException {
        DockerEnvironmentDriverModuleConfig config =
                configuration.getModuleConfig(DockerEnvironmentDriverModuleConfig.class);

        dockerIp = config.getIp();
        dockerEndpoint = "http://" + dockerIp + ":2375";
        containerUser = config.getInContainerUser();
        containerUserPsswd = config.getInContainerUserPassword();
        dockerImageId = config.getDockerImageId();
        containerFirewallAllowedDestinations = config.getFirewallAllowedDestinations();
        proxyServer = config.getProxyServer();
        proxyPort = config.getProxyPort();

        dockerContext = ContextBuilder.newBuilder("docker")
                .endpoint(dockerEndpoint)
                .credentials(containerUser, containerUserPsswd)
                .modules(ImmutableSet.<Module> of(new SLF4JLoggingModule()))
                .buildView(ComputeServiceContext.class);
        dockerClient = dockerContext.unwrapApi(DockerApi.class).getRemoteApi();
    }

    @Override
    public StartedEnvironment buildEnvironment(Environment buildEnvironment,
            RepositorySession repositorySession) throws EnvironmentDriverException {
        if (!canBuildEnvironment(buildEnvironment))
            throw new UnsupportedOperationException(
                    "DockerEnvironmentDriver currently provides support only for Linux environments on Docker.");

        String containerId = generator.generateContainerId();
        BuildContainerState buildContainerState = BuildContainerState.NOT_BUILT;

        logger.info("Trying to start Docker container...");
        int sshPort, jenkinsPort;
        try {
            Config config = Config
                    .builder()
                    .imageId(dockerImageId)
                    .env(prepareEnvVariables(repositorySession.getConnectionInfo().getDependencyUrl(), 
                                             repositorySession.getConnectionInfo().getDeployUrl(),
                                             proxyServer,
                                             proxyPort)).build();
            logger.fine("Creating docker container with config: " + config);
            Container createdContainer = dockerClient.createContainer(containerId, config);
            buildContainerState = BuildContainerState.BUILT;

            dockerClient.startContainer(containerId,
                    HostConfig.builder()
                            .publishAllPorts(true)
                            .privileged(true)
                            .build());
            buildContainerState = BuildContainerState.STARTED;

            Map<String, HostPortMapping> containerPortMappings =
                    getContainerPortMappings(createdContainer.getId());
            // Find out, which ports are opened for SSH and Jenkins
            sshPort = getSshPort(containerPortMappings);
            jenkinsPort = getJenkinsPort(containerPortMappings);
        } catch (Exception e) {
            // Creating container failed => clean up
            logger.warning("Docker container failed to start. " + e);

            if (buildContainerState != BuildContainerState.NOT_BUILT) {
                if (buildContainerState == BuildContainerState.BUILT)
                    destroyContainer(containerId, false);
                else
                    destroyContainer(containerId, true);
            }
            throw new EnvironmentDriverException("Docker container couldn't be created.", e);
        }

        logger.info("Created and started Docker container. ID: " + containerId
                + ", SSH port: " + sshPort + ", Jenkins Port: " + jenkinsPort + ", Working directory: " + workingDirectory);

        return new DockerStartedEnvironment(this, dockerInitMonitor, repositorySession,
                containerId, jenkinsPort, sshPort, "http://" + dockerIp, workingDirectory);
    }

    @Override
    public boolean canBuildEnvironment(Environment environment) {
        if (environment.getBuildType() == BuildType.JAVA &&
                environment.getOperationalSystem() == OperationalSystem.LINUX)
            return true;
        else
            return false;
    }

    /**
     * Destroys running container
     * 
     * @param containerId ID of container
     * @throws EnvironmentDriverException Thrown if any error occurs during destroying running environment
     */
    public void destroyEnvironment(String containerId) throws EnvironmentDriverException {
        destroyContainer(containerId, true);
    }

    /**
     * Destroys container
     * 
     * @param containerId ID of container
     * @param isRunning True if the container is running
     * @throws EnvironmentDriverException Thrown if any error occurs during destroying running environment
     */
    private void destroyContainer(String containerId, boolean isRunning) throws EnvironmentDriverException {
        logger.info("Trying to destroy Docker container with ID: " + containerId);
        try {
            if (isRunning)
                dockerClient.stopContainer(containerId);
            dockerClient.removeContainer(containerId, new RemoveContainerOptionsExtended().force(true));
        } catch (RuntimeException e) {
            logger.warning("Docker container (ID:" + containerId + " )couldn't be removed: " + e);
            throw new EnvironmentDriverException("Cannot destroy environment.", e);
        }
        logger.info("Docker container with ID: " + containerId + " was destroyed.");
    }

    /**
     * Gets public host port of Jenkins
     * 
     * @param ports Port mappings of container
     * @return Public host port of Jenkins
     */
    private int getJenkinsPort(Map<String, HostPortMapping> ports) {
        return Integer.parseInt(ports.get("8080").getHostPort());
    }

    /**
     * Gets public host port of SSH
     * 
     * @param ports Port mappings of container
     * @return Public host port of SSH
     */
    private int getSshPort(Map<String, HostPortMapping> ports) {
        return Integer.parseInt(ports.get("22").getHostPort());
    }

    /**
     * Prepares configuration of environment variables
     * for creating Docker container
     * 
     * @param dependencyUrl AProx dependencyUrl
     * @param deployUrl AProx deployUrl
     * @param proxyServer Proxy server IP address or DNS resolvable name
     * @param proxyPort number of proxy server port where is it listening
     * 
     * @return Environment variables configuration
     */
    private List<String> prepareEnvVariables(String dependencyUrl, String deployUrl, String proxyServer, String proxyPort) {
        String proxyActive = "false";
        
        if ( (proxyServer != null && proxyPort != null) &&
             (!proxyServer.isEmpty() && !proxyPort.isEmpty()) ) {
            proxyActive = "true";
        }
        
        List<String> envVariables = new ArrayList<>();
        envVariables.add("firewallAllowedDestinations=" + containerFirewallAllowedDestinations);
        envVariables.add("AProxDependencyUrl=" + dependencyUrl);
        envVariables.add("AProxDeployUrl=" + deployUrl);
        envVariables.add("isHttpActive=" + proxyActive);
        envVariables.add("proxyIPAddress=" + proxyServer);
        envVariables.add("proxyPort=" + proxyPort);
        return envVariables;
    }   

    /**
     * Get container port mapping from Docker daemon REST interface.
     * 
     * @param containerId ID of running container
     * @return Map with pairs containerPort:publicPort
     * @throws Exception Thrown if data could not be obtained from Docker daemon or are corrupted
     */
    private Map<String, HostPortMapping> getContainerPortMappings(String containerId) throws Exception {
        Map<String, HostPortMapping> resultMap = new HashMap<>();
        String response = HttpUtils.processGetRequest(String.class,
                dockerEndpoint + "/containers/" + containerId + "/json");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode networkSettingsNode = rootNode.path("NetworkSettings");
        JsonNode portsNode = networkSettingsNode.path("Ports");

        Map<String, List<HostPortMapping>> portsMap = objectMapper.readValue(portsNode.traverse(),
                new TypeReference<Map<String, List<HostPortMapping>>>() {
                });

        for (Map.Entry<String, List<HostPortMapping>> entry : portsMap.entrySet()) {
            resultMap.put(entry.getKey().substring(0, entry.getKey().indexOf("/")),
                    entry.getValue().get(0));
        }

        return resultMap;
    }

    /**
     * Extended options for removing container, which forces Docker to remove
     * volume attached to the container 
     * 
     * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
     *
     */
    private class RemoveContainerOptionsExtended extends RemoveContainerOptions {

        public RemoveContainerOptionsExtended() {
            this.queryParameters.put("v", "true");
        }
    }

}
