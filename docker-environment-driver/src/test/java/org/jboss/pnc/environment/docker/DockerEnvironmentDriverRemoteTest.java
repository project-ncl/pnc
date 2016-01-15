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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.DockerEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.environment.RunningEnvironment;
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
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for DockerEnnvironmentDriver
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@RunWith(Arquillian.class)
@Category({ RemoteTest.class, ContainerTest.class })
public class DockerEnvironmentDriverRemoteTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final String APROX_DEPENDENCY_URL = "AProx dependency URL";

    private static final String APROX_DEPLOY_URL = "AProx deploy URL";

    private static final RepositorySession DUMMY_REPOSITORY_CONFIGURATION = new DummyRepositoryConfiguration();

    /** Maximum test duration in seconds */
    private static final int MAX_TEST_DURATION = 100;

    @Inject
    private DockerEnvironmentDriver dockerEnvDriver;

    @Inject
    private Configuration configurationService;

    private String dockerIp;

    private String dockerControlEndpoint;
    
    private String dockerProxyHost;
    
    private String dockerProxyPort;

    private boolean isInitialized = false;

    @Deployment
    public static WebArchive createDeployment() {
        final WebArchive testedEjb = ShrinkWrap
                .create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("pnc-config.json")
                .addPackage(RemoteTest.class.getPackage())
                .addPackages(true, DockerEnvironmentDriver.class.getPackage());

        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity().asFile();

        testedEjb.addAsLibraries(libs);

        logger.info("Deployment: " + testedEjb.toString(true));
        return testedEjb;
    }

    @Before
    public void init() throws ConfigurationParseException {
        if (!isInitialized) {
            final DockerEnvironmentDriverModuleConfig config = configurationService
                    .getModuleConfig(new PncConfigProvider<DockerEnvironmentDriverModuleConfig>(DockerEnvironmentDriverModuleConfig.class));
            dockerIp = config.getIp();
            dockerControlEndpoint = "http://" + dockerIp + ":2375";
            dockerProxyHost = config.getProxyServer();
            dockerProxyPort = config.getProxyPort();
            isInitialized = true;
        }
    }

    @Test
    public void injectedTypeTest() {
        assertNotNull(dockerEnvDriver);
    }

    @Test
    public void canBuildEnvironmentTest() {
        assertTrue(dockerEnvDriver.canBuildEnvironment(BuildType.JAVA));
        assertFalse(dockerEnvDriver.canBuildEnvironment(null));
        assertFalse(dockerEnvDriver.canBuildEnvironment(BuildType.DOCKER));
    }

    @Test
    public void buildDestroyEnvironmentTest() throws EnvironmentDriverException, InterruptedException {
        final Semaphore mutex = new Semaphore(0);

        // Create container
        final DockerStartedEnvironment startedEnv = (DockerStartedEnvironment)
                dockerEnvDriver.buildEnvironment(BuildType.JAVA, DUMMY_REPOSITORY_CONFIGURATION);

        Consumer<RunningEnvironment> onComplete = (generalRunningEnv) -> {
            DockerRunningEnvironment runningEnv = (DockerRunningEnvironment) generalRunningEnv;
            boolean containerDestroyed = false;
            try {
                testRunningContainer(runningEnv, true,
                        "Environment wasn't successfully built.");
                testRunningEnvContainer(runningEnv, true, "Environment wasn't set up correctly.");

                // Destroy container
                dockerEnvDriver.destroyEnvironment(runningEnv.getId());
                containerDestroyed = true;
                testRunningContainer(runningEnv, false,
                        "Environment wasn't successfully destroyed.");
                mutex.release();
            } catch (Throwable e) {
                fail(e.getMessage());
            } finally {
                if (!containerDestroyed)
                    destroyEnvironmentWithReport(runningEnv.getId());
            }
        };

        Consumer<Exception> onError = (e) -> {
            destroyEnvironmentWithReport(startedEnv.getId());
            fail("Failed to init docker container. " + e.getMessage());
        };

        startedEnv.monitorInitialization(onComplete, onError);
        mutex.tryAcquire(MAX_TEST_DURATION, TimeUnit.SECONDS);
    }
    
    /**
     * Checks if container was started and the services are on.
     *
     * @param runningEnv Connection data about environment to test
     * @param shouldBeRunning Indicates, if the environment should be running or should be not available
     * @param baseErrorMsg Prefix of error message, which is printed if the container is not in expected state
     */
    private void testRunningContainer(final DockerRunningEnvironment runningEnv,
            final boolean shouldBeRunning,
            final String baseErrorMsg) {
        final int sshPort = runningEnv.getSshPort();
        final String containerId = runningEnv.getId();

        // Test if the container is running
        try {
            HttpUtils.testResponseHttpCode(200, dockerControlEndpoint + "/containers/" + containerId
                    + "/json");
            assertEquals(baseErrorMsg + " Container is running", shouldBeRunning, true);
        } catch (final Exception e) {
            assertEquals(baseErrorMsg + " Container is not running", shouldBeRunning, false);
        }

        // Test if Jenkins is running
        try {
            HttpUtils.testResponseHttpCode(200, runningEnv.getJenkinsUrl());
            if (!shouldBeRunning) {
                fail("Jenkins is running, but should be down");
            }
        } catch (final Exception e) {
            if (shouldBeRunning) {
                fail("Jenkins wasn't started successully");
            }
        }

        // Test it the SSH port is opened
        assertEquals(baseErrorMsg + " Test opened SSH port", shouldBeRunning, testOpenedPort(sshPort));
    }
    
    private void testRunningEnvContainer(final DockerRunningEnvironment runningEnv,
            final boolean shouldBeRunning,
            final String baseErrorMsg) {
        final int sshPort = runningEnv.getSshPort();
        final String containerId = runningEnv.getId();

        // Test if the container is running
        try {
            HttpUtils.testResponseHttpCode(200, dockerControlEndpoint + "/containers/" + containerId
                    + "/json");

            String containerJSON = HttpUtils.processGetRequest(dockerControlEndpoint + "/containers/" + containerId + "/json");
            Map<String,Object> jsonMap = getJSONFromString(containerJSON);
            Map<String,Object> envMap = getEnvJSONFromDocker(jsonMap);
            
            String proxyIP = (String) envMap.get("proxyIPAddress");
            String proxyPort = (String) envMap.get("proxyPort");
            String isHttpActive = (String) envMap.get("isHttpActive");
                        
            assertEquals(dockerProxyHost, proxyIP);
            assertEquals(dockerProxyPort, proxyPort);
            assertEquals( ((dockerProxyHost == null) || (dockerProxyPort == null)), isHttpActive);
            
            assertEquals(baseErrorMsg + " Container is running", shouldBeRunning, true);
        } catch (final Exception e) {
            assertEquals(baseErrorMsg + " Container is not running", shouldBeRunning, false);
        }

        // Test if Jenkins is running
        try {
            HttpUtils.testResponseHttpCode(200, runningEnv.getJenkinsUrl());
            if (!shouldBeRunning) {
                fail("Jenkins is running, but should be down");
            }
        } catch (final Exception e) {
            if (shouldBeRunning) {
                fail("Jenkins wasn't started successully");
            }
        }

        // Test it the SSH port is opened
        assertEquals(baseErrorMsg + " Test opened SSH port", shouldBeRunning, testOpenedPort(sshPort));
    }
    
    static private Map<String,Object> getJSONFromString(String str){
        Map<String,Object> result = new HashMap<String, Object>();
        try {
            result = (Map<String,Object>) new ObjectMapper().readValue(str, Map.class);    
        } catch (JsonMappingException jme){
            logger.error("Error while converting container JSON to Map - " + jme.getLocalizedMessage());
        } catch (IOException ioe){
            logger.error("Error while converting container JSON to Map - " + ioe.getLocalizedMessage());
        }
        return result;    
    }

    private void destroyEnvironmentWithReport(String id) {
        try {
            dockerEnvDriver.destroyEnvironment(id);
        } catch (Exception e1) {
            logger.warn("Environment LEAK! The running environment couldn't be removed. ID: " + id + "\n" + e1);
        }
    }

    private boolean testOpenedPort(final int port) {
        try {
            final Socket echoSocket = new Socket(dockerIp, port);
            echoSocket.close();
        } catch (final IOException e) {
            return false;
        }
        return true;
    }
    /**
     * Get Environment variables from Map created from JSON
     * @param map
     * @return
     */
    @SuppressWarnings("unchecked")
    static private Map<String,Object> getEnvJSONFromDocker(Map<String, Object> map){
        Map<String,Object> result = new HashMap<String, Object>();
        Map<String, Object> configMap = ((Map<String, Object>) map.get("Config"));
        Object envConfig = configMap.get("Env");
        
        if (envConfig instanceof ArrayList<?>) {
          List<?> configEnv = (ArrayList<?>) envConfig;
          for (Object object : configEnv) {
            String valuePair = (String) object;
            String[] splitStrings = valuePair.split("=");
            result.put(splitStrings[0], splitStrings[1]);
          }
        }
        return result;    
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

    }

}
