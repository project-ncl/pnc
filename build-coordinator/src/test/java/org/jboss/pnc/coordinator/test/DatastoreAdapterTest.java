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

import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DatastoreAdapterTest {

    private static final String REPOSITORY_MANAGER_LOG = "Repository manager log.";
    private static final String BUILD_LOG = "Build Driver log.";;

    @Test
    public void shouldStoreRepositoryManagerSuccessResult() throws DatastoreException {
        //given
        DatastoreMock datastore = new DatastoreMock();
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        BuildStatus buildStatus = BuildStatus.SUCCESS;
        RepositoryManagerStatus repositoryManagerStatus = RepositoryManagerStatus.SUCCESS;

        //when
        storeResult(datastoreAdapter, buildStatus, repositoryManagerStatus);

        //then
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals(1, buildRecords.size());
        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertEquals(buildRecord.getStatus(), BuildStatus.SUCCESS);
        Assert.assertTrue(buildRecord.getBuildLog().contains(BUILD_LOG));
        Assert.assertTrue(buildRecord.getBuildLog().contains(REPOSITORY_MANAGER_LOG));
    }

    @Test
    public void shouldStoreRepositoryManagerError() throws DatastoreException {
        //given
        DatastoreMock datastore = new DatastoreMock();
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        BuildStatus buildStatus = BuildStatus.SUCCESS;
        RepositoryManagerStatus repositoryManagerStatus = RepositoryManagerStatus.VALIDATION_ERROR;

        //when
        storeResult(datastoreAdapter, buildStatus, repositoryManagerStatus);

        //then
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals(1, buildRecords.size());
        BuildRecord buildRecord = buildRecords.get(0);

        Assert.assertEquals(buildRecord.getStatus(), BuildStatus.FAILED);
        Assert.assertTrue(buildRecord.getBuildLog().contains(BUILD_LOG));
        Assert.assertTrue(buildRecord.getBuildLog().contains(REPOSITORY_MANAGER_LOG));
    }

    private void storeResult(DatastoreAdapter datastoreAdapter, BuildStatus buildStatus, RepositoryManagerStatus repositoryManagerStatus) throws DatastoreException {
        BuildDriverResult buildDriverResult = mock(BuildDriverResult.class);
        when(buildDriverResult.getBuildStatus()).thenReturn(buildStatus);
        when(buildDriverResult.getBuildLog()).thenReturn(BUILD_LOG);

        RepositoryManagerResult repositoryManagerResult = mock(RepositoryManagerResult.class);
        when(repositoryManagerResult.getStatus()).thenReturn(repositoryManagerStatus);
        when(repositoryManagerResult.getLog()).thenReturn(REPOSITORY_MANAGER_LOG);


        BuildTask buildTask = mock(BuildTask.class);
        when(buildTask.getId()).thenReturn(123);

        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);

        BuildResult buildResult = new BuildResult(
                Optional.of(buildExecutionConfiguration),
                Optional.of(buildDriverResult),
                Optional.of(repositoryManagerResult),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        datastoreAdapter.storeResult(buildTask, buildResult);
    }

}
