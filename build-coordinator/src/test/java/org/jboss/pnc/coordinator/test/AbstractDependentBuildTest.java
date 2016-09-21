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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.coordinator.builder.BuildScheduler;
import org.jboss.pnc.coordinator.builder.BuildSchedulerFactory;
import org.jboss.pnc.coordinator.builder.DefaultBuildCoordinator;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.test.util.Wait;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;

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
import static org.mockito.Matchers.eq;
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

    private final List<BuildTask> builtTasks = new ArrayList<>();

    @Mock
    private Datastore datastore;
    @InjectMocks
    private DatastoreAdapter datastoreAdapter = new DatastoreAdapter();

    private BuildQueue buildQueue;

    private BuildCoordinator coordinator;

    @Before
    @SuppressWarnings("unchecked")
    public void initialize() throws DatastoreException, ConfigurationParseException {
        MockitoAnnotations.initMocks(this);
        when(datastore.saveBuildConfigSetRecord(any(BuildConfigSetRecord.class)))
                .thenAnswer(new ReturnsArgumentAt(0));
        when(datastore.getLatestBuildConfigurationAudited(any(Integer.class)))
                .thenReturn(buildConfigAudited());
        Configuration config = mock(Configuration.class);
        SystemConfig systemConfig = mock(SystemConfig.class);
        when(systemConfig.getCoordinatorThreadPoolSize()).thenReturn(1);
        when(systemConfig.getCoordinatorMaxConcurrentBuilds()).thenReturn(1);
        when(config.getModuleConfig(any())).thenReturn(systemConfig);
        buildQueue = new BuildQueue(config);
        coordinator = new DefaultBuildCoordinator(datastoreAdapter, mock(Event.class), mock(Event.class),
                new MockBuildSchedulerFactory(),
                buildQueue,
                config);
        builtTasks.clear();
        buildQueue.initSemaphore();
    }


    protected void markAsAlreadyBuilt(BuildConfiguration... configs) {
        Stream.of(configs).forEach(
                c -> {
                    when(datastore.hasSuccessfulBuildRecord(eq(c))).thenReturn(true);
                    c.addBuildRecord(buildRecord(c));
                }
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

        when(datastore.getLatestBuildConfigurationAudited(eq(id))).thenReturn(buildConfigAudited());

        return config;
    }

    private BuildConfigurationAudited buildConfigAudited() {
        BuildConfigurationAudited configAudited = new BuildConfigurationAudited();

        configAudited.setIdRev(new IdRev(configAuditedIdSequence.getAndIncrement(), RandomUtils.randInt(100000, 10000000)));
        //// buildConfigAudited.getProject().getBuildConfigurations().forEach(BuildConfiguration::getId)
        Project project = new Project();
        configAudited.setProject(project);

        return configAudited;
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
    }

    private static BuildResult buildResult() {
        return new BuildResult(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }
}
