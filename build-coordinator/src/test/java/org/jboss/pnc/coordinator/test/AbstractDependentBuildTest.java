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
package org.jboss.pnc.coordinator.test;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.coordinator.builder.BuildScheduler;
import org.jboss.pnc.coordinator.builder.BuildSchedulerFactory;
import org.jboss.pnc.coordinator.builder.DefaultBuildCoordinator;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.datastore.DefaultDatastore;
import org.jboss.pnc.mock.repository.ArtifactRepositoryMock;
import org.jboss.pnc.mock.repository.BuildConfigSetRecordRepositoryMock;
import org.jboss.pnc.mock.repository.BuildConfigurationAuditedRepositoryMock;
import org.jboss.pnc.mock.repository.BuildConfigurationRepositoryMock;
import org.jboss.pnc.mock.repository.BuildRecordRepositoryMock;
import org.jboss.pnc.mock.repository.SequenceHandlerRepositoryMock;
import org.jboss.pnc.mock.repository.UserRepositoryMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerStatus;
import org.jboss.pnc.test.util.Wait;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import javax.enterprise.event.Event;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 9/16/16
 * Time: 1:19 PM
 */
@SuppressWarnings("deprecation")
public abstract class AbstractDependentBuildTest {
    private static final AtomicInteger configIdSequence = new AtomicInteger(0);
    private static final AtomicInteger configAuditedIdSequence = new AtomicInteger(0);
    private static final AtomicInteger buildRecordIdSequence = new AtomicInteger(0);

    private List<BuildTask> builtTasks;

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private BuildConfigurationRepositoryMock buildConfigurationRepository;

    private BuildQueue buildQueue;

    protected BuildCoordinator coordinator;
    protected BuildRecordRepositoryMock buildRecordRepository;

    @Before
    @SuppressWarnings("unchecked")
    public void initialize() throws DatastoreException, ConfigurationParseException {
        MockitoAnnotations.initMocks(this);

        builtTasks = new ArrayList<>();

        Configuration config = mock(Configuration.class);
        SystemConfig systemConfig = mock(SystemConfig.class);
        when(systemConfig.getCoordinatorThreadPoolSize()).thenReturn(1);
        when(systemConfig.getCoordinatorMaxConcurrentBuilds()).thenReturn(1);
        when(config.getModuleConfig(any())).thenReturn(systemConfig);

        buildQueue = new BuildQueue(config);

        buildConfigurationRepository = new BuildConfigurationRepositoryMock();
        buildRecordRepository = new BuildRecordRepositoryMock();
        buildConfigurationAuditedRepository = new BuildConfigurationAuditedRepositoryMock();
        DefaultDatastore datastore = new DefaultDatastore(
                new ArtifactRepositoryMock(),
                buildRecordRepository,
                buildConfigurationRepository,
                buildConfigurationAuditedRepository,
                new BuildConfigSetRecordRepositoryMock(),
                new UserRepositoryMock(),
                new SequenceHandlerRepositoryMock()
        );
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        coordinator = new DefaultBuildCoordinator(datastoreAdapter, mock(Event.class), mock(Event.class),
                new MockBuildSchedulerFactory(),
                buildQueue,
                config);
        buildQueue.initSemaphore();
        coordinator.start();
    }


    protected void markAsAlreadyBuilt(BuildConfiguration... configs) {
        Stream.of(configs).forEach(
                c -> c.addBuildRecord(buildRecord(c))
        );
    }

    protected static BuildRecord buildRecord(BuildConfiguration config) {
        return BuildRecord.Builder.newBuilder()
                .id(buildRecordIdSequence.getAndIncrement())
                .status(BuildStatus.SUCCESS)
                .latestBuildConfiguration(config)
                .build();
    }

    protected BuildConfiguration config(String name, BuildConfiguration... dependencies) {
        int id = configIdSequence.getAndIncrement();

        BuildConfiguration config = new BuildConfiguration();
        config.setName(name);
        config.setId(id);
        Stream.of(dependencies).forEach(config::addDependency);

        buildConfigurationRepository.save(config);
        BuildConfigurationAudited auditedConfig = new BuildConfigurationAudited();
        auditedConfig.setIdRev(new IdRev(config.getId(), RandomUtils.randInt(1000, 1000000)));
        Project project = new Project();
        auditedConfig.setProject(project);
        buildConfigurationAuditedRepository.save(auditedConfig);

        return config;
    }

    protected void build(BuildConfigurationSet configSet, boolean rebuildAll) throws CoreException {
        coordinator.build(configSet, null, false, rebuildAll);
        coordinator.start();
    }

    protected List<BuildConfiguration> getBuiltConfigs() {
        return builtTasks.stream()
                .map(BuildTask::getBuildConfiguration)
                .collect(Collectors.toList());
    }

    protected BuildConfigurationSet configSet(BuildConfiguration... configs) {
        BuildConfigurationSet set = new BuildConfigurationSet();
        Stream.of(configs).forEach(set::addBuildConfiguration);
        return set;
    }

    protected void waitForEmptyBuildQueue() throws InterruptedException, TimeoutException {
        Wait.forCondition(() -> buildQueue.isEmpty(), 10, ChronoUnit.SECONDS, "Tired waiting for BuildQueue to be empty.");
    }

    private class MockBuildSchedulerFactory extends BuildSchedulerFactory {
        @Override
        public BuildScheduler getBuildScheduler() {
            return new MockBuildScheduler();
        }
    }

    private class MockBuildScheduler implements BuildScheduler {

        @Override
        public void startBuilding(BuildTask buildTask, Consumer<BuildResult> onComplete) throws CoreException, ExecutorException {
            builtTasks.add(buildTask);
            BuildResult result = buildResult();
            onComplete.accept(result);
        }

        @Override
        public String getId() {
            return "MockBuildScheduler";
        }

        @Override
        public boolean cancel(BuildTask buildTask) throws CoreException {
            return false;
        }
    }

    private static BuildResult buildResult() {
        return new BuildResult(
                Optional.of(mock(BuildExecutionConfiguration.class)),
                Optional.of(buildDriverResult()),
                Optional.of(repoManagerResult()),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(RandomStringUtils.randomAlphabetic(3)),
                Optional.of(RandomStringUtils.randomAlphabetic(3))
        );
    }

    private static BuildDriverResult buildDriverResult() {
        BuildDriverResult mock = mock(BuildDriverResult.class);
        when(mock.getBuildStatus()).thenReturn(BuildStatus.SUCCESS);
        return mock;
    }

    private static RepositoryManagerResult repoManagerResult() {
        RepositoryManagerResult mock = mock(RepositoryManagerResult.class);
        when(mock.getStatus()).thenReturn(RepositoryManagerStatus.SUCCESS);
        return mock;
    }
}
