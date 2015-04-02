package org.jboss.pnc.environment.docker;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.DockerEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.category.RemoteTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Unit tests for DockerEnnvironmentDriver
 *
 * @author Jakub Bartecek <jbartece@redhat.com>
 */
@RunWith(Arquillian.class)
@Category({RemoteTest.class, ContainerTest.class})
public class DockerEnvironmentDriverRemoteTest {

    private static final Logger log = Logger.getLogger(DockerEnvironmentDriverRemoteTest.class.getName());

    private static final String APROX_DEPENDENCY_URL = "AProx dependency URL";

    private static final String APROX_DEPLOY_URL = "AProx deploy URL";

    private static final RepositorySession DUMMY_REPOSITORY_CONFIGURATION = new DummyRepositoryConfiguration();

    @Inject
    private DockerEnvironmentDriver dockerEnvDriver;

    @Inject
    private Configuration configurationService;

    private String dockerIp;

    private String dockerControlEndpoint;

    private boolean isInitialized = false;

    @Deployment
    public static WebArchive createDeployment() {
        final WebArchive testedEjb = ShrinkWrap
                .create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("jenkins-maven-config.xml")
                .addAsResource("pnc-config.json")
                .addPackage(RemoteTest.class.getPackage())
                .addPackages(true, DockerEnvironmentDriver.class.getPackage());

        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity().asFile();

        testedEjb.addAsLibraries(libs);

        log.info("Deployment: " + testedEjb.toString(true));
        return testedEjb;
    }

    @Before
    public void init() throws ConfigurationParseException {
        if (!isInitialized) {
            final DockerEnvironmentDriverModuleConfig config = 
                    configurationService.getModuleConfig(DockerEnvironmentDriverModuleConfig.class);
            dockerIp = config.getIp();
            dockerControlEndpoint = "http://" + dockerIp + ":2375";
            isInitialized = true;
        }
    }

    @Test
    public void injectedTypeTest() {
        assertNotNull(dockerEnvDriver);
    }

    @Test
    public void canBuildEnvironmentTest() {
        final Environment goodEnv = new Environment(BuildType.JAVA, OperationalSystem.LINUX);
        final Environment badEnv1 = new Environment(null, null);
        final Environment badEnv2 = new Environment(BuildType.DOCKER, OperationalSystem.LINUX);
        final Environment badEnv3 = new Environment(BuildType.JAVA, OperationalSystem.WINDOWS);

        assertTrue(dockerEnvDriver.canBuildEnvironment(goodEnv));
        assertFalse(dockerEnvDriver.canBuildEnvironment(badEnv1));
        assertFalse(dockerEnvDriver.canBuildEnvironment(badEnv2));
        assertFalse(dockerEnvDriver.canBuildEnvironment(badEnv3));
    }

    @Test
    public void buildDestroyEnvironmentTest() throws EnvironmentDriverException, InterruptedException {
        // Create container
        final Environment environment = new Environment(BuildType.JAVA, OperationalSystem.LINUX);
        final DockerRunningEnvironment runningEnv = (DockerRunningEnvironment)
                dockerEnvDriver.buildEnvironment(environment, DUMMY_REPOSITORY_CONFIGURATION);

        try {
            testRunningContainer(runningEnv, true, "Environment wasn't successfully built.");
        } catch (Exception | AssertionError e) {
            dockerEnvDriver.destroyEnvironment(runningEnv.getId());
            throw e;
        }

        // Destroy container
        dockerEnvDriver.destroyEnvironment(runningEnv.getId());
        testRunningContainer(runningEnv, false, "Environment wasn't successfully destroyed.");
    }

    @Test
    public void copyFileToContainerStringDataTest() throws Exception {
        copyFileToContainerInvariantData("TEST CONTENT", null);
    }

    @Test
    public void copyFileToContainerStreamDataTest() throws Exception {
        copyFileToContainerInvariantData(null, new ByteArrayInputStream("TEST CONTENT".getBytes("UTF-8")));
    }

    private void copyFileToContainerInvariantData(final String string, final InputStream stream) throws Exception {
        final Environment environment = new Environment(BuildType.JAVA, OperationalSystem.LINUX);

        final DockerRunningEnvironment runningEnv = (DockerRunningEnvironment)
                dockerEnvDriver.buildEnvironment(environment, DUMMY_REPOSITORY_CONFIGURATION);

        final String pathToFile = "/tmp/testFile-" + UUID.randomUUID().toString() + ".txt";

        // Get content of container's file system
        final String fsContentOld = HttpUtils.processGetRequest(String.class, dockerControlEndpoint + "/containers/"
                + runningEnv.getId() + "/changes");
        fsContentOld.contains(pathToFile);

        try {
            dockerEnvDriver.copyFileToContainer(runningEnv.getSshPort(), pathToFile, string, stream);
        } catch (Exception | AssertionError e) {
            dockerEnvDriver.destroyEnvironment(runningEnv.getId());
            throw e;
        }

        // Get content of container's file system
        final String fsContentNew = HttpUtils.processGetRequest(String.class, dockerControlEndpoint + "/containers/"
                + runningEnv.getId() + "/changes");
        assertTrue("File was not coppied to the container.", fsContentNew.contains(pathToFile));

        dockerEnvDriver.destroyEnvironment(runningEnv.getId());
    }

    /**
     * Checks if container was started and the services are on.
     *
     * @param runningEnv Connection data about environment to test
     * @param shouldBeRunning Indicates, if the environment should be running or should be not available
     * @param baseErrorMsg Prefix of error message, which is printed if the container is not in expected state
     */
    private void testRunningContainer(final DockerRunningEnvironment runningEnv, final boolean shouldBeRunning,
            final String baseErrorMsg)  {
        final int sshPort = runningEnv.getSshPort();
        final String containerId = runningEnv.getId();

        // Test if the container is running
        try {
            HttpUtils.testResponseHttpCode(200, dockerControlEndpoint + "/containers/" + containerId + "/json");
            assertEquals(baseErrorMsg + " Container is running", shouldBeRunning, true);
        } catch (final Exception e) {
            assertEquals(baseErrorMsg + " Container is not running", shouldBeRunning, false);
        }

        // Test if Jenkins is running
        try {
            HttpUtils.testResponseHttpCode(200, runningEnv.getJenkinsUrl());
            if(!shouldBeRunning) {
                fail("Jenkins is running, but should be down");
            }
        } catch (final Exception e) {
            if(shouldBeRunning) {
                fail("Jenkins wasn't started successully");
            }
        }

        // Test it the SSH port is opened
        assertEquals(baseErrorMsg + " Test opened SSH port", shouldBeRunning, testOpenedPort(sshPort));
    }

    /**
     * Checks if the specified port is opened on Docker host
     *
     * @param generatedSshPort Port to test
     */
    private boolean testOpenedPort(final int port) {
        try {
            final Socket echoSocket = new Socket(dockerIp, port);
            echoSocket.close();
        } catch (final IOException e) {
            return false;
        }
        return true;
    }


    private static class DummyRepositoryConfiguration implements RepositorySession {

        @Override
        public RepositoryType getType() {
            return null;
        }

        @Override
        public String getBuildRepositoryId() {
            return null;
        }

        @Override
        public RepositoryConnectionInfo getConnectionInfo() {
            return new RepositoryConnectionInfo() {

                @Override
                public String getToolchainUrl() {
                    return null;
                }

                @Override
                public Map<String, String> getProperties() {
                    return null;
                }

                @Override
                public String getDeployUrl() {
                    return APROX_DEPLOY_URL;
                }

                @Override
                public String getDependencyUrl() {
                    return APROX_DEPENDENCY_URL;
                }
            };
        }

        @Override
        public RepositoryManagerResult extractBuildArtifacts() throws RepositoryManagerException {
            return null;
        }

        @Override
        public String getBuildSetRepositoryId() {
            return null;
        }

    }

}
