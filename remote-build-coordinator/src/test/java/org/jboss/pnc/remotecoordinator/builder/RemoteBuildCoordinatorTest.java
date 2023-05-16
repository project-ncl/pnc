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

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.mock.datastore.BuildTaskRepositoryMock;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.remotecoordinator.test.BuildCoordinatorDeployments;
import org.jboss.pnc.remotecoordinator.test.mock.MockBuildScheduler;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.DefaultBuildTaskRef;
import org.jboss.pnc.spi.coordinator.Remote;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.ScheduleConflictException;
import org.jboss.pnc.spi.exception.ScheduleErrorException;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Arquillian.class)
public class RemoteBuildCoordinatorTest {
    private DatastoreAdapter datastoreAdapter;

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
    }

    @Inject
    @Remote
    BuildCoordinator buildCoordinator;

    @Inject
    TestProjectConfigurationBuilder testProjectConfigurationBuilder;

    @Inject
    DatastoreMock datastoreMock;

    @Inject
    BuildTaskRepositoryMock taskRepository;

    @Inject
    MockBuildScheduler buildScheduler;

    private static final User USER = new User();

    @Before
    public void setUp() {
        datastoreMock.clear();
        buildScheduler.reset();
        datastoreAdapter = new DatastoreAdapter(datastoreMock);
        USER.setId(1);
    }

    /**
     * TODO - test storing the results of sys_error builds - test conflicting schedule retries
     */

    @Test
    public void shouldRejectSingleBuildIfItsAlreadyRunning() throws Exception {
        BuildConfiguration bc200 = testProjectConfigurationBuilder.buildWithDependencies(200, "Project-223");

        taskRepository.addTask(getBuildTaskRef(bc200, BuildCoordinationStatus.BUILDING));

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.IMPLICIT_DEPENDENCY_CHECK);

        try {
            buildCoordinator.buildConfig(bc200, USER, buildOptions);
        } catch (Exception e) {
            Assertions.assertThat(e).isInstanceOf(BuildConflictException.class);
            Assertions.assertThat(e.getMessage()).contains("already running");
            return;
        }
        Assert.fail("Did not throw expected exception.");
    }

    @Test
    public void shouldRejectBuildSetIfAllAreAlreadyRunning() throws Exception {
        BuildConfigurationSet set = new BuildConfigurationSet();
        set.setName("test-build-configuration");
        set.setId(1);

        BuildConfiguration bc202 = testProjectConfigurationBuilder.build(202, "Project-223", set);
        BuildConfiguration bc201 = testProjectConfigurationBuilder
                .buildWithDependencies(201, "Project-223", set, bc202);
        BuildConfiguration bc200 = testProjectConfigurationBuilder
                .buildWithDependencies(200, "Project-223", set, bc201);

        taskRepository.addTask(getBuildTaskRef(bc200, BuildCoordinationStatus.BUILDING));
        taskRepository.addTask(getBuildTaskRef(bc201, BuildCoordinationStatus.BUILDING));
        taskRepository.addTask(getBuildTaskRef(bc202, BuildCoordinationStatus.BUILDING));

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Map<Integer, BuildConfigurationAudited> bcas = new HashMap<>();
        addBCAsToMap(bc200, bcas);
        addBCAsToMap(bc201, bcas);
        addBCAsToMap(bc202, bcas);
        try {
            buildCoordinator.buildSet(set, bcas, USER, buildOptions);
        } catch (Exception e) {
            Assertions.assertThat(e).isInstanceOf(BuildConflictException.class);
            Assertions.assertThat(e.getMessage()).contains("All the build configurations are already running");
            return;
        }
        Assert.fail("Did not throw expected exception.");
    }

    @Test
    public void shouldStoreNoRebuildRequired() throws Exception {
        BuildConfiguration bc202 = testProjectConfigurationBuilder.build(202, "Project-223");
        BuildConfiguration bc201 = testProjectConfigurationBuilder.buildWithDependencies(201, "Project-223", bc202);
        BuildConfiguration bc200 = testProjectConfigurationBuilder.buildWithDependencies(200, "Project-223", bc201);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Map<Integer, BuildConfigurationAudited> bcas = new HashMap<>();
        addBCAsToMap(bc200, bcas);
        addBCAsToMap(bc201, bcas);
        addBCAsToMap(bc202, bcas);

        datastoreMock.addNoRebuildRequiredBCAIdREv(bcas.get(bc202.getId()).getIdRev());
        datastoreMock.addNoRebuildRequiredBCAIdREv(bcas.get(bc201.getId()).getIdRev());

        buildCoordinator.buildConfig(bc200, USER, buildOptions);

        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        Assert.assertEquals(2, buildRecords.size());
        Assert.assertEquals(BuildStatus.NO_REBUILD_REQUIRED, buildRecords.get(0).getStatus());

        // make sure no_rebuild tasks are not scheduled
        Assert.assertEquals(1, buildScheduler.getActiveBuildTasks().size());
    }

    @Test
    public void shouldStoreNoRebuildRequiredWhenRunningASet() throws Exception {
        BuildConfigurationSet set = new BuildConfigurationSet();
        set.setName("test-build-configuration");
        set.setId(1);

        BuildConfiguration bc202 = testProjectConfigurationBuilder.build(202, "Project-223", set);
        BuildConfiguration bc201 = testProjectConfigurationBuilder
                .buildWithDependencies(201, "Project-223", set, bc202);
        BuildConfiguration bc200 = testProjectConfigurationBuilder
                .buildWithDependencies(200, "Project-223", set, bc201);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Map<Integer, BuildConfigurationAudited> bcas = new HashMap<>();
        addBCAsToMap(bc200, bcas);
        addBCAsToMap(bc201, bcas);
        addBCAsToMap(bc202, bcas);

        datastoreMock.addNoRebuildRequiredBCAIdREv(bcas.get(bc202.getId()).getIdRev());
        datastoreMock.addNoRebuildRequiredBCAIdREv(bcas.get(bc201.getId()).getIdRev());

        buildCoordinator.buildSet(set, bcas, USER, buildOptions);

        List<BuildConfigSetRecord> buildConfigSetRecords = datastoreMock.getBuildConfigSetRecords();
        Assert.assertEquals(1, buildConfigSetRecords.size());

        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        Assert.assertEquals(2, buildRecords.size());
        Assert.assertEquals(BuildStatus.NO_REBUILD_REQUIRED, buildRecords.get(0).getStatus());

        // make sure no_rebuild tasks are not scheduled
        Assert.assertEquals(1, buildScheduler.getActiveBuildTasks().size());
    }

    @Test
    public void shouldRetryAndThrowWhenBuildSchedulingFails() throws Exception {
        buildScheduler.setScheduleException(new ScheduleConflictException("Intentionally failing to start the build."));

        BuildConfiguration bc200 = testProjectConfigurationBuilder.buildWithDependencies(200, "Project-223");

        BuildOptions buildOptions = new BuildOptions();

        try {
            buildCoordinator.buildConfig(bc200, USER, buildOptions);
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("failing to start");
        }
        // make sure there were retries
        Assert.assertTrue(buildScheduler.getScheduleRequests().size() > 1);
    }

    @Test
    public void shouldNotRetryWhenBuildSchedulingFailsWithError() throws Exception {
        buildScheduler.setScheduleException(new ScheduleErrorException("Intentionally failing to start the build."));

        BuildConfiguration bc200 = testProjectConfigurationBuilder.buildWithDependencies(200, "Project-223");

        BuildOptions buildOptions = new BuildOptions();

        try {
            buildCoordinator.buildConfig(bc200, USER, buildOptions);
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("failing to start");
        }
        // make sure there were NO retries
        Assert.assertTrue(buildScheduler.getScheduleRequests().size() == 1);
    }

    @Test
    public void shouldRetryAndThrowWhenBuildSetSchedulingFails() throws Exception {
        buildScheduler.setScheduleException(new ScheduleConflictException("Intentionally failing to start the build."));

        BuildConfigurationSet set = testProjectConfigurationBuilder.buildConfigurationSet(1);

        BuildConfiguration bc202 = testProjectConfigurationBuilder.build(202, "Project-223", set);
        BuildConfiguration bc201 = testProjectConfigurationBuilder
                .buildWithDependencies(201, "Project-223", set, bc202);
        BuildConfiguration bc200 = testProjectConfigurationBuilder
                .buildWithDependencies(200, "Project-223", set, bc201);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Map<Integer, BuildConfigurationAudited> bcas = new HashMap<>();
        addBCAsToMap(bc200, bcas);
        addBCAsToMap(bc201, bcas);
        addBCAsToMap(bc202, bcas);
        try {
            buildCoordinator.buildSet(set, bcas, USER, buildOptions);
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("failing to start");
        }
        // make sure there were retries
        Assert.assertTrue(buildScheduler.getScheduleRequests().size() > 1);
    }

    @Test
    public void shouldNotRetryWhenBuildSetSchedulingFailsWithError() throws Exception {
        buildScheduler.setScheduleException(new ScheduleErrorException("Intentionally failing to start the build."));

        BuildConfigurationSet set = testProjectConfigurationBuilder.buildConfigurationSet(1);

        BuildConfiguration bc202 = testProjectConfigurationBuilder.build(202, "Project-223", set);
        BuildConfiguration bc201 = testProjectConfigurationBuilder
                .buildWithDependencies(201, "Project-223", set, bc202);
        BuildConfiguration bc200 = testProjectConfigurationBuilder
                .buildWithDependencies(200, "Project-223", set, bc201);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Map<Integer, BuildConfigurationAudited> bcas = new HashMap<>();
        addBCAsToMap(bc200, bcas);
        addBCAsToMap(bc201, bcas);
        addBCAsToMap(bc202, bcas);
        try {
            buildCoordinator.buildSet(set, bcas, USER, buildOptions);
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("failing to start");
        }
        // make sure there were NO retries
        Assert.assertTrue(buildScheduler.getScheduleRequests().size() == 1);
    }

    private BuildTaskRef getBuildTaskRef(BuildConfiguration bc200, BuildCoordinationStatus status) {
        BuildConfigurationAudited audited = getBuildConfigurationAudited(bc200);
        return DefaultBuildTaskRef.builder()
                .id(String.valueOf(audited.getId()))
                .idRev(audited.getIdRev())
                .submitTime(Instant.now())
                .status(status)
                .build();
    }

    private BuildConfigurationAudited getBuildConfigurationAudited(BuildConfiguration buildConfiguration) {
        return datastoreAdapter.getLatestBuildConfigurationAuditedInitializeBCDependencies(buildConfiguration.getId());
    }

    private void addBCAsToMap(BuildConfiguration buildConfiguration, Map<Integer, BuildConfigurationAudited> bcas) {
        bcas.put(buildConfiguration.getId(), getBuildConfigurationAudited(buildConfiguration));
    }

}
