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
package org.jboss.pnc.remotecoordinator.test.event;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.remotecoordinator.builder.BuildTasksInitializer;
import org.jboss.pnc.remotecoordinator.builder.SetRecordUpdateJob;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.remotecoordinator.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.remotecoordinator.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.remotecoordinator.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.remotecoordinator.test.BuildCoordinatorDeployments;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.mock.spi.RepositoryManagerResultMock;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.*;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class StatusUpdatesTest {

    private static final int MIN_TASKS = 3;

    private static final Logger log = LoggerFactory.getLogger(StatusUpdatesTest.class);

    @Inject
    @RemoteBuildCoordinator
    BuildCoordinator buildCoordinator;

    @Inject
    TestCDIBuildSetStatusChangedReceiver testCDIBuildSetStatusChangedReceiver;

    @Inject
    BuildStatusNotifications buildStatusNotifications;

    @Inject
    BuildSetStatusNotifications buildSetStatusNotifications;

    @Inject
    Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Inject
    DatastoreAdapter datastoreAdapter;

    @Inject
    BuildTaskRepository taskRepository;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    @Inject
    SetRecordUpdateJob setRecordUpdateJob;

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
    }

    @Test
    @InSequence(10)
    public void buildSetStatusShouldUpdateWhenAllBuildStatusChangeToCompletedState()
            throws DatastoreException, InterruptedException, CoreException {
        ObjectWrapper<BuildSetStatusChangedEvent> receivedBuildSetStatusChangedEvent = new ObjectWrapper<>();
        Consumer<BuildSetStatusChangedEvent> statusUpdateListener = receivedBuildSetStatusChangedEvent::set;
        testCDIBuildSetStatusChangedReceiver.addBuildSetStatusChangedEventListener(statusUpdateListener);

        User user = User.Builder.newBuilder().id(1).username("test-user-1").build();
        Set<BuildTask> buildTasks = initializeBuildTaskSet(configurationBuilder, user, (buildConfigSetRecord) -> {})
                .getBuildTasks();
        buildTasks.forEach((bt) -> {
            buildCoordinator.updateBuildTaskStatus(bt, BuildCoordinationStatus.DONE);
            buildCoordinator.completeBuild(bt, createBuildResult());
        });
        this.waitForConditionWithTimeout(() -> buildTasks.stream().allMatch(task -> task.getStatus().isCompleted()), 4);
        setRecordUpdateJob.updateConfigSetRecordsStatuses();
        Assert.assertNotNull("Did not receive build set status update.", receivedBuildSetStatusChangedEvent.get());
        Assert.assertTrue(receivedBuildSetStatusChangedEvent.get().getNewBuildStatus().isFinal());
    }

    @Test
    @InSequence(30)
    public void buildTaskCallbacksShouldBeCalled() throws DatastoreException, CoreException {
        User user = User.Builder.newBuilder().id(3).username("test-user-3").build();
        Set<BuildTask> buildTasks = initializeBuildTaskSet(configurationBuilder, user, (buildConfigSetRecord) -> {})
                .getBuildTasks();
        Set<String> tasksIds = buildTasks.stream().map((BuildTask::getId)).collect(Collectors.toSet());

        Set<String> receivedUpdatesForId = new HashSet<>();
        Consumer<BuildStatusChangedEvent> statusChangeEventConsumer = (statusChangedEvent) -> {
            receivedUpdatesForId.add(statusChangedEvent.getBuild().getId());
        };

        tasksIds.forEach((id) -> buildStatusNotifications.subscribe(new BuildCallBack(id, statusChangeEventConsumer)));

        buildTasks.forEach((bt) -> buildCoordinator.updateBuildTaskStatus(bt, BuildCoordinationStatus.DONE));

        System.out.println("Received updates: " + receivedUpdatesForId);
        tasksIds.forEach(
                (id) -> Assert.assertTrue("Did not receive update for task " + id, receivedUpdatesForId.contains(id)));
    }

    private BuildSetTask initializeBuildTaskSet(
            TestProjectConfigurationBuilder configurationBuilder,
            User user,
            Consumer<BuildConfigSetRecord> onBuildSetTaskCompleted) throws DatastoreException, CoreException {
        BuildConfigurationSet buildConfigurationSet = configurationBuilder.buildConfigurationSet(1);
        return createBuildSetTask(buildConfigurationSet, user);
    }

    private BuildSetTask initializeBuildTaskSet(
            TestProjectConfigurationBuilder configurationBuilder,
            Consumer<BuildConfigSetRecord> onBuildSetTaskCompleted) throws DatastoreException, CoreException {
        User user = User.Builder.newBuilder().id(1).username("test-user").build();
        return initializeBuildTaskSet(configurationBuilder, user, onBuildSetTaskCompleted);
    }

    public BuildSetTask createBuildSetTask(BuildConfigurationSet buildConfigurationSet, User user)
            throws CoreException {
        BuildTasksInitializer buildTasksInitializer = new BuildTasksInitializer(datastoreAdapter, 1L);
        AtomicInteger atomicInteger = new AtomicInteger(1);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.FORCE);
        BuildSetTask setTask = buildTasksInitializer.createBuildSetTask(
                buildConfigurationSet,
                user,
                buildOptions,
                Sequence::nextBase32Id,
                taskRepository.getUnfinishedTasks());
        buildCoordinator
                .updateBuildConfigSetRecordStatus(setTask.getBuildConfigSetRecord().get(), BuildStatus.BUILDING, "");
        return setTask;
    }

    /**
     * use Wait.forCondition
     *
     * @param sup
     * @param timeoutSeconds
     */
    @Deprecated
    private void waitForConditionWithTimeout(Supplier<Boolean> sup, int timeoutSeconds) throws InterruptedException {
        int secondsPassed = 0;
        while (!sup.get() && secondsPassed < timeoutSeconds) {
            Thread.sleep(1000);
            secondsPassed++;
        }
    }

    private BuildResult createBuildResult() {
        BuildDriverResult driverResult = new BuildDriverResult() {
            @Override
            public String getBuildLog() {
                return "";
            }

            @Override
            public BuildStatus getBuildStatus() {
                return BuildStatus.SUCCESS;
            }

            @Override
            public Optional<String> getOutputChecksum() {
                return Optional.of("3678bbe366b11f7216bd03ad33f583d9");
            }
        };
        return new BuildResult(
                CompletionStatus.SUCCESS,
                Optional.empty(),
                "",
                Optional.empty(),
                Optional.of(driverResult),
                Optional.of(RepositoryManagerResultMock.mockResult(false)),
                Optional.empty(),
                Optional.empty());
    }
}
