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

import lombok.RequiredArgsConstructor;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
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
import org.jboss.pnc.mock.repository.TargetRepositoryRepositoryMock;
import org.jboss.pnc.mock.repository.UserRepositoryMock;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.test.util.Wait;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
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

    private Logger logger = LoggerFactory.getLogger(AbstractDependentBuildTest.class);

    protected static final AtomicInteger configIdSequence = new AtomicInteger(0);
    protected static final AtomicInteger configAuditedIdSequence = new AtomicInteger(0);
    protected static final AtomicInteger buildRecordIdSequence = new AtomicInteger(0);

    protected List<BuildTask> builtTasks;

    private BuildConfigurationAuditedRepositoryMock buildConfigurationAuditedRepository;

    protected BuildConfigurationRepositoryMock buildConfigurationRepository;

    private BuildQueue buildQueue;

    protected BuildCoordinator coordinator;
    protected BuildRecordRepositoryMock buildRecordRepository;
    protected BuildSchedulerFactory buildSchedulerFactory;

    @Before
    @SuppressWarnings("unchecked")
    public void initialize() throws DatastoreException, ConfigurationParseException {
        MockitoAnnotations.initMocks(this);

        builtTasks = new ArrayList<>();

        Configuration config = mock(Configuration.class);
        SystemConfig systemConfig = mock(SystemConfig.class);
        when(systemConfig.getCoordinatorThreadPoolSize()).thenReturn(1);
        when(systemConfig.getCoordinatorMaxConcurrentBuilds()).thenReturn(1);
        when(systemConfig.getTemporaryBuildsLifeSpan()).thenReturn(1);
        when(systemConfig.getTemporalBuildExpireDate()).thenReturn(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        when(config.getModuleConfig(any())).thenReturn(systemConfig);

        buildQueue = new BuildQueue(config);

        if (buildConfigurationRepository == null) {
            buildConfigurationRepository = new BuildConfigurationRepositoryMock();
        }

        buildRecordRepository = new BuildRecordRepositoryMock();
        buildConfigurationAuditedRepository = new BuildConfigurationAuditedRepositoryMock();
        TargetRepositoryRepository targetRepositoryRepository = new TargetRepositoryRepositoryMock();

        DefaultDatastore datastore = new DefaultDatastore(
                new ArtifactRepositoryMock(),
                buildRecordRepository,
                buildConfigurationRepository,
                buildConfigurationAuditedRepository,
                new BuildConfigSetRecordRepositoryMock(),
                new UserRepositoryMock(),
                new SequenceHandlerRepositoryMock(),
                targetRepositoryRepository
        );
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        if (buildSchedulerFactory == null) {
            buildSchedulerFactory = new MockBuildSchedulerFactory();
        }

        coordinator = new DefaultBuildCoordinator(datastoreAdapter, mock(Event.class), mock(Event.class),
                buildSchedulerFactory,
                buildQueue,
                systemConfig);
        buildQueue.initSemaphore();
        coordinator.start();
    }


    protected void markAsAlreadyBuilt(BuildConfiguration... configs) {
        Stream.of(configs).forEach(
                c -> buildRecordRepository.save(buildRecord(c))
        );
    }

    protected BuildRecord buildRecord(BuildConfiguration config) {
        BuildConfigurationAudited configurationAudited =
                buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(config.getId()).iterator().next();
        return BuildRecord.Builder.newBuilder()
                .id(buildRecordIdSequence.getAndIncrement())
                .status(BuildStatus.SUCCESS)
                .buildConfigurationAudited(configurationAudited)
                .temporaryBuild(false)
                .build();
    }

    protected BuildConfiguration config(String name, BuildConfiguration... dependencies) {
        BuildConfiguration config = buildConfig(name, dependencies);
        saveConfig(config);
        return config;
    }

    protected BuildConfiguration buildConfig(String name, BuildConfiguration... dependencies) {
        int id = configIdSequence.getAndIncrement();
        Project project = Project.Builder.newBuilder()
                .id(1)
                .name("Mock project")
                .build();

        BuildConfiguration config = BuildConfiguration.Builder.newBuilder()
                        .id(id)
                        .name(name)
                        .project(project)
                        .build();
        Stream.of(dependencies).forEach(config::addDependency);
        return config;
    }

    protected void saveConfig(BuildConfiguration config) {
        buildConfigurationRepository.save(config);
        buildConfigurationAuditedRepository.save(auditedConfig(config));
    }

    /**
     * Create a new revision
     */
    protected BuildConfiguration updateConfiguration(BuildConfiguration buildConfiguration) {
        int id = configIdSequence.getAndIncrement();

        RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
                .internalUrl("http://path.to/repo.git")
                .build();
        buildConfiguration.setRepositoryConfiguration(repositoryConfiguration);

        saveConfig(buildConfiguration);

        return buildConfiguration;
    }

    protected void modifyConfigurations(BuildConfiguration... configurations) {
        Stream.of(configurations).forEach(
                c -> buildConfigurationAuditedRepository.save(auditedConfig(c))
        );
    }

    private BuildConfigurationAudited auditedConfig(BuildConfiguration config) {
        int rev = configAuditedIdSequence.incrementAndGet();
        return BuildConfigurationAudited.fromBuildConfiguration(config, rev);
    }

    protected void build(BuildConfigurationSet configSet, boolean rebuildAll) throws CoreException {
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setForceRebuild(rebuildAll);

        coordinator.build(configSet, null, buildOptions);
        coordinator.start();
    }

    protected void build(BuildConfiguration config) {
        BuildOptions buildOptions = new BuildOptions();
        build(config, buildOptions);
    }

    protected void build(BuildConfiguration config, BuildOptions buildOptions) {
        try {
            coordinator.build(config, null, buildOptions);
        } catch (BuildConflictException | CoreException e) {
            throw new RuntimeException("Failed to run a build of: " + config, e);
        }
    }

    protected BuildTask getBuildTaskById(Integer taskId) {
        Optional<BuildTask> buildTask = builtTasks.stream()
                .filter(bt -> bt.getId() == taskId)
                .findAny();
        if (buildTask.isPresent()) {
            return buildTask.get();
        } else {
            throw new RuntimeException("Task with id [" + taskId + "] was not found.");
        }
    }

    protected Optional<BuildTask> getScheduledBuildTaskByConfigurationId(Integer configurationId) {
        return builtTasks.stream()
                .filter(bt -> bt.getBuildConfigurationAudited().getBuildConfiguration().getId().equals(configurationId))
                .findAny();
    }

    protected List<BuildConfiguration> getBuiltConfigs() {
        return builtTasks.stream()
                .map(BuildTask::getBuildConfigurationAudited)
                .map(BuildConfigurationAudited::getBuildConfiguration)
                .collect(Collectors.toList());
    }

    protected BuildConfigurationSet configSet(BuildConfiguration... configs) {
        BuildConfigurationSet set = new BuildConfigurationSet();
        Stream.of(configs).forEach(set::addBuildConfiguration);
        return set;
    }

    protected void waitForEmptyBuildQueue() throws InterruptedException, TimeoutException {
        Supplier<String> errorMessage = () -> {
            return "Tired waiting for BuildQueue to be empty."
                + "There are still tasks in the queue: " + buildQueue.getUnfinishedTasks();
        };
        Wait.forCondition(() -> buildQueue.isEmpty(), 10, ChronoUnit.SECONDS, errorMessage);
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

    protected static BuildResult buildResult() {
        return new BuildResult(
                CompletionStatus.SUCCESS,
                Optional.empty(),
                "",
                Optional.of(mock(BuildExecutionConfiguration.class)),
                Optional.of(buildDriverResult()),
                Optional.of(repoManagerResult()),
                Optional.empty(),
                Optional.empty());
    }

    private static BuildDriverResult buildDriverResult() {
        BuildDriverResult mock = mock(BuildDriverResult.class);
        when(mock.getBuildStatus()).thenReturn(BuildStatus.SUCCESS);
        return mock;
    }

    private static RepositoryManagerResult repoManagerResult() {
        RepositoryManagerResult mock = mock(RepositoryManagerResult.class);
        when(mock.getCompletionStatus()).thenReturn(CompletionStatus.SUCCESS);
        return mock;
    }

    protected DependencyHandler makeResult(BuildConfiguration config) {
        return new DependencyHandler(config);
    }

    @RequiredArgsConstructor
    protected class DependencyHandler {
        final BuildConfiguration config;
        BuildRecord record;

        public void dependOn(BuildConfiguration... dependencies) {
            record = buildRecordRepository.getLatestSuccessfulBuildRecord(config.getId());

            Set<Artifact> artifacts = Stream.of(dependencies)
                    .map(this::mockArtifactBuiltWith)
                    .collect(Collectors.toSet());
            record.setDependencies(artifacts);
        }

        private Artifact mockArtifactBuiltWith(BuildConfiguration config) {
            BuildRecord record = buildRecordRepository.getLatestSuccessfulBuildRecord(config.getId());

            Set<BuildRecord> records = new HashSet<>();
            records.add(record);
            return  Artifact.Builder.newBuilder()
                    .buildRecords(records)
                    .build();
        }
    }
}
