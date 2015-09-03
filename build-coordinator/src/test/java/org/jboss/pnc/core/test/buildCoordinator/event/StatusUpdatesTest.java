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
package org.jboss.pnc.core.test.buildCoordinator.event;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildSetTask;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.core.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.core.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashSet;
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
    TestProjectConfigurationBuilder configurationBuilder;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addPackages(true,
                        Configuration.class.getPackage(),
                        StatusUpdatesTest.class.getPackage(),
                        BuildDriverMock.class.getPackage(),
                        BuildCoordinator.class.getPackage(),
                        BuildDriverFactory.class.getPackage(),
                        BuildSetStatusChangedEvent.class.getPackage(),
                        Observes.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties");
        log.debug(jar.toString(true));
        return jar;
    }

    @Test
    @InSequence(10)
    public void buildSetStatusShouldUpdateWhenAllBuildStatusChangeToCompletedState() throws DatastoreException, InterruptedException {
        ObjectWrapper<BuildSetStatusChangedEvent> receivedBuildSetStatusChangedEvent = new ObjectWrapper<>();
        Consumer<BuildSetStatusChangedEvent> statusUpdateListener = (event) -> {
            receivedBuildSetStatusChangedEvent.set(event);
        };
        testCDIBuildSetStatusChangedReceiver.addBuildSetStatusChangedEventListener(statusUpdateListener);

        Set<BuildTask> buildTasks = initializeBuildTask().getBuildTasks();
        buildTasks.forEach((bt) -> bt.setStatus(BuildStatus.DONE));
        this.waitForConditionWithTimeout(() -> buildTasks.stream().allMatch(task -> task.getStatus().isCompleted()), 4);

        Assert.assertNotNull("Did not receive status update.", receivedBuildSetStatusChangedEvent.get());
        Assert.assertEquals(BuildSetStatus.DONE, receivedBuildSetStatusChangedEvent.get().getNewStatus());
    }

    @Test
    @InSequence(20)
    public void buildSetStatusShouldNotUpdateWhenAllBuildStatusChangeToNonCompletedState() throws DatastoreException {
        ObjectWrapper<BuildSetStatusChangedEvent> receivedBuildSetStatusChangedEvent = new ObjectWrapper<>();
        Consumer<BuildSetStatusChangedEvent> statusUpdateListener = (event) -> {
            receivedBuildSetStatusChangedEvent.set(event);
        };
        testCDIBuildSetStatusChangedReceiver.addBuildSetStatusChangedEventListener(statusUpdateListener);

        Set<BuildTask> buildTasks = initializeBuildTask().getBuildTasks();
        Assert.assertTrue("There should be at least " + MIN_TASKS + " tasks in the set", buildTasks.size() > MIN_TASKS);
        int i = 0;
        for (BuildTask buildTask : buildTasks) {
            i++;
            if (i < MIN_TASKS) {
                buildTask.setStatus(BuildStatus.BUILD_WAITING);
            } else {
                buildTask.setStatus(BuildStatus.DONE);
            }
        }
        Assert.assertEquals(BuildSetStatus.NEW, receivedBuildSetStatusChangedEvent.get().getNewStatus());
    }

    @Test
    @InSequence(30)
    public void BuildTaskCallbacksShouldBeCalled() throws DatastoreException {
        Set<BuildTask> buildTasks = initializeBuildTask().getBuildTasks();
        Set<Integer> tasksIds = buildTasks.stream().map((buildTask -> buildTask.getId())).collect(Collectors.toSet());

        Set<Integer> receivedUpdatesForId = new HashSet<>();
        Consumer<BuildStatusChangedEvent> statusChangeEventConsumer = (statusChangedEvent) -> {
            receivedUpdatesForId.add(statusChangedEvent.getBuildTaskId());
        };

        tasksIds.forEach((id) -> {
            buildStatusNotifications.subscribe(new BuildCallBack(id, statusChangeEventConsumer));
        });

        buildTasks.forEach((bt) -> bt.setStatus(BuildStatus.DONE));

        tasksIds.forEach((id) -> {
            Assert.assertTrue("Did not receive update for task " + id, receivedUpdatesForId.contains(id));
        });
    }

    @Test
    @InSequence(40)
    public void BuildSetTaskCallbacksShouldBeCalled() throws DatastoreException {
        BuildSetTask buildSetTask = initializeBuildTask();
        Set<BuildTask> buildTasks = buildSetTask.getBuildTasks();

        ObjectWrapper<BuildSetStatusChangedEvent> buildSetStatusChangedEvent = new ObjectWrapper<>();
        Consumer<BuildSetStatusChangedEvent> statusChangeEventConsumer = (statusChangedEvent) -> {
            buildSetStatusChangedEvent.set(statusChangedEvent);
        };

        buildSetStatusNotifications.subscribe(new BuildSetCallBack(buildSetTask.getId(), statusChangeEventConsumer));

        buildTasks.forEach((bt) -> bt.setStatus(BuildStatus.DONE));

        Assert.assertNotNull("Did not receive status update.", buildSetStatusChangedEvent.get());
        Assert.assertEquals("Did not receive status update to DONE for task set.", BuildSetStatus.DONE, buildSetStatusChangedEvent.get().getNewStatus());
    }

    AtomicInteger buildTaskSetIdSupplier = new AtomicInteger(0);
    AtomicInteger buildTaskIdSupplier = new AtomicInteger(0);

    private BuildSetTask initializeBuildTask() throws DatastoreException {
        BuildConfigurationSet buildConfigurationSet = configurationBuilder.buildConfigurationSet(1);
        User user = User.Builder.newBuilder().id(1).username("test-user").build();
        BuildSetTask buildSetTask = null;
        try {
            buildSetTask = buildCoordinator.createBuildSetTask(buildConfigurationSet, user);
        } catch (CoreException e) {
            Assert.fail(e.getMessage());
        }

        return buildSetTask;
    }

    /**
     * Wait until the give boolean condition becomes true, or the given number of seconds passes
     * 
     * @param sup
     * @param timeoutSeconds
     */
    private void waitForConditionWithTimeout(Supplier<Boolean> sup, int timeoutSeconds) throws InterruptedException {
        int secondsPassed = 0;
        while (!sup.get() && secondsPassed < timeoutSeconds) {
            Thread.sleep(1000);
            secondsPassed++;
        }
    }
}
