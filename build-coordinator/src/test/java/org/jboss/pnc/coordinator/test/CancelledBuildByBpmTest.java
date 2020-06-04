/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.mock.BpmMock;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.bpm.model.mapper.BuildResultMapper;
import org.jboss.pnc.bpm.model.mapper.RepositoryManagerResultMapper;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.coordinator.builder.BuildScheduler;
import org.jboss.pnc.coordinator.builder.DefaultBuildCoordinator;
import org.jboss.pnc.coordinator.builder.bpm.BpmBuildScheduler;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mapper.AbstractArtifactMapperImpl;
import org.jboss.pnc.mapper.EnvironmentMapperImpl;
import org.jboss.pnc.mapper.SCMRepositoryMapperImpl;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.EnvironmentMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mapper.api.ProjectMapper;
import org.jboss.pnc.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.MockUser;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.TypeLiteral;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.jboss.pnc.bpm.BpmEventType.BUILD_COMPLETE;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(MockitoJUnitRunner.class)
public class CancelledBuildByBpmTest {

    private static final Logger log = LoggerFactory.getLogger(CancelledBuildByBpmTest.class);

    @Spy
    private GroupBuildMapper groupBuildMapper;

    @Spy
    private BuildMapper buildMapper;

    @Mock
    private Configuration configuration;

    @Spy
    private TargetRepositoryMapper targetRepositoryMapper;

    @Spy
    private AbstractArtifactMapperImpl artifactMapper;

    @Spy
    private RepositoryManagerResultMapper repositoryManagerResultMapper;

    @Spy
    private SCMRepositoryMapper scmRepositoryMapper = new SCMRepositoryMapperImpl();

    @Spy
    private EnvironmentMapper environmentMapper = new EnvironmentMapperImpl();

    @Spy
    @InjectMocks
    private BuildResultMapper buildResultMapper = new BuildResultMapper();

    @Spy
    private ProjectMapper projectMapper;

    @Test(timeout = 5_000)
    public void buildSingleProjectTestCase() throws Exception {

        // given
        DatastoreMock datastoreMock = new DatastoreMock();
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder(datastoreMock);

        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastoreMock);

        SystemConfig systemConfig = createConfiguration();
        BuildQueue queue = new BuildQueue(systemConfig);

        BlockingQueue<BuildStatusChangedEvent> receivedStatuses = new ArrayBlockingQueue<>(5);
        Consumer<BuildStatusChangedEvent> onStatusUpdate = (event) -> {
            receivedStatuses.add(event);
        };
        EventListener buildStatusChangedEventNotifier = new EventListener(onStatusUpdate);

        BlockingQueue<BpmTask> task = new ArrayBlockingQueue<>(5);
        Consumer<BpmTask> onBpmTaskCreated = (t) -> {
            task.add(t);
        };
        BuildSchedulerFactory buildSchedulerFactory = new BuildSchedulerFactory(onBpmTaskCreated);

        BuildCoordinator coordinator = new DefaultBuildCoordinator(
                datastoreAdapter,
                buildStatusChangedEventNotifier,
                null,
                buildSchedulerFactory,
                queue,
                systemConfig,
                groupBuildMapper,
                buildMapper);
        coordinator.start();
        queue.initSemaphore();

        coordinator.build(
                configurationBuilder.buildConfigurationToCancel(1, "c1-bpm"),
                MockUser.newTestUser(1),
                new BuildOptions());

        waitForStatus(receivedStatuses, BuildStatus.BUILDING);

        BpmTask bpmTask = task.poll(1, TimeUnit.SECONDS);
        BuildResultRest result = new BuildResultRest();
        result.setCompletionStatus(CompletionStatus.CANCELLED);

        // when
        bpmTask.notify(BUILD_COMPLETE, result);

        waitForStatus(receivedStatuses, BuildStatus.CANCELLED);

        // expect
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();

        Assert.assertEquals("Too many build records in datastore: " + buildRecords, 1, buildRecords.size());

        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertNotNull(buildRecord.getSubmitTime());
        Assert.assertNotNull(buildRecord.getStartTime());
        Assert.assertNotNull(buildRecord.getEndTime());
        Assert.assertEquals(BuildStatus.CANCELLED, buildRecord.getStatus());
    }

    private void waitForStatus(BlockingQueue<BuildStatusChangedEvent> receivedStatuses, BuildStatus status)
            throws InterruptedException, TimeoutException {
        BuildStatusChangedEvent statusChangedEvent = receivedStatuses.poll(1, TimeUnit.SECONDS);
        if (statusChangedEvent == null) {
            throw new TimeoutException("Did not received status update: " + status);
        }
        if (statusChangedEvent.getNewStatus().equals(status)) {
            return;
        }
        waitForStatus(receivedStatuses, status);
    }

    public class BuildSchedulerFactory extends org.jboss.pnc.coordinator.builder.BuildSchedulerFactory {

        BpmBuildScheduler buildScheduler;

        public BuildSchedulerFactory(Consumer<BpmTask> onTaskStarted)
                throws CoreException, ConfigurationParseException, IOException {
            BpmMock manager = new BpmMock();
            manager.setOnTaskStarted(onTaskStarted);
            buildScheduler = new BpmBuildScheduler(manager, buildResultMapper);
        }

        @Override
        public BuildScheduler getBuildScheduler() {
            return buildScheduler;
        }
    }

    private SystemConfig createConfiguration() {
        return new SystemConfig(
                "ProperDriver",
                "local-build-scheduler",
                "NO_AUTH",
                "10",
                "10",
                "10",
                "${product_short_name}-${product_version}-pnc",
                "10",
                null,
                null,
                "14",
                "",
                "10");
    }

    private static class EventListener implements Event<BuildStatusChangedEvent> {

        Consumer<BuildStatusChangedEvent> onEvent;

        public EventListener(Consumer<BuildStatusChangedEvent> onEvent) {
            this.onEvent = onEvent;
        }

        @Override
        public void fire(BuildStatusChangedEvent event) {
            onEvent.accept(event);
        }

        @Override
        public <U extends BuildStatusChangedEvent> CompletionStage<U> fireAsync(U event) {
            return null;
        }

        @Override
        public <U extends BuildStatusChangedEvent> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
            return null;
        }

        @Override
        public Event<BuildStatusChangedEvent> select(Annotation... qualifiers) {
            return null;
        }

        @Override
        public <U extends BuildStatusChangedEvent> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
            return null;
        }

        @Override
        public <U extends BuildStatusChangedEvent> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            return null;
        }
    }

}
