package org.jboss.pnc.environment.docker;

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
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.docker.DockerApi;
import org.jclouds.docker.domain.Config;
import org.jclouds.docker.domain.HostConfig;
import org.jclouds.docker.features.RemoteApi;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Stateless
public class DockerEnvironmentDriver implements EnvironmentDriver {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass()
            .getName());

    private static final String IMAGE_ID = "jbartece/isshd-jenkins";

    private static final String DOCKER_ENDPOINT = "http://10.3.8.102:2375";

    @Inject
    private Generator generator;

    private RemoteApi dockerClient;

    @PostConstruct
    private void init() {
        ComputeServiceContext context = ContextBuilder.newBuilder("docker")
                .endpoint(DOCKER_ENDPOINT)
                .credentials("root", "changeme")
                .modules(ImmutableSet.<Module> of(new SLF4JLoggingModule(),
                        new SshjSshClientModule()))
                .buildView(ComputeServiceContext.class);
        dockerClient = context.unwrapApi(DockerApi.class).getRemoteApi();
    }

    @Override
    public RunningEnvironment buildEnvironment(Environment buildEnvironment) {
        if (buildEnvironment.getBuildType() != BuildType.DOCKER ||
                buildEnvironment.getOperationalSystem() != OperationalSystem.LINUX)
            throw new UnsupportedOperationException(
                    "DockerEnvironmentDriver currently provides support only for Linux enviroments on Docker.");

        int jenkinsPort = generator.generateJenkinsPort();
        int sshPort = generator.generateSshPort();
        String containerId = generator.generateContainerId();

        logger.warning("Creating container");
        dockerClient.createContainer(
                containerId,
                Config.builder()
                        .imageId(IMAGE_ID)
                        .build());

        logger.warning("Starting container");
        dockerClient.startContainer(containerId,
                HostConfig.builder().portBindings(createPortBinding(jenkinsPort, sshPort)).build());

        return new RunningEnvironment(containerId, jenkinsPort, sshPort);
    }

    @Override
    public void destroyEnvironment(RunningEnvironment runningEnv) {
        logger.warning("Stopping container");
        dockerClient.stopContainer(runningEnv.getId());
        logger.warning("Removing container");
        dockerClient.removeContainer(runningEnv.getId());
    }

    @Override
    public boolean canBuildEnviroment(Environment environment) {
        if (environment.getBuildType() == BuildType.DOCKER ||
                environment.getOperationalSystem() == OperationalSystem.LINUX)
            return true;
        else
            return false;
    }

    private static Map<String, List<Map<String, String>>> createPortBinding(
            int jenkinsPort, int sshPort) {
        Map<String, List<Map<String, String>>> portBindings = new HashMap<>();

        portBindings.put("8080/tcp", createHostBindings(jenkinsPort));
        portBindings.put("22/tcp", createHostBindings(sshPort));

        return portBindings;
    }

    private static List<Map<String, String>> createHostBindings(int hostPort) {
        Map<String, String> jenkinsHostBinding = new HashMap<>();
        jenkinsHostBinding.put("HostIp", "0.0.0.0");
        jenkinsHostBinding.put("HostPort", Integer.toString(hostPort));
        List<Map<String, String>> hostBindings = new ArrayList<>();
        hostBindings.add(jenkinsHostBinding);
        return hostBindings;
    }

}
