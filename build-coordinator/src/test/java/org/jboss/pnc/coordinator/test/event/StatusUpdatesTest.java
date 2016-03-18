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
package org.jboss.pnc.coordinator.test.event;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.coordinator.builder.BuildCoordinator;
import org.jboss.pnc.coordinator.builder.BuildSetTask;
import org.jboss.pnc.coordinator.builder.BuildTask;
import org.jboss.pnc.coordinator.builder.BuildTasksInitializer;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.coordinator.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.coordinator.test.BuildCoordinatorDeployments;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
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
    BuildCoordinator buildCoordinator;

    @Inject
    TestCDIBuildSetStatusChangedReceiver testCDIBuildSetStatusChangedReceiver;

    @Inject
    BuildStatusNotifications buildStatusNotifications;

    @Inject
    BuildSetStatusNotifications buildSetStatusNotifications;

    @Inject
    Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier;

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Inject
    DatastoreAdapter datastoreAdapter;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(BuildCoordinatorDeployments.Options.WITH_DATASTORE);
    }

    @Test
    @InSequence(10)
    public void buildSetStatusShouldUpdateWhenAllBuildStatusChangeToCompletedState() throws DatastoreException, InterruptedException, CoreException {
        ObjectWrapper<BuildSetStatusChangedEvent> receivedBuildSetStatusChangedEvent = new ObjectWrapper<>();
        Consumer<BuildSetStatusChangedEvent> statusUpdateListener = (event) -> {
            receivedBuildSetStatusChangedEvent.set(event);
        };
        testCDIBuildSetStatusChangedReceiver.addBuildSetStatusChangedEventListener(statusUpdateListener);

        User user = User.Builder.newBuilder().id(1).username("test-user-1").build();
        Set<BuildTask> buildTasks = initializeBuildTaskSet(configurationBuilder, user, (buildConfigSetRecord) -> {}).getBuildTasks();
        buildTasks.forEach((bt) -> bt.setStatus(BuildCoordinationStatus.DONE));
        this.waitForConditionWithTimeout(() -> buildTasks.stream().allMatch(task -> task.getStatus().isCompleted()), 4);

        Assert.assertNotNull("Did not receive build set status update.", receivedBuildSetStatusChangedEvent.get());
        Assert.assertEquals(BuildSetStatus.DONE, receivedBuildSetStatusChangedEvent.get().getNewStatus());
    }

    @Test
    @InSequence(20)
    public void buildSetStatusShouldNotUpdateWhenAllBuildStatusChangeToNonCompletedState() throws DatastoreException, CoreException {
        ObjectWrapper<BuildSetStatusChangedEvent> receivedBuildSetStatusChangedEvent = new ObjectWrapper<>();
        Consumer<BuildSetStatusChangedEvent> statusUpdateListener = (event) -> {
            receivedBuildSetStatusChangedEvent.set(event);
        };
        testCDIBuildSetStatusChangedReceiver.addBuildSetStatusChangedEventListener(statusUpdateListener);

        User user = User.Builder.newBuilder().id(2).username("test-user-2").build();
        Set<BuildTask> buildTasks = initializeBuildTaskSet(configurationBuilder, user, (buildConfigSetRecord) -> {}).getBuildTasks();
        Assert.assertTrue("There should be at least " + MIN_TASKS + " tasks in the set", buildTasks.size() > MIN_TASKS);
        int i = 0;
        for (BuildTask buildTask : buildTasks) {
            i++;
            if (i < MIN_TASKS) {
                buildTask.setStatus(BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES);
            } else {
                buildTask.setStatus(BuildCoordinationStatus.DONE);
            }
        }
        Assert.assertEquals(BuildSetStatus.NEW, receivedBuildSetStatusChangedEvent.get().getNewStatus());
    }

    @Test
    @InSequence(30)
    public void BuildTaskCallbacksShouldBeCalled() throws DatastoreException, CoreException {
        User user = User.Builder.newBuilder().id(3).username("test-user-3").build();
        Set<BuildTask> buildTasks = initializeBuildTaskSet(configurationBuilder, user, (buildConfigSetRecord) -> {}).getBuildTasks();
        Set<Integer> tasksIds = buildTasks.stream().map((buildTask -> buildTask.getId())).collect(Collectors.toSet());

        Set<Integer> receivedUpdatesForId = new HashSet<>();
        Consumer<BuildCoordinationStatusChangedEvent> statusChangeEventConsumer = (statusChangedEvent) -> {
            receivedUpdatesForId.add(statusChangedEvent.getBuildTaskId());
        };

        tasksIds.forEach((id) -> {
            buildStatusNotifications.subscribe(new BuildCallBack(id, statusChangeEventConsumer));
        });

        buildTasks.forEach((bt) -> bt.setStatus(BuildCoordinationStatus.DONE));

        tasksIds.forEach((id) -> {
            Assert.assertTrue("Did not receive update for task " + id, receivedUpdatesForId.contains(id));
        });
    }

    private BuildSetTask initializeBuildTaskSet(TestProjectConfigurationBuilder configurationBuilder, User user, Consumer<BuildConfigSetRecord> onBuildSetTaskCompleted) throws DatastoreException, CoreException {
        BuildConfigurationSet buildConfigurationSet = configurationBuilder.buildConfigurationSet(1);
        return createBuildSetTask(buildConfigurationSet, user);
    }

    private BuildSetTask initializeBuildTaskSet(TestProjectConfigurationBuilder configurationBuilder, Consumer<BuildConfigSetRecord> onBuildSetTaskCompleted) throws DatastoreException, CoreException {
        User user = User.Builder.newBuilder().id(1).username("test-user").build();
        return initializeBuildTaskSet(configurationBuilder, user, onBuildSetTaskCompleted);
    }

    public BuildSetTask createBuildSetTask(BuildConfigurationSet buildConfigurationSet, User user) throws CoreException {
        BuildTasksInitializer buildTasksInitializer = new BuildTasksInitializer(datastoreAdapter, Optional.of(buildSetStatusChangedEventNotifier));
        AtomicInteger atomicInteger = new AtomicInteger(1);

        return buildTasksInitializer.createBuildSetTask(
                buildConfigurationSet,
                user,
                true,
                buildStatusChangedEventNotifier,
                () -> atomicInteger.getAndIncrement(),
                (buildTask) -> {},
                (buildConfigSetRecord) -> {},
                (buildTask) -> {});
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
}
