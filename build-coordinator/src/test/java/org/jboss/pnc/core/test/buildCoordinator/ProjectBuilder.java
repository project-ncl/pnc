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
import org.jboss.pnc.core.builder.coordinator.BuildCoordinator;
import org.jboss.pnc.core.builder.coordinator.BuildSetTask;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.core.builder.coordinator.filtering.BuildTaskFilter;
import org.jboss.pnc.core.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.core.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.core.test.buildCoordinator.event.TestCDIBuildStatusChangedReceiver;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.mock.model.builders.TestEntitiesFactory;
import org.jboss.pnc.mock.executor.BuildExecutorMock;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.mock.MockUser;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
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
    TestProjectConfigurationBuilder configurationBuilder;

    @Inject
    TestCDIBuildStatusChangedReceiver statusChangedReceiver;

    private static final Logger log = LoggerFactory.getLogger(ProjectBuilder.class);
    public static final int N_STATUS_UPDATES_PER_TASK = 4;
    public static final int N_STATUS_UPDATES_PER_TASK_WAITING_FOR_FAILED_DEPS = 1;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = BuildCoordinatorDeployments.defaultDeployment();
                jar.addPackage(BuildExecutorMock.class.getPackage())
                .addClass(Configuration.class)
                .addClass(BuildEnvironment.Builder.class)
                .addPackages(true,
                        BuildCoordinator.class.getPackage(),
                        DatastoreAdapter.class.getPackage(),
                        TestProjectConfigurationBuilder.class.getPackage(),
                        ContentIdentityManager.class.getPackage(),
                        BuildConfigSetRecordRepository.class.getPackage(),
                        BuildTaskFilter.class.getPackage(),
                        TestCDIBuildStatusChangedReceiver.class.getPackage(),
                        BuildSetCallBack.class.getPackage(),
                        BuildCallBack.class.getPackage(),
                        BuildCoordinationStatus.class.getPackage(),
                        DefaultBuildStatusChangedEvent.class.getPackage(),
                        DatastoreMock.class.getPackage())
                .addClass(Datastore.class)
                .addClass(TestEntitiesFactory.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("simplelogger.properties");

        log.debug(jar.toString(true));
        return jar;
    }

    void buildProject(BuildConfiguration buildConfiguration) throws BuildConflictException, InterruptedException {
        log.debug("Building project {}", buildConfiguration.getName());
        List<BuildCoordinationStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        //Defines a number of callbacks, which are executed after buildStatus update
        final Semaphore semaphore = registerReleaseListenersAndAcquireSemaphore(receivedStatuses, N_STATUS_UPDATES_PER_TASK);


        BuildTask buildTask = buildCoordinator.build(buildConfiguration, MockUser.newTestUser(1), false);
        log.info("Started build task {}", buildTask);

        assertBuildStartedSuccessfully(buildTask);
        waitForStatusUpdates(N_STATUS_UPDATES_PER_TASK, semaphore);
        assertAllStatusUpdateReceived(receivedStatuses, buildTask.getId());
    }

    void buildProjects(BuildConfigurationSet buildConfigurationSet) throws InterruptedException, CoreException, DatastoreException {
        log.info("Building configuration set {}", buildConfigurationSet.getName());
        List<BuildCoordinationStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        //Defines a number of callbacks, which are executed after buildStatus update
        final int nStatusUpdates = N_STATUS_UPDATES_PER_TASK * buildConfigurationSet.getBuildConfigurations().size();

        final Semaphore semaphore = registerReleaseListenersAndAcquireSemaphore(receivedStatuses, nStatusUpdates);

        BuildSetTask buildSetTask = buildCoordinator.build(buildConfigurationSet, MockUser.newTestUser(1), true);

        assertBuildStartedSuccessfully(buildSetTask);

        log.info("Waiting to receive all {} status updates...", nStatusUpdates);
        waitForStatusUpdates(nStatusUpdates, semaphore);
        log.debug("All status updates should be received. Semaphore has {} free entries.", semaphore.availablePermits());

        log.info("Checking if received all status updates...");
        buildSetTask.getBuildTasks().forEach(bt -> assertAllStatusUpdateReceived(receivedStatuses, bt.getId()));
    }

    void buildFailingProject(BuildConfigurationSet buildConfigurationSet, int numCompletedBuilds, int numLackingDependecies) throws InterruptedException, CoreException, DatastoreException {
        log.info("Building configuration set {}", buildConfigurationSet.getName());
        List<BuildCoordinationStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        //Defines a number of callbacks, which are executed after buildStatus update
        final int nStatusUpdates = N_STATUS_UPDATES_PER_TASK * numCompletedBuilds + N_STATUS_UPDATES_PER_TASK_WAITING_FOR_FAILED_DEPS;

        final Semaphore semaphore = registerReleaseListenersAndAcquireSemaphore(receivedStatuses, nStatusUpdates);

        BuildSetTask buildSetTask = buildCoordinator.build(buildConfigurationSet, MockUser.newTestUser(1), true);

        assertBuildStartedSuccessfully(buildSetTask);
        log.info("Waiting to receive all {} status updates...", nStatusUpdates);
        waitForStatusUpdates(nStatusUpdates, semaphore);
        log.info("Checking if received all status updates...");
        buildSetTask.getBuildTasks().stream().filter(b -> BuildCoordinationStatus.DONE_WITH_ERRORS.equals(b.getStatus())).forEach(bt -> assertAllStatusUpdateReceivedForFailedBuild(receivedStatuses, bt.getId()));
        buildSetTask.getBuildTasks().stream().filter(b -> BuildCoordinationStatus.REJECTED.equals(b.getStatus())).forEach(bt -> assertAllStatusUpdateReceivedForFailedWaitingForDeps(receivedStatuses, bt.getId()));
    }

    private Semaphore registerReleaseListenersAndAcquireSemaphore(List<BuildCoordinationStatusChangedEvent> receivedStatuses, int nStatusUpdates) throws InterruptedException {
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
        List<BuildSetStatus> errorStates = Arrays.asList(BuildSetStatus.REJECTED);
        if (errorStates.contains(buildSetTask.getStatus())) {
            fail("Build " + buildSetTask.getId() + " has status:" + buildSetTask.getStatus() + " with description: " + buildSetTask.getStatusDescription());
        }
    }

    private void waitForStatusUpdates(int nStatusUpdates, Semaphore semaphore) throws InterruptedException {
        if (!semaphore.tryAcquire(nStatusUpdates, 15, TimeUnit.SECONDS)) { //wait for callback to release
            fail("Timeout while waiting for status updates. Received " + semaphore.availablePermits() + " of " + nStatusUpdates + " status updates.");
        }
    }

    private void assertAllStatusUpdateReceived(List<BuildCoordinationStatusChangedEvent> receivedStatuses, Integer buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.NEW, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILDING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.STORING_RESULTS, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.DONE, buildTaskId);
    }

    private void assertAllStatusUpdateReceivedForFailedBuild(List<BuildCoordinationStatusChangedEvent> receivedStatuses, Integer buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.NEW, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILDING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.STORING_RESULTS, buildTaskId);
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

    public static void assertBuildArtifactsPresent(List<Artifact> builtArtifacts) {
        assertTrue("Missing built artifacts.", builtArtifacts.size() > 0);
        Artifact artifact = builtArtifacts.get(0);
        assertTrue("Invalid built artifact in result.", artifact.getIdentifier().startsWith("test"));
    }
}
