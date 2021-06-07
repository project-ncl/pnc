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

import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.mock.builddriver.BuildDriverResultMock;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.environmentdriver.EnvironmentDriverResultMock;
import org.jboss.pnc.mock.model.MockUser;
import org.jboss.pnc.mock.repositorymanager.RepositoryManagerResultMock;
import org.jboss.pnc.mock.repour.RepourResultMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repour.RepourResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DatastoreAdapterTest {

    private static final String REPOSITORY_MANAGER_LOG = "Repository manager log.";
    private static final String BUILD_LOG = "Build Driver log.";

    @Test
    public void shouldStoreRepositoryManagerSuccessResult() throws DatastoreException {
        // given
        DatastoreMock datastore = new DatastoreMock();
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        BuildStatus buildStatus = BuildStatus.SUCCESS;
        CompletionStatus completionStatus = CompletionStatus.SUCCESS;

        // when
        storeResult(datastoreAdapter, buildStatus, completionStatus);

        // then
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals(1, buildRecords.size());
        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertEquals(buildRecord.getStatus(), BuildStatus.SUCCESS);
        Assert.assertTrue(buildRecord.getBuildLog().contains(BUILD_LOG));
        Assert.assertTrue(buildRecord.getBuildLog().contains(REPOSITORY_MANAGER_LOG));
    }

    @Test
    public void shouldStoreRepositoryManagerError() throws DatastoreException {
        // given
        DatastoreMock datastore = new DatastoreMock();
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        BuildStatus buildStatus = BuildStatus.SUCCESS;
        CompletionStatus completionStatus = CompletionStatus.FAILED;

        // when
        storeResult(datastoreAdapter, buildStatus, completionStatus);

        // then
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals(1, buildRecords.size());
        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertEquals(BuildStatus.FAILED, buildRecord.getStatus());
        Assert.assertTrue(buildRecord.getBuildLog().contains(BUILD_LOG));
        Assert.assertTrue(buildRecord.getBuildLog().contains(REPOSITORY_MANAGER_LOG));
    }

    @Test
    public void shouldStoreNoRequiredRebuild() throws DatastoreException {
        // given
        DatastoreMock datastore = new DatastoreMock();
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        // when
        datastoreAdapter.storeRecordForNoRebuild(mockBuildTask());

        // then
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals(1, buildRecords.size());
        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertEquals(BuildStatus.NO_REBUILD_REQUIRED, buildRecord.getStatus());
    }

    @Test
    public void shouldStoreRepourResult() throws DatastoreException {
        // given
        DatastoreMock datastore = new DatastoreMock();
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        RepourResult repourResult = RepourResultMock.mock();

        // when
        BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder().name("Configuration.").build();

        BuildConfigurationAudited buildConfigurationAudited = BuildConfigurationAudited.Builder.newBuilder()
                .buildConfiguration(buildConfiguration)
                .build();

        BuildTask buildTask = mockBuildTask();
        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);

        BuildResult buildResult = new BuildResult(
                CompletionStatus.SUCCESS,
                Optional.empty(),
                "",
                Optional.of(buildExecutionConfiguration),
                Optional.of(BuildDriverResultMock.mockResult(BuildStatus.SUCCESS)),
                Optional.of(RepositoryManagerResultMock.mockResult()),
                Optional.of(EnvironmentDriverResultMock.mock()),
                Optional.of(repourResult));

        datastoreAdapter.storeResult(buildTask, buildResult);

        // then
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals(1, buildRecords.size());
        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertEquals(buildRecord.getStatus(), BuildStatus.SUCCESS);
        Assert.assertEquals(repourResult.getExecutionRootName(), buildRecord.getExecutionRootName());
        Assert.assertEquals(repourResult.getExecutionRootVersion(), buildRecord.getExecutionRootVersion());
        Assert.assertEquals(repourResult.getLog(), buildRecord.getRepourLog());
    }

    private void storeResult(
            DatastoreAdapter datastoreAdapter,
            BuildStatus buildStatus,
            CompletionStatus completionStatus) throws DatastoreException {
        BuildDriverResult buildDriverResult = mock(BuildDriverResult.class);
        when(buildDriverResult.getBuildStatus()).thenReturn(buildStatus);
        when(buildDriverResult.getBuildLog()).thenReturn(BUILD_LOG);

        RepositoryManagerResult repositoryManagerResult = mock(RepositoryManagerResult.class);
        when(repositoryManagerResult.getCompletionStatus()).thenReturn(completionStatus);
        when(repositoryManagerResult.getLog()).thenReturn(REPOSITORY_MANAGER_LOG);

        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);

        BuildResult buildResult = new BuildResult(
                CompletionStatus.SUCCESS,
                Optional.empty(),
                "",
                Optional.of(buildExecutionConfiguration),
                Optional.of(buildDriverResult),
                Optional.of(repositoryManagerResult),
                Optional.of(EnvironmentDriverResultMock.mock()),
                Optional.of(RepourResultMock.mock()));

        BuildTask buildTask = mockBuildTask();
        datastoreAdapter.storeResult(buildTask, buildResult);
    }

    private BuildTask mockBuildTask() {
        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfiguration.setId(12);
        buildConfiguration.setName("Configuration.");
        buildConfiguration.setProject(new Project());

        BuildOptions buildOptions = new BuildOptions(false, true, false, false, RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        BuildTask buildTask = BuildTask.build(
                BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, 13),
                buildOptions,
                MockUser.newTestUser(1),
                "123",
                null,
                new Date(),
                null,
                "context-id",
                Optional.empty());

        buildTask.setStatus(BuildCoordinationStatus.DONE);
        return buildTask;
    }

}
