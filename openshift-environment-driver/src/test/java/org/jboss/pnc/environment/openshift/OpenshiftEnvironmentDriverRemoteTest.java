/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015 Red Hat, Inc., and individual contributors
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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
//@RunWith(Arquillian.class)
@Category({ RemoteTest.class })
public class OpenshiftEnvironmentDriverRemoteTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final RepositorySession DUMMY_REPOSITORY_CONFIGURATION = new DummyRepositoryConfiguration();

    private static final int TEST_EXECUTION_TIMEOUT = 30;

    private final String pingUrl = "/";

    private final EnvironmentDriver environmentDriver;

    private final Configuration configurationService;

    public OpenshiftEnvironmentDriverRemoteTest() throws Exception {
        //workaround for protected root rest endpoint from where version should be read
        System.setProperty(DefaultClient.SYSTEM_PROP_OPENSHIFT_API_VERSION, "v1");

        configurationService = new Configuration();

        final Environment environment = new Environment(BuildType.JAVA, OperationalSystem.LINUX);
        environmentDriver = new OpenshiftEnvironmentDriver(configurationService, new PullingMonitor());
    }

    @Test
    public void createAndDestroyEnvironment() throws EnvironmentDriverException, InterruptedException {
        final Semaphore mutex = new Semaphore(0);
        ObjectWrapper<Throwable> exceptionWrapper = new ObjectWrapper<>();

        // Create container
        final Environment environment = new Environment(BuildType.JAVA, OperationalSystem.LINUX);
        final StartedEnvironment startedEnv = environmentDriver.buildEnvironment(environment, DUMMY_REPOSITORY_CONFIGURATION);

        Consumer<RunningEnvironment> onEnvironmentStarted = (runningEnvironment) -> {
            boolean containerDestroyed = false;
            try {
                assertThatContainerIsRunning(runningEnvironment);

                // Destroy container
                logger.info("Trying to destroy environment.");
                runningEnvironment.destroyEnvironment();
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
                logger.info("Trying to destroy environment due to an error.", e);
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

    private void assertThatContainerIsRunning(final RunningEnvironment runningEnvironment) throws Exception {
        HttpURLConnection connection = connectToPingUrl(runningEnvironment);
        assertEquals("Environment wasn't successfully started.", 200, connection.getResponseCode());
    }

    private void assertThatContainerIsNotRunning(RunningEnvironment runningEnvironment) throws IOException {
        boolean timeoutReached = false;
        try {
            HttpURLConnection connection = connectToPingUrl(runningEnvironment);
        } catch (java.net.SocketTimeoutException e) {
            timeoutReached = true;
        }
        assertTrue("Environment [" + runningEnvironment.getId() + "] should be destroyed.", timeoutReached);
    }

    private HttpURLConnection connectToPingUrl(RunningEnvironment runningEnvironment) throws IOException {
        URL url = new URL(runningEnvironment.getJenkinsUrl() + ":" + runningEnvironment.getJenkinsPort() + pingUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(1000);
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        return connection;
    }

    private void destroyEnvironmentWithReport(RunningEnvironment runningEnvironment) {
        try {
            logger.info("Trying to destroy environment!");
            runningEnvironment.destroyEnvironment();
        } catch (Exception e) {
            logger.error("Environment LEAK! The running environment was not removed stopped. ID: " + runningEnvironment.getId());
        }
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

        @Override
        public String getBuildSetRepositoryId() {
            return null;
        }

    }

}
