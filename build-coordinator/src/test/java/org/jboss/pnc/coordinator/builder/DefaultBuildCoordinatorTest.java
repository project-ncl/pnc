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
package org.jboss.pnc.coordinator.builder;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.mock.model.MockUser;
import org.jboss.pnc.mock.repour.RepourResultMock;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.enterprise.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.jboss.pnc.spi.exception.BuildConflictException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/12/16
 * Time: 2:33 PM
 */
public class DefaultBuildCoordinatorTest {
    private static final User USER = new User();
    private static final BuildOptions BUILD_OPTIONS = new BuildOptions();
    private static final Project PROJECT = Project.Builder.newBuilder().id(123).build();
    private static final BuildConfiguration BC_3 = BuildConfiguration.Builder.newBuilder()
            .id(3)
            .project(PROJECT)
            .name("build-config-3")
            .build();
    private static final BuildConfiguration BC_2 = BuildConfiguration.Builder.newBuilder()
            .id(2)
            .project(PROJECT)
            .name("build-config-2")
            .build();
    private static final BuildConfiguration BC_1 = BuildConfiguration.Builder.newBuilder()
            .id(1)
            .project(PROJECT)
            .name("build-config")
            .dependency(BC_2)
            .build();
    private static final BuildConfigurationSet BCS = BuildConfigurationSet.Builder.newBuilder()
            .id(88)
            .buildConfiguration(BC_1)
            .buildConfiguration(BC_3)
            .build();
    static{
        PROJECT.addBuildConfiguration(BC_1);
        PROJECT.addBuildConfiguration(BC_2);
        PROJECT.addBuildConfiguration(BC_3);
    }

    @Mock
    private Datastore datastore;
    @Mock
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;
    @Mock
    private BuildSchedulerFactory buildSchedulerFactory;
    @Mock
    private BuildQueue buildQueue;
    @Mock
    private SystemConfig systemConfig;
    @Mock
    private Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier;

    @InjectMocks
    private DatastoreAdapter datastoreAdapter;

    private BuildCoordinator coordinator;


    @Before
    public void setUp() throws DatastoreException {
        MockitoAnnotations.initMocks(this);
        when(systemConfig.getTemporalBuildExpireDate()).thenReturn(new Date(1));
        when(datastore.requiresRebuild(any(BuildConfiguration.class))).thenReturn(true);
        when(datastore.requiresRebuild(any(BuildTask.class))).thenReturn(true);
        when(datastore.saveBuildConfigSetRecord(any())).thenAnswer(new SaveBuildConfigSetRecordAnswer());
        coordinator = new DefaultBuildCoordinator(
                datastoreAdapter,
                buildStatusChangedEventNotifier,
                buildSetStatusChangedEventNotifier,
                buildSchedulerFactory,
                buildQueue,
                systemConfig);
    }

    @Test
    public void shouldStoreSshCredentialsOnSshEnabled() throws DatastoreException {
        BuildTask buildTask = mockBuildTask();
        BuildResult buildResult = mockBuildResult(true);

        SshCredentials sshCredentials = new SshCredentials();
        sshCredentials.setCommand(RandomStringUtils.randomAlphabetic(30));
        sshCredentials.setPassword(RandomStringUtils.randomAlphabetic(30));

        when(buildResult.getEnvironmentDriverResult()).thenReturn(Optional.of(new EnvironmentDriverResult(CompletionStatus.FAILED, "", Optional.of(sshCredentials))));

        when(buildResult.getRepourResult()).thenReturn(Optional.of(RepourResultMock.mock()));

        ArgumentGrabbingAnswer<BuildRecord.Builder> answer = new ArgumentGrabbingAnswer<>(BuildRecord.Builder.class);
        when(datastore.storeCompletedBuild(any(BuildRecord.Builder.class))).thenAnswer(answer);

        coordinator.completeBuild(buildTask, buildResult);

        assertThat(answer.arguments).hasSize(1);
        BuildRecord.Builder builder = answer.arguments.iterator().next();
        BuildRecord record = builder.build();
        assertThat(record.getSshCommand()).isEqualTo(sshCredentials.getCommand());
        assertThat(record.getSshPassword()).isEqualTo(sshCredentials.getPassword());
    }

    @Test
    public void shouldUpdateBuildRecordSetIfBuildSetBuilIsRejected() throws DatastoreException, CoreException {
        BuildConfigurationSet bcSet = BuildConfigurationSet.Builder.newBuilder()
                .buildConfigurations(Collections.emptySet())
                .name("BCSet").id(1).build();

        BuildSetTask bsTask = coordinator.build(bcSet, USER, BUILD_OPTIONS);
        assertThat(bsTask.getBuildConfigSetRecord().get().getStatus())
            .isEqualTo(BuildStatus.REJECTED);
    }

    @Test
    public void testBuildBuildConfiguration() throws BuildConflictException, CoreException {
        assertTrue("Test assumes that we build with dependencies", BUILD_OPTIONS.isBuildDependencies());

        BuildConfigurationAudited bca = mockDatastoreWithBCAudited(BC_1, 5);
        BuildConfigurationAudited bcaDep = mockDatastoreWithBCAudited(BC_2, 2);


        BuildSetTask buildSetTask = coordinator.build(BC_1, USER, BUILD_OPTIONS);


        assertEquals(2, buildSetTask.getBuildTasks().size());
        assertNotNull(buildSetTask.getBuildTask(bca));
        assertNotNull(buildSetTask.getBuildTask(bcaDep));
        assertSame(BUILD_OPTIONS, buildSetTask.getBuildOptions());
    }

    @Test
    public void testBuildBuildConfigurationAudited() throws BuildConflictException, CoreException {
        assertTrue("Test assumes that we build with dependencies", BUILD_OPTIONS.isBuildDependencies());

        BuildConfigurationAudited bca = mockDatastoreWithBCAudited(BC_1, 5);
        BuildConfigurationAudited bcaDep = mockDatastoreWithBCAudited(BC_2, 2);
        BuildConfigurationAudited reqBCA = toBuildConfigurationAudited(BC_1, 4);
        reqBCA.setName("build-config-changed");


        BuildSetTask buildSetTask = coordinator.build(reqBCA, USER, BUILD_OPTIONS);


        assertEquals(2, buildSetTask.getBuildTasks().size());
        BuildTask buildTask = buildSetTask.getBuildTask(reqBCA);
        assertNotNull(buildTask);
        assertNotNull(buildSetTask.getBuildTask(bcaDep));
        assertNull(buildSetTask.getBuildTask(bca));
        assertEquals(4, buildTask.getBuildConfigurationAudited().getRev().intValue());
        assertEquals("build-config-changed", buildTask.getBuildConfigurationAudited().getName());
        assertSame(BUILD_OPTIONS, buildSetTask.getBuildOptions());
    }

    @Test
    public void testBuildBuildConfigurationSet() throws BuildConflictException, CoreException, DatastoreException {
        BuildConfigurationAudited bca = mockDatastoreWithBCAudited(BC_1, 5);
        BuildConfigurationAudited bcaDep = mockDatastoreWithBCAudited(BC_2, 2);
        BuildConfigurationAudited bca3 = mockDatastoreWithBCAudited(BC_3, 9);

        when(datastore.getBuildConfigurations(BCS)).thenReturn(BCS.getBuildConfigurations());

        BuildSetTask buildSetTask = coordinator.build(BCS, USER, BUILD_OPTIONS);


        assertEquals(2, buildSetTask.getBuildTasks().size());
        assertNotNull(buildSetTask.getBuildTask(bca));
        assertNull(buildSetTask.getBuildTask(bcaDep)); // Dependencies outside group are not build
        assertNotNull(buildSetTask.getBuildTask(bca3));
        assertSame(BUILD_OPTIONS, buildSetTask.getBuildOptions());
    }

    @Test
    public void testBuildBuildConfigurationSetWithAudited() throws BuildConflictException, CoreException, DatastoreException {
        BuildConfigurationAudited bca = mockDatastoreWithBCAudited(BC_1, 5);
        BuildConfigurationAudited bcaDep = mockDatastoreWithBCAudited(BC_2, 2);
        BuildConfigurationAudited bca3 = mockDatastoreWithBCAudited(BC_3, 9);
        BuildConfigurationAudited reqBCA = toBuildConfigurationAudited(BC_1, 4);
        reqBCA.setName("build-config-changed");

        when(datastore.getBuildConfigurations(BCS)).thenReturn(BCS.getBuildConfigurations());


        Map<Integer,BuildConfigurationAudited> overrides = Collections.singletonMap(1, reqBCA);
        BuildSetTask buildSetTask = coordinator.build(BCS, overrides, USER, BUILD_OPTIONS);


        assertEquals(2, buildSetTask.getBuildTasks().size());
        BuildTask buildTask = buildSetTask.getBuildTask(reqBCA);
        assertNotNull(buildTask);
        assertNull(buildSetTask.getBuildTask(bca));
        assertNull(buildSetTask.getBuildTask(bcaDep)); // Dependencies outside group are not build
        assertNotNull(buildSetTask.getBuildTask(bca3));
        assertEquals(4, buildTask.getBuildConfigurationAudited().getRev().intValue());
        assertEquals("build-config-changed", buildTask.getBuildConfigurationAudited().getName());
        assertSame(BUILD_OPTIONS, buildSetTask.getBuildOptions());
    }

    private BuildConfigurationAudited mockDatastoreWithBCAudited(BuildConfiguration bc, int rev){
        BuildConfigurationAudited bca = toBuildConfigurationAudited(bc, rev);

        when(datastore.getLatestBuildConfigurationAudited(bc.getId())).thenReturn(bca);
        when(datastore.getLatestBuildConfigurationAuditedLoadBCDependencies(bc.getId()))
                .thenReturn(bca);
        return bca;
    }

    private BuildConfigurationAudited toBuildConfigurationAudited(BuildConfiguration bc, int rev) {
        BuildConfigurationAudited bca = BuildConfigurationAudited.fromBuildConfiguration(bc, rev);
        bca.setRev(rev);
        return bca;
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

        BuildOptions buildOptions = new BuildOptions(false, false, true, false, false);
        BuildTask buildTask = BuildTask.build(
                BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, 13),
                buildOptions,
                MockUser.newTestUser(1),
                1,
                null,
                new Date(),
                null,
                "context-id"
        );

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

}