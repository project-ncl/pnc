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

import lombok.RequiredArgsConstructor;
import org.jboss.pnc.bifrost.upload.BifrostLogUploader;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.coordinator.builder.BuildSchedulerFactory;
import org.jboss.pnc.coordinator.builder.DefaultBuildCoordinator;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.datastore.DefaultDatastore;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mock.model.BuildEnvironmentMock;
import org.jboss.pnc.mock.model.RepositoryConfigurationMock;
import org.jboss.pnc.mock.repository.ArtifactRepositoryMock;
import org.jboss.pnc.mock.repository.BuildConfigSetRecordRepositoryMock;
import org.jboss.pnc.mock.repository.BuildConfigurationAuditedRepositoryMock;
import org.jboss.pnc.mock.repository.BuildConfigurationRepositoryMock;
import org.jboss.pnc.mock.repository.BuildRecordRepositoryMock;
import org.jboss.pnc.mock.repository.TargetRepositoryRepositoryMock;
import org.jboss.pnc.mock.repository.UserRepositoryMock;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.BuildRequestException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.test.util.Wait;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import java.lang.reflect.Field;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/16/16 Time: 1:19 PM
 */
@SuppressWarnings("deprecation")
public abstract class AbstractDependentBuildTest {

    private Logger logger = LoggerFactory.getLogger(AbstractDependentBuildTest.class);

    protected static final AtomicInteger configIdSequence = new AtomicInteger(0);
    protected static final AtomicInteger configAuditedIdSequence = new AtomicInteger(0);
    protected static final AtomicInteger artifactsIdSequence = new AtomicInteger(0);

    protected List<BuildTask> builtTasks;

    private BuildConfigurationAuditedRepositoryMock buildConfigurationAuditedRepository;

    protected BuildConfigurationRepositoryMock buildConfigurationRepository;

    private BuildQueue buildQueue;

    protected BuildCoordinator coordinator;
    protected BuildRecordRepositoryMock buildRecordRepository;
    protected BuildSchedulerFactory buildSchedulerFactory;

    protected User user;

    @Before
    @SuppressWarnings("unchecked")
    public void initialize() throws DatastoreException, ConfigurationParseException {
        MockitoAnnotations.initMocks(this);

        builtTasks = new ArrayList<>();

        user = new User();
        user.setId(375939);
        user.setUsername("username");

        Configuration config = mock(Configuration.class);
        SystemConfig systemConfig = mock(SystemConfig.class);
        when(systemConfig.getCoordinatorThreadPoolSize()).thenReturn(1);
        when(systemConfig.getCoordinatorMaxConcurrentBuilds()).thenReturn(1);
        when(systemConfig.getTemporaryBuildsLifeSpan()).thenReturn(1);
        when(config.getModuleConfig(any())).thenReturn(systemConfig);

        buildQueue = new BuildQueue(config.getModuleConfig(new PncConfigProvider<>(SystemConfig.class)));

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
                targetRepositoryRepository);
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore, mock(BifrostLogUploader.class));

        if (buildSchedulerFactory == null) {
            buildSchedulerFactory = new MockBuildSchedulerFactory();
        }

        coordinator = new DefaultBuildCoordinator(
                datastoreAdapter,
                mock(Event.class),
                mock(Event.class),
                buildSchedulerFactory,
                buildQueue,
                systemConfig,
                mock(GroupBuildMapper.class),
                mock(BuildMapper.class));
        buildQueue.initSemaphore();
        coordinator.start();
    }

    protected void insertNewBuildRecords(BuildConfiguration... configs) {
        Stream.of(configs).forEach(this::insertNewBuildRecord);
    }

    private void insertNewBuildRecord(BuildConfiguration config) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buildRecordRepository.save(buildRecord(config));
    }

    protected BuildRecord buildRecord(BuildConfiguration config) {
        BuildConfigurationAudited configurationAudited = buildConfigurationAuditedRepository
                .findAllByIdOrderByRevDesc(config.getId())
                .iterator()
                .next();
        return BuildRecord.Builder.newBuilder()
                .id(Sequence.nextBase32Id())
                .status(BuildStatus.SUCCESS)
                .buildConfigurationAudited(configurationAudited)
                .temporaryBuild(false)
                .submitTime(new Date())
                .startTime(new Date())
                .endTime(new Date())
                .build();
    }

    protected BuildConfiguration config(String name, BuildConfiguration... dependencies) {
        BuildConfiguration config = buildConfig(name, dependencies);
        saveConfig(config);
        return config;
    }

    protected BuildConfiguration buildConfig(String name, BuildConfiguration... dependencies) {
        int id = configIdSequence.getAndIncrement();
        Project project = Project.Builder.newBuilder().id(1).name("Mock project").build();

        BuildConfiguration config = BuildConfiguration.Builder.newBuilder()
                .id(id)
                .name(name)
                .project(project)
                .repositoryConfiguration(RepositoryConfigurationMock.newTestRepository())
                .buildEnvironment(BuildEnvironmentMock.newTest())
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
        Stream.of(configurations).forEach(c -> buildConfigurationAuditedRepository.save(auditedConfig(c)));
    }

    private BuildConfigurationAudited auditedConfig(BuildConfiguration config) {
        int rev = configAuditedIdSequence.incrementAndGet();
        return BuildConfigurationAudited.fromBuildConfiguration(config, rev);
    }

    protected void build(BuildConfigurationSet configSet, RebuildMode rebuildMode) throws CoreException {
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(rebuildMode);

        coordinator.buildSet(configSet, user, buildOptions);
        coordinator.start();
    }

    protected void build(BuildConfiguration config) {
        BuildOptions buildOptions = new BuildOptions();
        build(config, buildOptions);
    }

    protected void build(BuildConfiguration config, BuildOptions buildOptions) {
        try {

            coordinator.buildConfig(config, user, buildOptions);
        } catch (CoreException | BuildRequestException | BuildConflictException e) {
            throw new RuntimeException("Failed to run a build of: " + config, e);
        }
    }

    protected BuildTask getBuildTaskById(String taskId) {
        Optional<BuildTask> buildTask = builtTasks.stream().filter(bt -> bt.getId().equals(taskId)).findAny();
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
        Supplier<String> errorMessage = () -> "Tired waiting for BuildQueue to be empty. There are still tasks in the queue:\n"
                + buildQueue.getDebugInfo();
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
        public void startBuilding(BuildTask buildTask) throws CoreException {
            builtTasks.add(buildTask);
            BuildResult result = buildResult();
            coordinator.completeBuild(buildTask, result);
        }

        @Override
        public void startBuilding(BuildSetTask buildSetTask) throws CoreException {
            throw new UnsupportedOperationException("Only to be used with remote build scheduler.");
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

    protected void expectBuilt(BuildConfiguration... configurations) throws InterruptedException, TimeoutException {
        waitForEmptyBuildQueue();
        List<BuildConfiguration> configsWithTasks = getBuiltConfigs();
        assertThat(configsWithTasks).hasSameElementsAs(Arrays.asList(configurations));
    }

    @RequiredArgsConstructor
    protected class DependencyHandler {
        final BuildConfiguration config;
        BuildRecord record;

        public void dependOn(BuildConfiguration... dependencies) {
            record = buildRecordRepository
                    .getPreferredLatestSuccessfulBuildRecordWithBuildConfig(config.getId(), false, null);

            Set<Artifact> artifacts = Stream.of(dependencies)
                    .map(this::mockArtifactBuiltWith)
                    .collect(Collectors.toSet());
            record.setDependencies(artifacts);
            artifacts.stream().forEach(artifact -> artifact.addDependantBuildRecord(record));
        }

        private Artifact mockArtifactBuiltWith(BuildConfiguration config) {
            BuildRecord record = buildRecordRepository
                    .getPreferredLatestSuccessfulBuildRecordWithBuildConfig(config.getId(), false, null);

            Artifact artifact = Artifact.Builder.newBuilder()
                    .id(artifactsIdSequence.incrementAndGet())
                    .buildRecord(record)
                    .build();
            try {
                Field field = BuildRecord.class.getDeclaredField("builtArtifacts");
                field.setAccessible(true);
                ((Set<Artifact>) field.get(record)).add(artifact);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
            return artifact;
        }
    }
}
