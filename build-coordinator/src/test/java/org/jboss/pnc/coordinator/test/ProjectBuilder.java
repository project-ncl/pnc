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
package org.jboss.pnc.coordinator.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.pnc.coordinator.builder.BuildCoordinator;
import org.jboss.pnc.coordinator.builder.BuildSetTask;
import org.jboss.pnc.coordinator.builder.BuildTask;
import org.jboss.pnc.coordinator.test.event.TestCDIBuildSetStatusChangedReceiver;
import org.jboss.pnc.coordinator.test.event.TestCDIBuildStatusChangedReceiver;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.builders.ArtifactBuilder;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.mock.MockUser;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
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

    public static final int BUILD_SET_STATUS_UPDATES = 2;
    @Inject
    BuildCoordinator buildCoordinator;

    @Inject
    DatastoreMock datastore;

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Inject
    TestCDIBuildStatusChangedReceiver statusChangedReceiver;

    @Inject
    TestCDIBuildSetStatusChangedReceiver setStatusChangedReceiver;

    private static final Logger log = LoggerFactory.getLogger(ProjectBuilder.class);
    public static final int N_STATUS_UPDATES_PER_TASK = 3;
    public static final int N_STATUS_UPDATES_PER_TASK_WAITING_FOR_FAILED_DEPS = 1;

    @Before
    public void setUp() {
        clearSemaphores();
    }

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(BuildCoordinatorDeployments.Options.WITH_DATASTORE);
    }

    void buildProject(BuildConfiguration buildConfiguration, BuildCoordinator buildCoordinator) throws BuildConflictException, InterruptedException {
        log.debug("Building project {}", buildConfiguration.getName());
        List<BuildCoordinationStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        //Defines a number of callbacks, which are executed after buildStatus update
        final Semaphore semaphore = registerReleaseListenersAndAcquireSemaphore(receivedStatuses, N_STATUS_UPDATES_PER_TASK);


        BuildTask buildTask = buildCoordinator.build(buildConfiguration, MockUser.newTestUser(1), false);
        log.info("Started build task {}", buildTask);

        assertBuildStartedSuccessfully(buildTask);
        waitForStatusUpdates(N_STATUS_UPDATES_PER_TASK, semaphore, "");
        assertAllStatusUpdateReceived(receivedStatuses, buildTask.getId());
    }

    @Deprecated //provide your own instance of BC
    void buildProjects(BuildConfigurationSet buildConfigurationSet) throws InterruptedException, CoreException, DatastoreException {
        buildProjects(buildConfigurationSet, buildCoordinator);
    }

    void buildProjects(BuildConfigurationSet buildConfigurationSet, BuildCoordinator buildCoordinator) throws InterruptedException, CoreException, DatastoreException {
        int numCompletedBuilds = buildConfigurationSet.getBuildConfigurations().size();
        int nStatusUpdates = N_STATUS_UPDATES_PER_TASK * numCompletedBuilds;
        buildProjectsAndVerifyResult(buildConfigurationSet, buildCoordinator, nStatusUpdates, this::verifySuccessfulBuild);
    }

    private void verifySuccessfulBuild(List<BuildCoordinationStatusChangedEvent> receivedStatuses, BuildSetTask buildSetTask) {
        buildSetTask.getBuildTasks().forEach(bt -> assertAllStatusUpdateReceived(receivedStatuses, bt.getId()));
    }

    void buildFailingProject(BuildConfigurationSet buildConfigurationSet, int numCompletedBuilds) throws InterruptedException, CoreException, DatastoreException {
        int nStatusUpdates = N_STATUS_UPDATES_PER_TASK * numCompletedBuilds + N_STATUS_UPDATES_PER_TASK_WAITING_FOR_FAILED_DEPS;
        buildProjectsAndVerifyResult(buildConfigurationSet, buildCoordinator, nStatusUpdates, this::verifyFailingProject);
    }

    private void verifyFailingProject(List<BuildCoordinationStatusChangedEvent> receivedStatuses, BuildSetTask buildSetTask) {
        buildSetTask.getBuildTasks().stream()
                .filter(b -> BuildCoordinationStatus.DONE_WITH_ERRORS.equals(b.getStatus()))
                .forEach(bt -> ProjectBuilder.this.assertAllStatusUpdateReceivedForFailedBuild(receivedStatuses, bt.getId()));
        buildSetTask.getBuildTasks().stream()
                .filter(b -> BuildCoordinationStatus.REJECTED.equals(b.getStatus()))
                .forEach(bt -> ProjectBuilder.this.assertAllStatusUpdateReceivedForFailedWaitingForDeps(receivedStatuses, bt.getId()));
    }

    private void buildProjectsAndVerifyResult(BuildConfigurationSet buildConfigurationSet, BuildCoordinator buildCoordinator, int nStatusUpdates, Verifier verifier) throws InterruptedException, CoreException {
        log.info("Building configuration set {}", buildConfigurationSet.getName());
        List<BuildCoordinationStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();
        List<BuildSetStatusChangedEvent> receivedSetStatuses = new CopyOnWriteArrayList<>();

        //Defines a number of callbacks, which are executed after buildStatus update

        final Semaphore semaphore = registerReleaseListenersAndAcquireSemaphore(receivedStatuses, nStatusUpdates);
        final Semaphore buildSetSemaphore = registerBuildSetListeners(receivedSetStatuses, BUILD_SET_STATUS_UPDATES);

        BuildSetTask buildSetTask = buildCoordinator.build(buildConfigurationSet, MockUser.newTestUser(1), true);

        assertBuildStartedSuccessfully(buildSetTask);

        log.info("Waiting to receive all {} status updates...", nStatusUpdates);
        waitForStatusUpdates(nStatusUpdates, semaphore, "");
        log.debug("All status updates should be received. Semaphore has {} free entries.", semaphore.availablePermits());

        log.info("Waiting to receive all {} build set status updates...", BUILD_SET_STATUS_UPDATES);
        waitForStatusUpdates(BUILD_SET_STATUS_UPDATES, buildSetSemaphore, "build set task: " + buildSetTask);
        log.debug("All status updates should be received. Semaphore has {} free entries.", semaphore.availablePermits());

        log.info("Checking if received all status updates...");
        verifier.verify(receivedStatuses, buildSetTask);
    }

    private Semaphore registerReleaseListenersAndAcquireSemaphore(List<BuildCoordinationStatusChangedEvent> receivedStatuses,
                                                                  int nStatusUpdates) throws InterruptedException {
        final Semaphore semaphore = new Semaphore(nStatusUpdates);
        statusChangedReceiver.addBuildStatusChangedEventListener(statusUpdate -> {
            log.debug("Received status update {}.", statusUpdate.toString());
            if (!BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES.equals(statusUpdate.getNewStatus())) {
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

    private Semaphore registerBuildSetListeners(List<BuildSetStatusChangedEvent> events,
                                                int nStatusUpdates) throws InterruptedException {
        final Semaphore semaphore = new Semaphore(nStatusUpdates);
        setStatusChangedReceiver.addBuildSetStatusChangedEventListener(statusUpdate -> {
            log.debug("Received status update {}.", statusUpdate.toString());
            events.add(statusUpdate);
            semaphore.release(1);
            log.debug("Semaphore released, there are {} free entries", semaphore.availablePermits());
        });
        semaphore.acquire(nStatusUpdates);
        return semaphore;
    }

    private void assertBuildStartedSuccessfully(BuildTask buildTask) {
        List<BuildCoordinationStatus> errorStates = Arrays.asList(
                BuildCoordinationStatus.REJECTED,
                BuildCoordinationStatus.REJECTED_ALREADY_BUILT,
                BuildCoordinationStatus.SYSTEM_ERROR,
                BuildCoordinationStatus.DONE_WITH_ERRORS);
        if (errorStates.contains(buildTask.getStatus())) {
            fail("Build " + buildTask.getId() + " has status:" + buildTask.getStatus() + " with description: " + buildTask.getStatusDescription());
        }
    }

    private void assertBuildStartedSuccessfully(BuildSetTask buildSetTask) {
        List<BuildSetStatus> errorStates = Collections.singletonList(BuildSetStatus.REJECTED);
        if (errorStates.contains(buildSetTask.getStatus())) {
            fail("Build " + buildSetTask.getId() + " has status:" + buildSetTask.getStatus() + " with description: " + buildSetTask.getStatusDescription());
        }
    }

    private void waitForStatusUpdates(int nStatusUpdates, Semaphore semaphore, String message) throws InterruptedException {
        if (!semaphore.tryAcquire(nStatusUpdates, 15, TimeUnit.SECONDS)) { //wait for callback to release
            fail("Timeout while waiting for status updates. Received " + semaphore.availablePermits() + " of " + nStatusUpdates + " status updates." + message);
        }
    }

    private void assertAllStatusUpdateReceived(List<BuildCoordinationStatusChangedEvent> receivedStatuses, Integer buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILDING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_COMPLETED, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.DONE, buildTaskId);
    }

    private void assertAllStatusUpdateReceivedForFailedBuild(List<BuildCoordinationStatusChangedEvent> receivedStatuses, Integer buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILDING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_COMPLETED, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.DONE_WITH_ERRORS, buildTaskId);
    }

    private void assertAllStatusUpdateReceivedForFailedWaitingForDeps(List<BuildCoordinationStatusChangedEvent> receivedStatuses, Integer buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.REJECTED, buildTaskId);
    }

    private void assertStatusUpdateReceived(List<BuildCoordinationStatusChangedEvent> receivedStatusEvents, BuildCoordinationStatus status, Integer buildTaskId) {
        boolean received = false;
        for (BuildCoordinationStatusChangedEvent receivedStatusEvent : receivedStatusEvents) {
            if (receivedStatusEvent.getBuildTaskId().equals(buildTaskId) &&
                    receivedStatusEvent.getNewStatus().equals(status)) {
                received = true;
                break;
            }
        }
        assertTrue("Did not received update for status: " + status + " for BuildTaskId: " + buildTaskId, received);
    }

    public void clearSemaphores() {
        setStatusChangedReceiver.clear();
        statusChangedReceiver.clear();
    }

    public static void assertArtifactsPresent(List<Artifact> builtArtifacts) {
        assertTrue("Missing artifacts.", builtArtifacts.size() > 0);
        Artifact artifact = builtArtifacts.get(0);
        assertTrue("Invalid artifact in result.", artifact.getIdentifier().startsWith(ArtifactBuilder.IDENTIFIER_PREFIX));
    }

    interface Verifier {
        void verify(List<BuildCoordinationStatusChangedEvent> receivedStatuses, BuildSetTask buildSetTask);
    }
}
