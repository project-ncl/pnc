/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.remotecoordinator.test;

import org.jboss.pnc.remotecoordinator.builder.SetRecordUpdateJob;
import org.jboss.pnc.remotecoordinator.test.event.TestCDIBuildSetStatusChangedReceiver;
import org.jboss.pnc.remotecoordinator.test.event.TestCDIBuildStatusChangedReceiver;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.MockUser;
import org.jboss.pnc.mock.model.builders.ArtifactBuilder;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.BuildRequestException;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@Dependent
public class ProjectBuilder {

    private static final Logger log = LoggerFactory.getLogger(ProjectBuilder.class);

    private static final int BUILD_SET_STATUS_UPDATES = 2;
    public static final int N_STATUS_UPDATES_PER_TASK = 3;
    public static final int N_STATUS_UPDATES_PER_TASK_WITH_DEPENDENCIES = 4; // additional WAITING_FOR_DEPENDENCIES
    public static final int N_STATUS_UPDATES_PER_TASK_WAITING_FOR_FAILED_DEPS = 2; // only REJECTED_FAILED_DEPENDENCIES
                                                                                   // and WAITING_FOR_DEPENDENCIES
    @Inject
    DatastoreMock datastore;

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Inject
    TestCDIBuildStatusChangedReceiver statusChangedReceiver;

    @Inject
    TestCDIBuildSetStatusChangedReceiver setStatusChangedReceiver;

    @Before
    public void setUp() {
        clearSemaphores();
    }

    BuildTask buildProject(
            BuildConfiguration buildConfiguration,
            BuildCoordinator buildCoordinator,
            Consumer<BuildStatusChangedEvent> onStatusUpdate)
            throws BuildConflictException, InterruptedException, CoreException, BuildRequestException {
        return buildProject(buildConfiguration, buildCoordinator, onStatusUpdate, new BuildOptions());
    }

    BuildTask buildProject(
            BuildConfiguration buildConfiguration,
            BuildCoordinator buildCoordinator,
            Consumer<BuildStatusChangedEvent> onStatusUpdate,
            BuildOptions buildOptions)
            throws BuildConflictException, InterruptedException, CoreException, BuildRequestException {

        log.debug("Building project {}", buildConfiguration.getName());
        List<BuildStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        Consumer<BuildStatusChangedEvent> onStatusUpdateInternal = (statusUpdate) -> {
            receivedStatuses.add(statusUpdate);
            onStatusUpdate.accept(statusUpdate);
        };

        // Defines a number of callbacks, which are executed after status update
        final Semaphore semaphore = registerReleaseListenersAndAcquireSemaphore(
                onStatusUpdateInternal,
                N_STATUS_UPDATES_PER_TASK);

        BuildSetTask taskSet = buildCoordinator.buildConfig(buildConfiguration, MockUser.newTestUser(1), buildOptions);
        Set<BuildTask> buildTasks = taskSet.getBuildTasks();
        assertThat(buildTasks).hasSize(1);
        BuildTask buildTask = buildTasks.iterator().next();
        log.info("Started build task {}", buildTask);

        assertBuildStartedSuccessfully(buildTask);
        waitForStatusUpdates(N_STATUS_UPDATES_PER_TASK, semaphore, "");
        return buildTask;
    }

    void buildProject(BuildConfiguration buildConfiguration, BuildCoordinator buildCoordinator)
            throws BuildConflictException, InterruptedException, CoreException, BuildRequestException {
        List<BuildStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        BuildTask buildTask = buildProject(buildConfiguration, buildCoordinator, receivedStatuses::add);
        assertAllStatusUpdateReceived(receivedStatuses, buildTask.getId());
    }

    void buildProjects(
            BuildConfigurationSet buildConfigurationSet,
            BuildCoordinator buildCoordinator,
            SetRecordUpdateJob setJob) throws InterruptedException, CoreException, DatastoreException {
        int nStatusUpdates = getNumberOfStatusUpdates(buildConfigurationSet);
        buildProjectsAndVerifyResult(
                buildConfigurationSet,
                buildCoordinator,
                setJob,
                nStatusUpdates,
                this::verifySuccessfulBuild);
    }

    BuildSetTask buildProjects(
            BuildConfigurationSet buildConfigurationSet,
            BuildCoordinator buildCoordinator,
            SetRecordUpdateJob setJob,
            Consumer<BuildStatusChangedEvent> onStatusUpdate)
            throws InterruptedException, CoreException, DatastoreException {
        int nStatusUpdates = getNumberOfStatusUpdates(buildConfigurationSet);
        return buildProjects(buildConfigurationSet, buildCoordinator, setJob, nStatusUpdates, onStatusUpdate);
    }

    BuildSetTask buildProjects(
            BuildConfigurationSet buildConfigurationSet,
            BuildCoordinator buildCoordinator,
            SetRecordUpdateJob setJob,
            Consumer<BuildStatusChangedEvent> onStatusUpdate,
            int skippedUpdates) throws InterruptedException, CoreException, DatastoreException {
        int nStatusUpdates = getNumberOfStatusUpdates(buildConfigurationSet) - skippedUpdates;
        return buildProjects(buildConfigurationSet, buildCoordinator, setJob, nStatusUpdates, onStatusUpdate);
    }

    private int getNumberOfStatusUpdates(BuildConfigurationSet buildConfigurationSet) {
        Set<BuildConfiguration> configs = buildConfigurationSet.getBuildConfigurations();
        int numCompletedBuilds = configs.size();
        int numBuildsWithDependencies = (int) configs.stream().filter(c -> c.dependsOnAny(configs)).count();
        return N_STATUS_UPDATES_PER_TASK * (numCompletedBuilds - numBuildsWithDependencies)
                + numBuildsWithDependencies * N_STATUS_UPDATES_PER_TASK_WITH_DEPENDENCIES;
    }

    private void verifySuccessfulBuild(List<BuildStatusChangedEvent> receivedStatuses, BuildSetTask buildSetTask) {
        buildSetTask.getBuildTasks().forEach(bt -> assertAllStatusUpdateReceived(receivedStatuses, bt.getId()));
    }

    void buildFailingProject(
            BuildConfigurationSet buildConfigurationSet,
            int numCompletedBuilds,
            BuildCoordinator buildCoordinator,
            SetRecordUpdateJob setJob) throws InterruptedException, CoreException, DatastoreException {
        buildFailingProject(buildConfigurationSet, numCompletedBuilds, 1, buildCoordinator, setJob);
    }

    void buildFailingProject(
            BuildConfigurationSet buildConfigurationSet,
            int numCompletedBuilds,
            int numFailedBuilds,
            BuildCoordinator buildCoordinator,
            SetRecordUpdateJob setJob) throws InterruptedException, CoreException, DatastoreException {
        int nStatusUpdates = N_STATUS_UPDATES_PER_TASK * numCompletedBuilds
                + N_STATUS_UPDATES_PER_TASK_WAITING_FOR_FAILED_DEPS * numFailedBuilds;
        buildProjectsAndVerifyResult(
                buildConfigurationSet,
                buildCoordinator,
                setJob,
                nStatusUpdates,
                this::verifyFailingProject);
    }

    private void verifyFailingProject(List<BuildStatusChangedEvent> receivedStatuses, BuildSetTask buildSetTask) {
        buildSetTask.getBuildTasks()
                .stream()
                .filter(b -> BuildCoordinationStatus.DONE_WITH_ERRORS.equals(b.getStatus()))
                .forEach(
                        bt -> ProjectBuilder.this
                                .assertAllStatusUpdateReceivedForFailedBuild(receivedStatuses, bt.getId()));
        buildSetTask.getBuildTasks()
                .stream()
                .filter(b -> BuildCoordinationStatus.REJECTED.equals(b.getStatus()))
                .forEach(
                        bt -> ProjectBuilder.this
                                .assertAllStatusUpdateReceivedForFailedWaitingForDeps(receivedStatuses, bt.getId()));
    }

    int i = 0;

    private BuildSetTask buildProjectsAndWaitForUpdates(
            BuildConfigurationSet buildConfigurationSet,
            BuildCoordinator buildCoordinator,
            SetRecordUpdateJob setJob,
            int nStatusUpdates,
            Consumer<BuildStatusChangedEvent> onStatusUpdate,
            List<BuildStatusChangedEvent> receivedStatuses,
            List<BuildSetStatusChangedEvent> receivedSetStatuses) throws InterruptedException, CoreException {
        Consumer<BuildStatusChangedEvent> onStatusUpdateInternal = (statusUpdate) -> {
            log.debug("Received status change event [" + (i++) + "]: {}.", statusUpdate);
            receivedStatuses.add(statusUpdate);
            onStatusUpdate.accept(statusUpdate);
        };

        final Semaphore semaphore = registerReleaseListenersAndAcquireSemaphore(onStatusUpdateInternal, nStatusUpdates);
        final Semaphore buildSetSemaphore = registerBuildSetListeners(receivedSetStatuses, BUILD_SET_STATUS_UPDATES);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.FORCE);
        BuildSetTask buildSetTask = buildCoordinator
                .buildSet(buildConfigurationSet, MockUser.newTestUser(1), buildOptions);

        assertBuildStartedSuccessfully(buildSetTask);

        log.info("Waiting to receive all {} status updates...", nStatusUpdates);
        waitForStatusUpdates(nStatusUpdates, semaphore, "");
        log.debug(
                "All status updates should be received. Semaphore has {} free entries.",
                semaphore.availablePermits());
        setJob.updateConfigSetRecordsStatuses();
        log.info("Waiting to receive all {} build set status updates...", BUILD_SET_STATUS_UPDATES);
        waitForStatusUpdates(BUILD_SET_STATUS_UPDATES, buildSetSemaphore, "build set task: " + buildSetTask);
        log.debug(
                "All status updates should be received. Semaphore has {} free entries.",
                semaphore.availablePermits());
        return buildSetTask;
    }

    private BuildSetTask buildProjects(
            BuildConfigurationSet buildConfigurationSet,
            BuildCoordinator buildCoordinator,
            SetRecordUpdateJob setJob,
            int nStatusUpdates,
            Consumer<BuildStatusChangedEvent> onStatusUpdate) throws InterruptedException, CoreException {
        log.info("Building configuration set {}", buildConfigurationSet.getName());
        List<BuildStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();
        List<BuildSetStatusChangedEvent> receivedSetStatuses = new CopyOnWriteArrayList<>();

        return buildProjectsAndWaitForUpdates(
                buildConfigurationSet,
                buildCoordinator,
                setJob,
                nStatusUpdates,
                onStatusUpdate,
                receivedStatuses,
                receivedSetStatuses);
    }

    private void buildProjectsAndVerifyResult(
            BuildConfigurationSet buildConfigurationSet,
            BuildCoordinator buildCoordinator,
            SetRecordUpdateJob setJob,
            int nStatusUpdates,
            Verifier verifier) throws InterruptedException, CoreException {
        log.info("Building configuration set {}", buildConfigurationSet.getName());
        List<BuildStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();
        List<BuildSetStatusChangedEvent> receivedSetStatuses = new CopyOnWriteArrayList<>();
        BuildSetTask buildSetTask = buildProjectsAndWaitForUpdates(
                buildConfigurationSet,
                buildCoordinator,
                setJob,
                nStatusUpdates,
                x -> {},
                receivedStatuses,
                receivedSetStatuses);

        log.info("Checking if received all status updates...");
        verifier.verify(receivedStatuses, buildSetTask);
    }

    private Semaphore registerReleaseListenersAndAcquireSemaphore(
            Consumer<BuildStatusChangedEvent> onStatusUpdate,
            int nStatusUpdates) throws InterruptedException {

        final Semaphore semaphore = new Semaphore(nStatusUpdates);
        statusChangedReceiver.addBuildStatusChangedEventListener(statusUpdate -> {
            log.debug("Received status update {}.", statusUpdate.toString());
            onStatusUpdate.accept(statusUpdate);
            semaphore.release(1);
            log.debug("Semaphore released, there are {} free entries", semaphore.availablePermits());
        });
        semaphore.acquire(nStatusUpdates);
        return semaphore;
    }

    private Semaphore registerBuildSetListeners(List<BuildSetStatusChangedEvent> events, int nStatusUpdates)
            throws InterruptedException {
        final Semaphore semaphore = new Semaphore(nStatusUpdates);
        setStatusChangedReceiver.addBuildSetStatusChangedEventListener(statusUpdate -> {
            log.debug("Received set status update {}.", statusUpdate.toString());
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
            fail(
                    "Build " + buildTask.getId() + " has status:" + buildTask.getStatus() + " with description: "
                            + buildTask.getStatusDescription());
        }
    }

    private void assertBuildStartedSuccessfully(BuildSetTask buildSetTask) {
        if (buildSetTask.getTaskStatus().isFinal() && !buildSetTask.getTaskStatus().completedSuccessfully()) {
            fail(
                    "Build " + buildSetTask.getBuildConfigSetRecord().get().getId() + " has status:"
                            + buildSetTask.getTaskStatus() + " with description: "
                            + buildSetTask.getStatusDescription());
        }
    }

    private void waitForStatusUpdates(int nStatusUpdates, Semaphore semaphore, String message)
            throws InterruptedException {
        if (!semaphore.tryAcquire(nStatusUpdates, 15, TimeUnit.SECONDS)) { // wait for callback to release
            fail(
                    "Timeout while waiting for status updates. " + "Received " + semaphore.availablePermits() + " of "
                            + nStatusUpdates + " status updates." + message);
        }
    }

    private void assertAllStatusUpdateReceived(List<BuildStatusChangedEvent> receivedStatuses, String buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.ENQUEUED, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILDING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.SUCCESS, buildTaskId);
    }

    private void assertAllStatusUpdateReceivedForFailedBuild(
            List<BuildStatusChangedEvent> receivedStatuses,
            String buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.ENQUEUED, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILDING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.FAILED, buildTaskId);
    }

    private void assertAllStatusUpdateReceivedForFailedWaitingForDeps(
            List<BuildStatusChangedEvent> receivedStatuses,
            String buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.REJECTED, buildTaskId);
    }

    void assertStatusUpdateReceived(
            List<BuildStatusChangedEvent> receivedStatusEvents,
            BuildStatus status,
            String buildTaskId) {
        boolean received = false;
        for (BuildStatusChangedEvent receivedStatusEvent : receivedStatusEvents) {
            if (receivedStatusEvent.getBuild().getId().equals(buildTaskId)
                    && receivedStatusEvent.getNewStatus().equals(status)) {
                received = true;
                break;
            }
        }
        assertTrue("Did not receive update for status: " + status + " for BuildTaskId: " + buildTaskId, received);
    }

    public void clearSemaphores() {
        setStatusChangedReceiver.clear();
        statusChangedReceiver.clear();
    }

    public static void assertArtifactsPresent(Set<Artifact> artifacts) {
        assertTrue("Missing artifacts.", artifacts.size() > 0);
        Artifact artifact = artifacts.iterator().next();
        assertTrue(
                "Invalid artifact in result.",
                artifact.getIdentifier().startsWith(ArtifactBuilder.IDENTIFIER_PREFIX));
    }

    interface Verifier {
        void verify(List<BuildStatusChangedEvent> receivedStatuses, BuildSetTask buildSetTask);
    }
}
