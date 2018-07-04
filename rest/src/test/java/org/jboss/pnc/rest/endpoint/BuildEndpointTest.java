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

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.common.util.RandomUtils.randInt;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * @author Jakub Bartecek
 */
public class BuildEndpointTest {

    private static final int CURRENT_USER = randInt(1000, 100000);

    @Mock
    private BuildExecutor buildExecutor;
    @Mock
    private BuildRecordRepository buildRecordRepository;
    @Mock
    private Datastore datastore;
    @Mock
    private EndpointAuthenticationProvider authProvider;
    @Mock
    private BuildCoordinator buildCoordinator;
    @Mock
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    /** new BuildRecordProvider(buildRecordRepository, buildCoordinator, null, null, buildConfigurationAuditedRepository, buildExecutor);*/
    @InjectMocks
    private BuildRecordProvider buildRecordProvider = new BuildRecordProvider();

    private BuildEndpoint endpoint;

    @Mock
    private BuildTriggerer buildTriggerer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        endpoint = new BuildEndpoint(buildRecordProvider, authProvider, buildTriggerer);

        User user = mock(User.class);
        when(user.getId()).thenReturn(CURRENT_USER);
        when(authProvider.getCurrentUser(any())).thenReturn(user);
        when(buildRecordProvider.getSpecific(any())).thenReturn(null);
    }

    @Test
    public void shouldReturnSshCredentialsForBuildRequester() {
        //given
        int buildRecordId = randInt(10000, 20000);
        String sshCommand = randomAlphabetic(20);
        String sshPassword = randomAlphabetic(20);
        prepareEndpointForKeepPodAlive(buildRecordId, CURRENT_USER, sshCommand, sshPassword);

        // when
        Singleton<SshCredentials> sshCredentialsSingleton = getSshCredentials(buildRecordId, 200);
        SshCredentials sshCredentials = sshCredentialsSingleton.getContent();

        // then
        assertThat(sshCredentials).isNotNull();
        assertThat(sshCredentials.getCommand()).isEqualTo(sshCommand);
        assertThat(sshCredentials.getPassword()).isEqualTo(sshPassword);
    }

    @Test
    public void shouldNotReturnSshCredentialsForOtherUsers() {
        //given
        int buildRecordId = randInt(10000, 20000);
        String sshCommand = randomAlphabetic(20);
        String sshPassword = randomAlphabetic(20);
        prepareEndpointForKeepPodAlive(buildRecordId, CURRENT_USER + 1, sshCommand, sshPassword);

        // when
        Object entity = getSshCredentials(buildRecordId, 204);
        assertThat(entity).isNull();

    }

    @Test
    public void shouldMarkBuildAsTemporaryWhenSpecifiedAndNoRunningBuildExecution() {
        // given
        int buildId = 1;
        List<BuildTask> buildTasks = new ArrayList<>();

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setTemporaryBuild(true);

        BuildSetTask buildSetTask = mock(BuildSetTask.class);
        when(buildSetTask.getBuildConfigSetRecord()).thenReturn(Optional.empty());

        User user = User.Builder.newBuilder()
                .id(CURRENT_USER)
                .build();

        IdRev idRev = new IdRev(1, 1);
        BuildConfigurationAudited buildConfigurationAudited = new BuildConfigurationAudited();
        buildConfigurationAudited.setIdRev(idRev);
        when(buildConfigurationAuditedRepository.queryById(idRev)).thenReturn(buildConfigurationAudited);

        buildTasks.add( BuildTask.build(null, buildConfigurationAudited, buildOptions, user, buildId, buildSetTask, null, null, "context-id"));
        when(buildCoordinator.getSubmittedBuildTasks()).thenReturn(buildTasks);

        // when
        Response response = endpoint.getSpecific(buildId);
        assertThat(response.getStatus()).isEqualTo(200);
        Singleton<BuildRecordRest> buildRecordSingleton = (Singleton<BuildRecordRest>) response.getEntity();

        // then
        assertThat(buildRecordSingleton.getContent().getTemporaryBuild()).isEqualTo(true);
    }

    private void prepareEndpointForKeepPodAlive(int buildRecordId, int buildRequesterId, String sshCommand, String sshPassword) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(buildRequesterId);

        BuildRecord record = mock(BuildRecord.class);
        when(record.getSshCommand()).thenReturn(sshCommand);
        when(record.getSshPassword()).thenReturn(sshPassword);
        when(record.getUser()).thenReturn(user);

        when(buildRecordRepository.queryById(eq(buildRecordId))).thenReturn(record);
    }


    private <T> T getSshCredentials(int buildRecordId, int expectedStatus) {
        Response response = endpoint.getSshCredentials(buildRecordId);
        assertThat(response.getStatus()).isEqualTo(expectedStatus);
        T wrappedRecord = (T)response.getEntity();
        return wrappedRecord;
    }
}
