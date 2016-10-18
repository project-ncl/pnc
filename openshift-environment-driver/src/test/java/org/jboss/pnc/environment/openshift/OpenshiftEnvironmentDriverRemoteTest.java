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
package org.jboss.pnc.environment.openshift;

import com.openshift.internal.restclient.DefaultClient;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.environment.monitor.PullingMonitor;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.model.ArtifactRepo;
import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.DebugTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Category({ DebugTest.class })
public class OpenshiftEnvironmentDriverRemoteTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final RepositorySession DUMMY_REPOSITORY_CONFIGURATION = new DummyRepositoryConfiguration();

    private static final int TEST_EXECUTION_TIMEOUT = 30;

    private final EnvironmentDriver environmentDriver;

    private final Configuration configurationService;

    public OpenshiftEnvironmentDriverRemoteTest() throws Exception {
        //workaround for protected root rest endpoint from where version should be read
        System.setProperty(DefaultClient.SYSTEM_PROP_OPENSHIFT_API_VERSION, "v1");

        configurationService = new Configuration();

        environmentDriver = new OpenshiftEnvironmentDriver(configurationService, new PullingMonitor(configurationService));
    }

    @Test
    public void createAndDestroyEnvironment() throws EnvironmentDriverException, InterruptedException {
        final Semaphore mutex = new Semaphore(0);
        ObjectWrapper<Throwable> exceptionWrapper = new ObjectWrapper<>();

        String dummyImageId = "abcd1234";
        String dummyRepoUrl = "test.repo.url/repo";

        // Create container
        final StartedEnvironment startedEnv = environmentDriver.startEnvironment(dummyImageId, dummyRepoUrl,
                SystemImageType.DOCKER_IMAGE, DUMMY_REPOSITORY_CONFIGURATION,
                new DebugData(false));

        Consumer<RunningEnvironment> onEnvironmentStarted = (runningEnvironment) -> {
            boolean containerDestroyed = false;
            try {
                assertThatContainerIsRunning(runningEnvironment);

                // Destroy container
                destroyEnvironment(runningEnvironment);
                containerDestroyed = true;
                assertThatContainerIsNotRunning(runningEnvironment);
                mutex.release();
            } catch (Throwable e) {
                exceptionWrapper.set(e);
            } finally {
                if (!containerDestroyed) {
                    destroyEnvironmentWithReport(runningEnvironment);
                }
            }
            mutex.release();
        };

        Consumer<Exception> onError = (e) -> {
            try {
                logger.info("Trying to destroy environment due to an error:", e);
                startedEnv.destroyEnvironment();
                mutex.release();
            } catch (EnvironmentDriverException e1) {
                logger.error("Environment LEAK! The running environment was not destroyed. ID: " + startedEnv.getId(), e1);
            }
            fail("Failed to init builder. " + e.getMessage());
        };

        startedEnv.monitorInitialization(onEnvironmentStarted, onError);
        boolean completed = mutex.tryAcquire(TEST_EXECUTION_TIMEOUT, TimeUnit.SECONDS);

        Throwable exception = exceptionWrapper.get();
        if (exception != null) {
            logger.error("", exception);
            fail(exception.getMessage());
        }

        assertTrue("timeout reached, test has not complete.", completed);

    }

    private void destroyEnvironment(RunningEnvironment runningEnvironment) throws EnvironmentDriverException {
        logger.info("Destroying environment...");
        runningEnvironment.destroyEnvironment();
    }

    private void assertThatContainerIsRunning(final RunningEnvironment runningEnvironment) throws Exception {
        boolean connected = connectToPingUrl(runningEnvironment, 20);
        assertTrue("Environment wasn't successfully started.", connected);
    }

    private void assertThatContainerIsNotRunning(RunningEnvironment runningEnvironment) throws IOException {
        boolean timeoutReached = false;
        boolean connected = connectToPingUrl(runningEnvironment, 0);
        assertFalse("Environment [" + runningEnvironment.getId() + "] should be destroyed.", connected);
    }

    private boolean connectToPingUrl(RunningEnvironment runningEnvironment, int maxRepeats) throws IOException {
        URL url = new URL(runningEnvironment.getBuildAgentUrl());
        logger.info("Left {} attempts to connect to {}", maxRepeats, url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(500);
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();

        int responseCode = connection.getResponseCode();
        connection.disconnect();

        logger.info("Got {} from {}.", responseCode, url);
        if (responseCode == 200) {
            return true;
        } else {
            if (maxRepeats > 0) {
                return connectToPingUrl(runningEnvironment, maxRepeats - 1);
            } else {
                return false;
            }
        }
    }

    private void destroyEnvironmentWithReport(RunningEnvironment runningEnvironment) {
        try {
            logger.info("Trying to destroy environment!");
            destroyEnvironment(runningEnvironment);
        } catch (Exception e) {
            logger.error("Environment LEAK! The running environment was not removed stopped. ID: " + runningEnvironment.getId());
        }
    }

    private static class DummyRepositoryConfiguration implements RepositorySession {

        @Override
        public ArtifactRepo.Type getType() {
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
                    return "approx/deploy/url";
                }

                @Override
                public String getDependencyUrl() {
                    return "approx/dependencies/url";
                }
            };
        }

        @Override
        public RepositoryManagerResult extractBuildArtifacts() throws RepositoryManagerException {
            return null;
        }

    }

}
