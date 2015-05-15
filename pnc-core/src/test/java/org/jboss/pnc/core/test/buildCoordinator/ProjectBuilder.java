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
package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildSetTask;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.test.buildCoordinator.event.TestCDIBuildStatusChangedReceiver;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.core.test.mock.DatastoreMock;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
public class ProjectBuilder {

    @Inject
    BuildCoordinator buildCoordinator;

    @Inject
    DatastoreMock datastore;

    @Inject
    TestCDIBuildStatusChangedReceiver statusChangedReceiver;

    private static final Logger log = LoggerFactory.getLogger(ProjectBuilder.class);
    public static final int N_STATUS_UPDATES = 13;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(Configuration.class)
                .addClass(Environment.Builder.class)
                .addClass(TestCDIBuildStatusChangedReceiver.class)
                .addPackages(true, BuildDriverFactory.class.getPackage(), BuildDriverMock.class.getPackage(),
                        ContentIdentityManager.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties");

        log.debug(jar.toString(true));
        return jar;
    }

    void buildProject(BuildConfiguration buildConfiguration) throws InterruptedException, CoreException {
        log.info("Building project {}", buildConfiguration.getName());
        List<BuildStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        //Defines a number of callbacks, which are executed after buildStatus update

        final Semaphore semaphore = registerReleaseListenersAndAcquireSemaphore(receivedStatuses, N_STATUS_UPDATES);

        User user = null;
        BuildTask buildTask = buildCoordinator.build(buildConfiguration, user);

        assertBuildStartedSuccessfully(buildTask);
        waitForStatusUpdates(N_STATUS_UPDATES, semaphore);
        assertAllStatusUpdateReceived(receivedStatuses, buildConfiguration.getId());
    }

    void buildProjects(BuildConfigurationSet buildConfigurationSet) throws InterruptedException, CoreException {
        log.info("Building configuration set {}", buildConfigurationSet.getName());
        List<BuildStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        //Defines a number of callbacks, which are executed after buildStatus update
        final int nStatusUpdates = N_STATUS_UPDATES * buildConfigurationSet.getBuildConfigurations().size();

        final Semaphore semaphore = registerReleaseListenersAndAcquireSemaphore(receivedStatuses, nStatusUpdates);

        User user = null;
        BuildSetTask buildSetTask = buildCoordinator.build(buildConfigurationSet, user);

        assertBuildStartedSuccessfully(buildSetTask);

        log.info("Waiting to receive all {} status updates...", nStatusUpdates);
        waitForStatusUpdates(nStatusUpdates, semaphore);

        log.info("Checking if received all status updates...");
        buildConfigurationSet.getBuildConfigurations().forEach(bc -> assertAllStatusUpdateReceived(receivedStatuses, bc.getId()));
    }

    private Semaphore registerReleaseListenersAndAcquireSemaphore(List<BuildStatusChangedEvent> receivedStatuses, int nStatusUpdates) throws InterruptedException {
        final Semaphore semaphore = new Semaphore(nStatusUpdates);
        statusChangedReceiver.addBuildStatusChangedEventListener(statusUpdate -> {
            log.debug("Received status update {}.", statusUpdate.toString());
            if (!BuildStatus.WAITING_FOR_DEPENDENCIES.equals(statusUpdate.getNewStatus())) {
                receivedStatuses.add(statusUpdate);
                semaphore.release(1);
                log.debug("Semaphore released, there are {} free entries", semaphore.availablePermits());
            } else {
                log.debug("Skipping {} status update", statusUpdate);
            }
        });
        semaphore.acquire(nStatusUpdates);
        return semaphore;
    }

    private void assertBuildStartedSuccessfully(BuildTask buildTask) {
        List<BuildStatus> errorStates = Arrays.asList(BuildStatus.REJECTED, BuildStatus.SYSTEM_ERROR, BuildStatus.BUILD_ENV_SETUP_COMPLETE_WITH_ERROR);
        if (errorStates.contains(buildTask.getStatus())) {
            fail("Build " + buildTask.getId() + " has status:" + buildTask.getStatus() + " with description: " + buildTask.getStatusDescription());
        }
    }

    private void assertBuildStartedSuccessfully(BuildSetTask buildSetTask) {
        List<BuildStatus> errorStates = Arrays.asList(BuildStatus.REJECTED, BuildStatus.SYSTEM_ERROR, BuildStatus.BUILD_ENV_SETUP_COMPLETE_WITH_ERROR);
        if (errorStates.contains(buildSetTask.getStatus())) {
            fail("Build " + buildSetTask.getId() + " has status:" + buildSetTask.getStatus() + " with description: " + buildSetTask.getStatusDescription());
        }
    }

    private void waitForStatusUpdates(int nStatusUpdates, Semaphore semaphore) throws InterruptedException {
        if (!semaphore.tryAcquire(nStatusUpdates, 15, TimeUnit.SECONDS)) { //wait for callback to release
            fail("Timeout while waiting for status updates. Received " + semaphore.availablePermits() + " of " + nStatusUpdates + " status updates.");
        }
    }

    private void assertAllStatusUpdateReceived(List<BuildStatusChangedEvent> receivedStatuses, Integer configurationId) {
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_ENV_SETTING_UP, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_ENV_WAITING, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.REPO_SETTING_UP, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_SETTING_UP, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_WAITING, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_COMPLETED_SUCCESS, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_ENV_DESTROYING, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_ENV_DESTROYED, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.STORING_RESULTS, configurationId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.DONE, configurationId);
    }

    private void assertStatusUpdateReceived(List<BuildStatusChangedEvent> receivedStatusEvents, BuildStatus status, Integer configurationId) {
        boolean received = false;
        for (BuildStatusChangedEvent receivedStatusEvent : receivedStatusEvents) {
            if (receivedStatusEvent.getBuildConfigurationId().equals(configurationId) &&
                    receivedStatusEvent.getNewStatus().equals(status)) {
                received = true;
                break;
            }
        }
        assertTrue("Did not received update for status: " + status + " for BuildConfiguration: " + configurationId, received);
    }

    protected void assertBuildArtifactsPresent(List<Artifact> builtArtifacts) {
        assertTrue("Missing built artifacts.", builtArtifacts.size() > 0);
        Artifact artifact = builtArtifacts.get(0);
        assertTrue("Invalid built artifact in result.", artifact.getIdentifier().startsWith("test"));
    }

}
