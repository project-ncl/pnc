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
package org.jboss.pnc.remotecoordinator.builder;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mock.datastore.BuildTaskRepositoryMock;
import org.jboss.pnc.mock.model.BuildEnvironmentMock;
import org.jboss.pnc.mock.model.MockUser;
import org.jboss.pnc.mock.model.RepositoryConfigurationMock;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.enterprise.event.Event;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/12/16 Time: 2:33 PM
 */
public class OldRemoteBuildCoordinatorTest {
    private static final User USER = new User();
    private static final BuildOptions BUILD_OPTIONS = new BuildOptions();
    private static final Project PROJECT = Project.Builder.newBuilder().id(123).build();
    private static final BuildConfiguration BC_3 = BuildConfiguration.Builder.newBuilder()
            .id(3)
            .project(PROJECT)
            .name("build-config-3")
            .repositoryConfiguration(RepositoryConfigurationMock.newTestRepository())
            .buildEnvironment(BuildEnvironmentMock.newTest())
            .build();
    private static final BuildConfiguration BC_2 = BuildConfiguration.Builder.newBuilder()
            .id(2)
            .project(PROJECT)
            .name("build-config-2")
            .repositoryConfiguration(RepositoryConfigurationMock.newTestRepository())
            .buildEnvironment(BuildEnvironmentMock.newTest())
            .build();
    private static final BuildConfiguration BC_1 = BuildConfiguration.Builder.newBuilder()
            .id(1)
            .project(PROJECT)
            .name("build-config")
            .dependency(BC_2)
            .repositoryConfiguration(RepositoryConfigurationMock.newTestRepository())
            .buildEnvironment(BuildEnvironmentMock.newTest())
            .build();
    private static final BuildConfigurationSet BCS = BuildConfigurationSet.Builder.newBuilder()
            .id(88)
            .buildConfiguration(BC_1)
            .buildConfiguration(BC_3)
            .build();
    static {
        PROJECT.addBuildConfiguration(BC_1);
        PROJECT.addBuildConfiguration(BC_2);
        PROJECT.addBuildConfiguration(BC_3);
    }

    @Mock
    private Datastore datastore;
    @Mock
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;
    @Mock
    private RexBuildScheduler buildScheduler;

    @Mock
    private SystemConfig systemConfig;
    @Mock
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    @Mock
    private GroupBuildMapper groupBuildMapper;

    @Mock
    private BuildMapper buildMapper;

    @InjectMocks
    private DatastoreAdapter datastoreAdapter;

    private BuildCoordinator coordinator;

    @Mock
    private BuildConfigurationAuditedRepository bcaRepository;

    @Before
    public void setUp() throws DatastoreException {
        MockitoAnnotations.initMocks(this);
        when(systemConfig.getTemporaryBuildsLifeSpan()).thenReturn(14);
        when(systemConfig.getCoordinatorThreadPoolSize()).thenReturn(1);
        when(systemConfig.getCoordinatorMaxConcurrentBuilds()).thenReturn(10);
        BuildTaskRepository taskRepository = new BuildTaskRepositoryMock();
        when(
                datastore.requiresRebuild(
                        any(BuildConfigurationAudited.class),
                        any(Boolean.class),
                        any(Boolean.class),
                        nullable(AlignmentPreference.class),
                        anySet())).thenReturn(true);
        when(
                datastore.requiresRebuild(
                        any(BuildConfigurationAudited.class),
                        any(Boolean.class),
                        any(Boolean.class),
                        nullable(AlignmentPreference.class),
                        anySet(),
                        any())).thenReturn(true);
        when(datastore.saveBuildConfigSetRecord(any())).thenAnswer(new SaveBuildConfigSetRecordAnswer());

        USER.setId(1);

        coordinator = new RemoteBuildCoordinator(
                datastoreAdapter,
                buildStatusChangedEventNotifier,
                buildSetStatusChangedEventNotifier,
                buildScheduler,
                taskRepository,
                bcaRepository,
                systemConfig,
                groupBuildMapper,
                buildMapper);
    }

    private BuildResult mockBuildResult(boolean withSshCredentials) {
        BuildResult result = mock(BuildResult.class);
        BuildDriverResult driverResult = mock(BuildDriverResult.class);
        when(driverResult.getBuildStatus()).thenReturn(BuildStatus.FAILED);
        when(result.getBuildDriverResult()).thenReturn(Optional.of(driverResult));
        RepositoryManagerResult repoManagerResult = mock(RepositoryManagerResult.class);
        when(repoManagerResult.getCompletionStatus()).thenReturn(CompletionStatus.SUCCESS);
        when(result.getRepositoryManagerResult()).thenReturn(Optional.of(repoManagerResult));

        when(result.getBuildExecutionConfiguration()).thenReturn(Optional.of(mock(BuildExecutionConfiguration.class)));
        return result;
    }

    private BuildTask mockBuildTask() {
        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfiguration.setId(12);
        buildConfiguration.setProject(new Project());
        buildConfiguration.setRepositoryConfiguration(RepositoryConfigurationMock.newTestRepository());
        buildConfiguration.setBuildEnvironment(BuildEnvironmentMock.newTest());

        BuildOptions buildOptions = new BuildOptions(
                false,
                true,
                false,
                false,
                RebuildMode.IMPLICIT_DEPENDENCY_CHECK,
                AlignmentPreference.PREFER_PERSISTENT);
        BuildTask buildTask = BuildTask.build(
                BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, 13),
                buildOptions,
                MockUser.newTestUser(1),
                "1",
                null,
                new Date(),
                null,
                "context-id",
                null);

        buildTask.setStatus(BuildCoordinationStatus.DONE);
        return buildTask;
    }

    private static class ArgumentGrabbingAnswer<T> implements Answer<T> {
        private final Class<T> argumentType;
        private final List<T> arguments = new ArrayList<>();

        private ArgumentGrabbingAnswer(Class<T> argumentType) {
            this.argumentType = argumentType;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            arguments.add(invocation.getArgument(0));
            return null;
        }
    }

    private static class SaveBuildConfigSetRecordAnswer implements Answer<BuildConfigSetRecord> {
        private static int id = 1;

        @Override
        public BuildConfigSetRecord answer(InvocationOnMock invocation) throws Throwable {
            BuildConfigSetRecord arg = invocation.getArgument(0);
            if (arg.getId() == null) {
                arg.setId(id++);
            }
            return arg;
        }
    }

    private static class SaveRecordForNoRebuildAnswer implements Answer<BuildRecord> {
        private Set<BuildRecord> storedRecords;

        public SaveRecordForNoRebuildAnswer(Set<BuildRecord> storedRecords) {
            this.storedRecords = storedRecords;
        }

        @Override
        public BuildRecord answer(InvocationOnMock invocation) throws Throwable {
            BuildRecord arg = invocation.getArgument(0);
            if (arg.getId() == null) {
                throw new IllegalArgumentException("Build Record must have ID set");
            }
            storedRecords.add(arg);
            return arg;
        }
    }
}
