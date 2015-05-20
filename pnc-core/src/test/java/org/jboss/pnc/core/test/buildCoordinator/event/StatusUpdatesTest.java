package org.jboss.pnc.core.test.buildCoordinator.event;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildSetTask;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.builder.BuildTasksTree;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.core.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.core.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.BuildExecutionType;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.BuildStatus;
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

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addPackages(true,
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
    public void buildSetStatusShouldUpdateWhenAllBuildStatusChangeToCompletedState() {
        ObjectWrapper<BuildSetStatusChangedEvent> receivedBuildSetStatusChangedEvent = new ObjectWrapper<>();
        Consumer<BuildSetStatusChangedEvent> statusUpdateListener = (event) -> {
            receivedBuildSetStatusChangedEvent.set(event);
        };
        testCDIBuildSetStatusChangedReceiver.addBuildSetStatusChangedEventListener(statusUpdateListener);

        Set<BuildTask> buildTasks = initializeBuildTask().getBuildTasks();
        buildTasks.forEach((bt) -> bt.setStatus(BuildStatus.DONE));
        Assert.assertNotNull("Did not receive status update.", receivedBuildSetStatusChangedEvent.get());
        Assert.assertEquals(BuildSetStatus.DONE, receivedBuildSetStatusChangedEvent.get().getNewStatus());
    }

    @Test
    @InSequence(20)
    public void buildSetStatusShouldNotUpdateWhenAllBuildStatusChangeToNonCompletedState() {
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
        Assert.assertNull("Received unexpected status update.", receivedBuildSetStatusChangedEvent.get());
    }

    @Test
    @InSequence(30)
    public void BuildTaskCallbacksShouldBeCalled() {
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
    public void BuildSetTaskCallbacksShouldBeCalled() {
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

    private BuildSetTask initializeBuildTask() {
        BuildConfigurationSet buildConfigurationSet = new TestProjectConfigurationBuilder().buildConfigurationSet(1);

        BuildSetTask buildSetTask = new BuildSetTask(
                buildCoordinator,
                buildConfigurationSet,
                BuildExecutionType.COMPOSED_BUILD,
                () -> buildTaskSetIdSupplier.incrementAndGet());
        BuildTasksTree.newInstance(buildCoordinator, buildSetTask, null, () -> buildTaskIdSupplier.incrementAndGet());

        return buildSetTask;
    }
}
