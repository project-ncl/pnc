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
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.core.builder.coordinator.BuildCoordinator;
import org.jboss.pnc.core.builder.coordinator.BuildSetTask;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.builders.TestEntitiesFactory;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jboss.pnc.core.test.buildCoordinator.BuildCoordinatorDeployments.Options.WITH_DATASTORE;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class BuildCoordinationTest {

    private static final Logger log = LoggerFactory.getLogger(BuildCoordinationTest.class);

    @Inject
    BuildCoordinator buildCoordinator;

    @Inject
    TestProjectConfigurationBuilder testProjectConfigurationBuilder;

    @Inject
    BuildSetStatusNotifications buildSetStatusNotifications;

    @Inject
    private DatastoreMock datastoreMock;

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(WITH_DATASTORE)
                .addClass(TestProjectConfigurationBuilder.class);
    }

    @Test
    public void buildRecordSetShouldBeMarkedSuccessWhenAllBuildsAreSuccess() throws CoreException, TimeoutException, InterruptedException {
        BuildConfigurationSet buildConfigurationSet = TestEntitiesFactory.newBuildConfigurationSet();
        testProjectConfigurationBuilder.buildConfigurationWithDependencies(buildConfigurationSet);

        ObjectWrapper<BuildSetStatus> lastBuildSetStatus = registerCallback(buildConfigurationSet);

        BuildSetTask buildSetTask = buildCoordinator.build(buildConfigurationSet, TestEntitiesFactory.newUser(), true);

        Wait.forCondition(lastBuildSetStatus::isSet, 5, ChronoUnit.SECONDS);

        //check the result
        Assert.assertEquals(BuildSetStatus.DONE, lastBuildSetStatus.get());
        Assert.assertEquals(BuildStatus.SUCCESS, buildSetTask.getBuildConfigSetRecord().getStatus());
    }

    @Test
    public void buildRecordSetShouldBeMarkedFailedOnFailure() throws CoreException, TimeoutException, InterruptedException {
        BuildConfigurationSet buildConfigurationSet = TestEntitiesFactory.newBuildConfigurationSet();
        testProjectConfigurationBuilder.buildConfigurationWithDependenciesThatFail(buildConfigurationSet);

        ObjectWrapper<BuildSetStatus> lastBuildSetStatus = registerCallback(buildConfigurationSet);

        BuildSetTask buildSetTask = buildCoordinator.build(buildConfigurationSet, TestEntitiesFactory.newUser(), true);

        Wait.forCondition(lastBuildSetStatus::isSet, 5, ChronoUnit.SECONDS);

        //check the result
        Assert.assertEquals(BuildSetStatus.DONE, lastBuildSetStatus.get());
        datastoreMock.getBuildConfigSetRecordById(buildConfigurationSet.getId());

        Assert.assertEquals(BuildStatus.FAILED, buildSetTask.getBuildConfigSetRecord().getStatus());
        Collection<BuildStatus> statuses = getBuildStatuses();
        Assert.assertTrue(statuses.contains(BuildStatus.FAILED));
        Assert.assertFalse(statuses.contains(BuildStatus.SYSTEM_ERROR));
    }

    @Test
    public void buildSetTaskCallbacksShouldBeCalled() throws DatastoreException, TimeoutException, InterruptedException, CoreException {
        BuildConfigurationSet buildConfigurationSet = TestEntitiesFactory.newBuildConfigurationSet();
        testProjectConfigurationBuilder.buildConfigurationWithDependencies(buildConfigurationSet);

        Set<BuildSetStatusChangedEvent> buildSetStatusChangedEvents = new HashSet<>();
        Consumer<BuildSetStatusChangedEvent> statusChangeEventConsumer = buildSetStatusChangedEvents::add;

        BuildSetCallBack buildSetCallBack = new BuildSetCallBack(buildConfigurationSet.getId(), statusChangeEventConsumer);
        log.info("Subscribing new listener for buildSetTask.id {}.", buildSetCallBack.getBuildSetConfigurationId());
        buildSetStatusNotifications.subscribe(buildSetCallBack);

        log.info("Running builds ...");

        buildCoordinator.build(buildConfigurationSet, TestEntitiesFactory.newUser(), true);

        Wait.forCondition(() -> contains(buildSetStatusChangedEvents, BuildSetStatus.NEW), 2000, ChronoUnit.MILLIS, "Did not receive status update to NEW for task set.");
        Wait.forCondition(() -> contains(buildSetStatusChangedEvents, BuildSetStatus.DONE), 2000, ChronoUnit.MILLIS, "Did not receive status update to DONE for task set.");
    }

    private boolean contains(Set<BuildSetStatusChangedEvent> buildSetStatusChangedEvents, BuildSetStatus status) {
        return buildSetStatusChangedEvents.stream().anyMatch((buildSetStatusChangedEvent) -> buildSetStatusChangedEvent.getNewStatus().equals(status));
    }

    private Collection<BuildStatus> getBuildStatuses() {
        return datastoreMock.getBuildRecords().stream()
                .map(BuildRecord::getStatus)
                .collect(Collectors.toSet());
    }

    private ObjectWrapper<BuildSetStatus> registerCallback(BuildConfigurationSet buildConfigurationSet) {
        ObjectWrapper<BuildSetStatus> lastBuildSetStatus = new ObjectWrapper<>();

        Consumer<BuildSetStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                lastBuildSetStatus.set(statusChangedEvent.getNewStatus());
            }
        };

        BuildSetCallBack buildSetCallBack = new BuildSetCallBack(buildConfigurationSet.getId(), onStatusUpdate);

        buildSetStatusNotifications.subscribe(buildSetCallBack);
        return lastBuildSetStatus;
    }
}
