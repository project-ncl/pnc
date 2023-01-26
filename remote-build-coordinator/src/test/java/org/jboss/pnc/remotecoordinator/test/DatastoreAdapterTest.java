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

package org.jboss.pnc.remotecoordinator.test;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.spi.BuildDriverResultMock;
import org.jboss.pnc.mock.spi.EnvironmentDriverResultMock;
import org.jboss.pnc.mock.spi.RepositoryManagerResultMock;
import org.jboss.pnc.mock.spi.RepourResultMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repour.RepourResult;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
// TODO review once the result storing from rex is done
@Ignore
public class DatastoreAdapterTest {

    private static final String REPOSITORY_MANAGER_LOG = "Repository manager log.";
    private static final String BUILD_LOG = "Build Driver log.";

    @Test
    public void shouldStoreNoRequiredRebuild() throws DatastoreException {
        // given
        DatastoreMock datastore = new DatastoreMock();
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        // when
        datastoreAdapter
                .storeRecordForNoRebuild(mockBuildTask(), null, Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);

        // then
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals(1, buildRecords.size());
        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertEquals(BuildStatus.NO_REBUILD_REQUIRED, buildRecord.getStatus());
    }

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

        // BuildTask buildTask = mockBuildTask();
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
        // TODO completed build
        // datastoreAdapter.storeResult(buildTask, buildResult);

        // then
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals(1, buildRecords.size());
        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertEquals(buildRecord.getStatus(), BuildStatus.SUCCESS);
        Assert.assertEquals(repourResult.getExecutionRootName(), buildRecord.getExecutionRootName());
        Assert.assertEquals(repourResult.getExecutionRootVersion(), buildRecord.getExecutionRootVersion());
        Assert.assertEquals(repourResult.getLog(), buildRecord.getRepourLog());
    }

    @Ignore
    @Test
    public void shouldStoreSshCredentialsOnSshEnabled() throws DatastoreException {
        // BuildTask buildTask = mockBuildTask();
        // BuildResult buildResult = mockBuildResult(true);
        //
        // SshCredentials sshCredentials = new SshCredentials();
        // sshCredentials.setCommand(RandomStringUtils.randomAlphabetic(30));
        // sshCredentials.setPassword(RandomStringUtils.randomAlphabetic(30));
        //
        // when(buildResult.getEnvironmentDriverResult()).thenReturn(
        // Optional.of(new EnvironmentDriverResult(CompletionStatus.FAILED, "", Optional.of(sshCredentials))));
        //
        // when(buildResult.getRepourResult()).thenReturn(Optional.of(RepourResultMock.mock()));
        //
        // OldRemoteBuildCoordinatorTest.ArgumentGrabbingAnswer<BuildRecord.Builder> answer = new
        // OldRemoteBuildCoordinatorTest.ArgumentGrabbingAnswer<>(BuildRecord.Builder.class);
        // when(datastore.storeCompletedBuild(any(BuildRecord.Builder.class), any(), any())).thenAnswer(answer);
        //
        // coordinator.completeBuild(buildTask, buildResult);
        //
        // assertThat(answer.arguments).hasSize(1);
        // BuildRecord.Builder builder = answer.arguments.iterator().next();
        // BuildRecord record = builder.build();
        // assertThat(record.getSshCommand()).isEqualTo(sshCredentials.getCommand());
        // assertThat(record.getSshPassword()).isEqualTo(sshCredentials.getPassword());
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

        // BuildTask buildTask = mockBuildTask(); //TODO completed build
        // datastoreAdapter.storeResult(buildTask, buildResult);
    }

    private RemoteBuildTask mockBuildTask() {
        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfiguration.setId(12);
        buildConfiguration.setName("Configuration.");
        buildConfiguration.setProject(new Project());

        BuildOptions buildOptions = new BuildOptions(
                false,
                true,
                false,
                false,
                RebuildMode.IMPLICIT_DEPENDENCY_CHECK,
                AlignmentPreference.PREFER_PERSISTENT);

        RemoteBuildTask remoteBuildTask = new RemoteBuildTask(
                "123",
                Instant.now(),
                BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, 13),
                buildOptions,
                "1",
                false,
                null,
                null);

        return remoteBuildTask;
    }

}
