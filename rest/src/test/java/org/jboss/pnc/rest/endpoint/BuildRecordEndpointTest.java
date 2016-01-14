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
package org.jboss.pnc.rest.endpoint;

import org.jboss.pnc.executor.DefaultBuildExecutionConfiguration;
import org.jboss.pnc.executor.DefaultBuildExecutionSession;
import org.jboss.pnc.executor.DefaultBuildExecutor;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class BuildRecordEndpointTest {

    @Test
    public void getLogsNoContentTest() {
        // given
        int logId = 1;
        String logContent = "";

        // when
        BuildRecordEndpoint buildRecordEndpoint = getLogsPrepareEndpoint(logId, logContent);

        // then
        assertEquals(204, buildRecordEndpoint.getLogs(logId).getStatus());
    }

    @Test
    public void getLogsWithContentTest() {
        // given
        int logId = 1;
        String logContent = "LOG CONTENT";
        
        // when
        BuildRecordEndpoint buildRecordEndpoint = getLogsPrepareEndpoint(logId, logContent);

        // then
        assertEquals(200, buildRecordEndpoint.getLogs(logId).getStatus());
    }

    private BuildRecordEndpoint getLogsPrepareEndpoint(int logId, String logContent) {
        BuildRecordRepository buildRecordRepository = mock(BuildRecordRepository.class);
        BuildExecutor buildExecutor = mockBuildExecutor(5684, 4538);
        BuildRecordProvider buildRecordProvider = new BuildRecordProvider(buildRecordRepository, null, null, null, null, buildExecutor);
        BuildRecordEndpoint buildRecordEndpoint = new BuildRecordEndpoint(buildRecordProvider, null);
        BuildRecord buildRecord = mock(BuildRecord.class);

        Mockito.when(buildRecord.getBuildLog()).thenReturn(logContent);
        Mockito.when(buildRecordRepository.findByIdFetchAllProperties(logId)).thenReturn(buildRecord);
        return buildRecordEndpoint;
    }

    private BuildExecutor mockBuildExecutor(int buildExecutionTaskId, int buildTaskId) {
        BuildExecutor buildExecutor = mock(DefaultBuildExecutor.class);

        BuildExecutionConfiguration buildExecutionConfiguration = new DefaultBuildExecutionConfiguration(
                buildExecutionTaskId,
                "build-content-id",
                1,
                "",
                "build-1",
                "",
                "",
                "",
                "",
                BuildType.JAVA);

        BuildExecutionSession buildExecutionSession = new DefaultBuildExecutionSession(buildExecutionConfiguration, null);
        when(buildExecutor.getRunningExecution(buildExecutionTaskId)).thenReturn(buildExecutionSession);
        return buildExecutor;
    }

}
