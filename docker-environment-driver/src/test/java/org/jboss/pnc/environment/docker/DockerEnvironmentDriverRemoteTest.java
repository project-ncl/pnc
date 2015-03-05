package org.jboss.pnc.environment.docker;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.DockerEnvironmentDriverModuleConfig;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for DockerEnnvironmentDriver
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@RunWith(Arquillian.class)
public class DockerEnvironmentDriverRemoteTest {

    private static final Logger log = Logger.getLogger(DockerEnvironmentDriverRemoteTest.class.getName());

    private final String APROX_DEPENDENCY_URL = "AProx dependency URL";

    private final String APROX_DEPLOY_URL = "AProx deploy URL";

    @Inject
    private DockerEnvironmentDriver dockerEnvDriver;

    @Inject
    private Configuration<DockerEnvironmentDriverModuleConfig> configurationService;

    private String dockerIp;

    private String dockerControlEndpoint;

    private boolean isInitialized = false;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive testedEjb = ShrinkWrap
                .create(WebArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("jenkins-maven-config.xml")
                .addAsResource("pnc-config.json")
                .addPackages(true, DockerEnvironmentDriver.class.getPackage());

        JavaArchive[] libs = Maven.configureResolver()
                .withMavenCentralRepo(true)
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .as(JavaArchive.class);

        testedEjb.addAsLibraries(libs);

        log.info("Deployment: " + testedEjb.toString(true));
        return testedEjb;
    }

    @Before
    public void init() throws ConfigurationParseException {
        if (!isInitialized) {
            DockerEnvironmentDriverModuleConfig config =
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
        Environment goodEnv = new Environment(BuildType.DOCKER, OperationalSystem.LINUX);
        Environment badEnv1 = new Environment(null, null);
        Environment badEnv2 = new Environment(BuildType.JAVA, OperationalSystem.LINUX);
        Environment badEnv3 = new Environment(BuildType.DOCKER, OperationalSystem.WINDOWS);

        assertTrue(dockerEnvDriver.canBuildEnvironment(goodEnv));
        assertFalse(dockerEnvDriver.canBuildEnvironment(badEnv1));
        assertFalse(dockerEnvDriver.canBuildEnvironment(badEnv2));
        assertFalse(dockerEnvDriver.canBuildEnvironment(badEnv3));
    }

    @Test
    public void buildDestroyEnvironmentTest() throws EnvironmentDriverException, InterruptedException {
        // Create container
        Environment environment = new Environment(BuildType.DOCKER, OperationalSystem.LINUX);
        DockerRunningEnvironment runningEnv = (DockerRunningEnvironment)
                dockerEnvDriver.buildEnvironment(environment, APROX_DEPENDENCY_URL, APROX_DEPLOY_URL);

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

    private void copyFileToContainerInvariantData(String string, InputStream stream) throws Exception {
        Environment environment = new Environment(BuildType.DOCKER, OperationalSystem.LINUX);

        DockerRunningEnvironment runningEnv = (DockerRunningEnvironment)
                dockerEnvDriver.buildEnvironment(environment, APROX_DEPENDENCY_URL, APROX_DEPLOY_URL);

        String pathToFile = "/tmp/testFile-" + UUID.randomUUID().toString() + ".txt";

        // Get content of container's file system
        String fsContentOld = processGetRequest(dockerControlEndpoint + "/containers/"
                + runningEnv.getId() + "/changes");
        fsContentOld.contains(pathToFile);

        try {
            dockerEnvDriver.copyFileToContainer(runningEnv.getSshPort(), pathToFile, string, stream);
        } catch (Exception | AssertionError e) {
            dockerEnvDriver.destroyEnvironment(runningEnv.getId());
            throw e;
        }

        // Get content of container's file system
        String fsContentNew = processGetRequest(dockerControlEndpoint + "/containers/"
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
    private void testRunningContainer(DockerRunningEnvironment runningEnv, boolean shouldBeRunning,
            String baseErrorMsg) {
        int sshPort = runningEnv.getSshPort();
        String containerId = runningEnv.getId();

        // Test if the container is running
        try {
            testResponseHttpCode(200, dockerControlEndpoint + "/containers/" + containerId + "/json");
            assertEquals(baseErrorMsg + " Container is running", shouldBeRunning, true);
        } catch (Exception e) {
            assertEquals(baseErrorMsg + " Container is not running", shouldBeRunning, false);
        }

        // Test if Jenkins is running is not performed currently
        // It would take 20-30 seconds to get code 200 from Jenkins

        // Test it the SSH port is opened
        assertEquals(baseErrorMsg + " Test opened SSH port", shouldBeRunning, testOpenedPort(sshPort));
    }

    /**
     * Checks if the specified port is opened on Docker host
     * 
     * @param generatedSshPort Port to test
     */
    private boolean testOpenedPort(int port) {
        try {
            Socket echoSocket = new Socket(dockerIp, port);
            echoSocket.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Process HTTP GET request and get the data as String.
     * Client accepts application/json MIME type.
     * 
     * @param url Request URL
     * @throws Exception Thrown if some error occurs in communication with server
     */
    protected String processGetRequest(String url) throws Exception {
        ClientRequest request = new ClientRequest(url);
        request.accept(MediaType.APPLICATION_JSON);

        ClientResponse<String> response = request.get(String.class);
        return response.getEntity();
    }

    /**
     * Process HTTP requests and tests if server responds with expected HTTP code.
     * Request is implicitly set to accept MIME type application/json.
     * 
     * @param ecode Expected HTTP error code
     * @param url Request URL
     * @throws Exception Thrown if some error occurs in communication with server
     */
    protected void testResponseHttpCode(int ecode, String url) throws Exception {
        ClientRequest request = new ClientRequest(url);
        request.accept(MediaType.APPLICATION_JSON);

        ClientResponse<String> response = request.get(String.class);
        if (response.getStatus() != ecode)
            throw new Exception("Server returned unexpected HTTP code! Returned code:" + response.getStatus());
    }

}
