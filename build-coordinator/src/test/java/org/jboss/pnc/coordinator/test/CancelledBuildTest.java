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
package org.jboss.pnc.coordinator.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class CancelledBuildTest extends ProjectBuilder {

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
    }

    private static final Logger log = LoggerFactory.getLogger(CancelledBuildTest.class);

    @Inject
    BuildCoordinatorFactory buildCoordinatorFactory;

    @Test(timeout = 5_000)
    public void buildSingleProjectTestCase() throws Exception {
        // given
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder(datastore);

        BuildCoordinator coordinator = buildCoordinatorFactory.createBuildCoordinator(datastore).coordinator;

        List<BuildStatusChangedEvent> receivedStatuses = new ArrayList<>();

        Consumer<BuildStatusChangedEvent> onStatusUpdate = (event) -> {
            receivedStatuses.add(event);
            if (event.getNewStatus().equals(BuildStatus.BUILDING)) {
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(250); // wait a bit for build execution to start
                        coordinator.cancel(event.getBuild().getId());
                    } catch (CoreException | InterruptedException e) {
                        log.error("Unable to cancel the build.", e);
                        Assert.fail("Unable to cancel the build.");
                    }
                });
            }
        };

        // when
        BuildTask buildTask = buildProject(
                configurationBuilder.buildConfigurationToCancel(1, "c1-java"),
                coordinator,
                onStatusUpdate);

        // expect
        List<BuildRecord> buildRecords = datastore.getBuildRecords();

        Assert.assertEquals("Too many build records in datastore: " + buildRecords, 1, buildRecords.size());

        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertNotNull(buildRecord.getSubmitTime());
        Assert.assertNotNull(buildRecord.getStartTime());
        Assert.assertNotNull(buildRecord.getEndTime());
        Assert.assertEquals(BuildStatus.CANCELLED, buildRecord.getStatus());

        String buildTaskId = buildTask.getId();
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILDING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.CANCELLED, buildTaskId);
    }

    @Test(timeout = 5_000)
    public void cancelBuildingConfigSetTestCase() throws Exception {
        // given
        DatastoreMock datastoreMock = new DatastoreMock();
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder(datastoreMock);
        BuildCoordinator coordinator = buildCoordinatorFactory.createBuildCoordinator(datastoreMock).coordinator;
        BuildConfigurationSet configurationSet = configurationBuilder.buildConfigurationSetForCancel(1);

        List<BuildStatusChangedEvent> receivedStatuses = new ArrayList<>();
        Consumer<BuildStatusChangedEvent> onStatusUpdate = (event) -> {
            receivedStatuses.add(event);
            if (event.getBuild().getBuildConfigRevision().getId().equals("2")
                    && event.getNewStatus().equals(BuildStatus.BUILDING)) {
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(250); // wait a bit for build execution to start
                        // we need to get buildConfigSet id to cancel BuildGroup, it is not provided by event class
                        // directly, so we need to dit it up from buildTaskId that event provides
                        log.info("Cancelling ...");
                        coordinator.cancelSet(getBuildConfigSetId(coordinator, event.getBuild().getId()));
                    } catch (CoreException | InterruptedException e) {
                        log.error("Unable to cancel the build.", e);
                        Assert.fail("Unable to cancel the build.");
                    }
                });
            }
        };

        // when
        BuildSetTask buildSetTask = buildProjects(configurationSet, coordinator, onStatusUpdate, 2);

        // expect
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();

        Assert.assertEquals("Incorrect number of build records in datastore: " + buildRecords, 3, buildRecords.size());

        for (BuildRecord buildRecord : buildRecords) {
            Assert.assertNotNull(buildRecord.getSubmitTime());
            switch (buildRecord.getBuildConfigurationId()) {
                case 1:
                    Assert.assertEquals(BuildStatus.CANCELLED, buildRecord.getStatus());
                    continue;
                case 2:
                    Assert.assertEquals(BuildStatus.CANCELLED, buildRecord.getStatus());
                    break;
                case 3:
                    Assert.assertEquals(BuildStatus.SUCCESS, buildRecord.getStatus());
                    break;
                default:
                    Assert.fail("Invalid build configuration ID");
                    break;
            }
            Assert.assertNotNull(buildRecord.getStartTime());
            Assert.assertNotNull(buildRecord.getEndTime());
        }

        // 3 is independent, 2 is dependent on 3, 1 is dependent on 2
        for (BuildTask buildTask : buildSetTask.getBuildTasks()) {
            String buildTaskId = buildTask.getId();
            switch (buildTask.getBuildConfigurationAudited().getId()) {
                case 1:
                    // Building status is skipped (cancelled before it can start building)
                    assertStatusUpdateReceived(receivedStatuses, BuildStatus.WAITING_FOR_DEPENDENCIES, buildTaskId);
                    assertStatusUpdateReceived(receivedStatuses, BuildStatus.CANCELLED, buildTaskId);
                    break;
                case 2:
                    assertStatusUpdateReceived(receivedStatuses, BuildStatus.WAITING_FOR_DEPENDENCIES, buildTaskId);
                    assertStatusUpdateReceived(receivedStatuses, BuildStatus.ENQUEUED, buildTaskId);
                    assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILDING, buildTaskId);
                    assertStatusUpdateReceived(receivedStatuses, BuildStatus.CANCELLED, buildTaskId);
                    break;
                case 3:
                    assertStatusUpdateReceived(receivedStatuses, BuildStatus.ENQUEUED, buildTaskId);
                    assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILDING, buildTaskId);
                    assertStatusUpdateReceived(receivedStatuses, BuildStatus.SUCCESS, buildTaskId);
                    break;
                default:
                    break;
            }
        }
    }

    private Long getBuildConfigSetId(BuildCoordinator coordinator, String buildTaskId) throws RemoteRequestException {
        return coordinator.getSubmittedBuildTasks()
                .stream()
                .filter(t -> buildTaskId.equals(t.getId()))
                .findAny() // got BuildTask
                .get()
                .getBuildSetTask() // got BuildConfigSet
                .getId();
    }
}