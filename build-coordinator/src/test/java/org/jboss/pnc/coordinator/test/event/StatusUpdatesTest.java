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

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.coordinator.builder.BuildTasksInitializer;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.coordinator.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.coordinator.test.BuildCoordinatorDeployments;
import org.jboss.pnc.mavenrepositorymanager.MavenRepositoryManagerResult;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
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
import java.util.Collections;
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
        buildTasks.forEach((bt) -> {
            buildCoordinator.updateBuildTaskStatus(bt, BuildCoordinationStatus.DONE);
            buildCoordinator.completeBuild(bt, createBuildResult());
        });
        this.waitForConditionWithTimeout(() -> buildTasks.stream().allMatch(task -> task.getStatus().isCompleted()), 4);

        Assert.assertNotNull("Did not receive build set status update.", receivedBuildSetStatusChangedEvent.get());
        Assert.assertEquals(BuildSetStatus.DONE, receivedBuildSetStatusChangedEvent.get().getNewStatus());
    }

    @Test
    @InSequence(30)
    public void BuildTaskCallbacksShouldBeCalled() throws DatastoreException, CoreException {
        User user = User.Builder.newBuilder().id(3).username("test-user-3").build();
        Set<BuildTask> buildTasks = initializeBuildTaskSet(configurationBuilder, user, (buildConfigSetRecord) -> {}).getBuildTasks();
        Set<Integer> tasksIds = buildTasks.stream().map((BuildTask::getId)).collect(Collectors.toSet());

        Set<Integer> receivedUpdatesForId = new HashSet<>();
        Consumer<BuildCoordinationStatusChangedEvent> statusChangeEventConsumer = (statusChangedEvent) -> {
            receivedUpdatesForId.add(statusChangedEvent.getBuildTaskId());
        };

        tasksIds.forEach((id) -> {
            buildStatusNotifications.subscribe(new BuildCallBack(id, statusChangeEventConsumer));
        });

        buildTasks.forEach((bt) -> {
            buildCoordinator.updateBuildTaskStatus(bt, BuildCoordinationStatus.DONE);
        });

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
        BuildTasksInitializer buildTasksInitializer = new BuildTasksInitializer(datastoreAdapter);
        AtomicInteger atomicInteger = new AtomicInteger(1);

        return buildTasksInitializer.createBuildSetTask(
                buildConfigurationSet,
                user,
                true,
                false,
                () -> atomicInteger.getAndIncrement(),
                buildConfigurationSet.getBuildConfigurations());
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
        MavenRepositoryManagerResult repoManagerResult = new MavenRepositoryManagerResult(Collections.emptyList(), Collections.emptyList(), RandomStringUtils.randomNumeric(4));
        BuildDriverResult driverResult = new BuildDriverResult() {
            @Override
            public String getBuildLog() {
                return "";
            }

            @Override
            public BuildStatus getBuildStatus() {
                return BuildStatus.SUCCESS;
            }
        };
        return new BuildResult(
                CompletionStatus.SUCCESS,
                Optional.empty(),
                "",
                Optional.empty(),
                Optional.of(driverResult),
                Optional.of(repoManagerResult),
                Optional.empty(),
                Optional.empty());
    }
}
