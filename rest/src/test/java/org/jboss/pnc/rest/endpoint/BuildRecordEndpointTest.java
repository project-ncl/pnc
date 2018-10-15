/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.executor.DefaultBuildExecutionConfiguration;
import org.jboss.pnc.executor.DefaultBuildExecutionSession;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.notifications.websockets.DefaultNotifier;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.common.util.RandomUtils.randInt;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class BuildRecordEndpointTest {

    private static final int CURRENT_USER = randInt(1000, 100000);

    private final static int BUILD_RECORD_NOT_VALID_ID = 99999;
    private final static int DEF_PAGE_SIZE = 100;
    private final static int DEF_PAGE_INDEX = 0;

    @Mock
    private BuildExecutor buildExecutor;
    @Mock
    private BuildRecordRepository buildRecordRepository;
    @Mock
    private Datastore datastore;
    @Mock
    private EndpointAuthenticationProvider authProvider;
    @Mock
    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    @InjectMocks
    private BuildRecordProvider buildRecordProvider = new BuildRecordProvider();

    private BuildRecordEndpoint endpoint;
    private ArtifactProvider artifactProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        this.artifactProvider = mock(ArtifactProvider.class);
        when(artifactProvider.getBuiltArtifactsForBuildRecord(DEF_PAGE_INDEX, DEF_PAGE_SIZE, null, null,
                BUILD_RECORD_NOT_VALID_ID)).thenReturn(new CollectionInfo<>(DEF_PAGE_INDEX, DEF_PAGE_SIZE, 0,
                        Collections.emptyList()));

        endpoint = new BuildRecordEndpoint(
                buildRecordProvider,
                artifactProvider,
                authProvider,
                temporaryBuildsCleanerAsyncInvoker,
                new DefaultNotifier());

        User user = mock(User.class);
        when(user.getId()).thenReturn(CURRENT_USER);
        when(authProvider.getCurrentUser(any())).thenReturn(user);
    }

    @Test
    public void shouldGetLogsNoContent() {
        // given
        int logId = 1;
        String logContent = "";

        // when
        endpointReturnsLog(logId, logContent);

        // then
        assertThat(endpoint.getLogs(logId).getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldGetLogsWithContent() {
        // given
        int logId = 1;
        String logContent = "LOG CONTENT";

        // when
        endpointReturnsLog(logId, logContent);

        // then
        assertThat(endpoint.getLogs(logId).getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldReturnNoContentWhenBuildRecordDoesntExists() {
        assertThat(endpoint.getBuiltArtifacts(BUILD_RECORD_NOT_VALID_ID,
                DEF_PAGE_INDEX, DEF_PAGE_SIZE, null, null).getStatus())
        .isEqualTo(204);
    }

    private void endpointReturnsLog(int logId, String logContent) {
        configureBuildExecutorMock(5684);
        BuildRecord buildRecord = mock(BuildRecord.class);

        when(buildRecord.getBuildLog()).thenReturn(logContent);
        when(buildRecordRepository.findByIdFetchAllProperties(logId)).thenReturn(buildRecord);
    }

    private void configureBuildExecutorMock(int buildExecutionTaskId) {

        BuildExecutionConfiguration buildExecutionConfiguration = new DefaultBuildExecutionConfiguration(
                buildExecutionTaskId,
                "build-content-id",
                1,
                "",
                "build-1",
                "",
                "",
                "",
                false,
                BuildType.MVN,
                "",
                "",
                SystemImageType.DOCKER_IMAGE,
                false,
                null,
                new HashMap<>(),
                false,
                null);

        BuildExecutionSession buildExecutionSession = new DefaultBuildExecutionSession(buildExecutionConfiguration, null);
        when(buildExecutor.getRunningExecution(buildExecutionTaskId)).thenReturn(buildExecutionSession);
    }

}
