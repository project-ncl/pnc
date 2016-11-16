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
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.coordinator.monitor.PullingMonitor;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.enterprise.event.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/12/16
 * Time: 2:33 PM
 */
public class DefaultBuildCoordinatorTest {
    @Mock
    private Datastore datastore;
    @Mock
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;
    @Mock
    private BuildSchedulerFactory buildSchedulerFactory;
    @Mock
    private BuildQueue buildQueue;
    @Mock
    private Configuration configuration;
    @Mock
    private Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier;

    @Mock
    private PullingMonitor monitor;

    @InjectMocks
    private DatastoreAdapter datastoreAdapter = new DatastoreAdapter();

    private BuildCoordinator coordinator;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        coordinator = new DefaultBuildCoordinator(
                datastoreAdapter,
                buildStatusChangedEventNotifier,
                buildSetStatusChangedEventNotifier,
                buildSchedulerFactory,
                buildQueue,
                configuration,
                monitor);
    }

    @Test
    public void shouldStoreSshCredentialsOnSshEnabled() throws DatastoreException {
        BuildTask buildTask = mockBuildTask();
        BuildResult buildResult = mockBuildResult(true);

        SshCredentials sshCredentials = new SshCredentials();
        sshCredentials.setCommand(RandomStringUtils.randomAlphabetic(30));
        sshCredentials.setPassword(RandomStringUtils.randomAlphabetic(30));
        when(buildResult.getSshCredentials()).thenReturn(Optional.of(sshCredentials));
        when(buildResult.getExecutionRootName()).thenReturn(Optional.empty());
        when(buildResult.getExecutionRootVersion()).thenReturn(Optional.empty());

        ArgumentGrabbingAnswer<BuildRecord.Builder> answer = new ArgumentGrabbingAnswer<>(BuildRecord.Builder.class);
        when(datastore.storeCompletedBuild(any(BuildRecord.Builder.class))).thenAnswer(answer);

        coordinator.updateBuildStatus(buildTask, buildResult);

        assertThat(answer.arguments).hasSize(1);
        BuildRecord.Builder builder = answer.arguments.iterator().next();
        BuildRecord record = builder.build();
        assertThat(record.getSshCommand()).isEqualTo(sshCredentials.getCommand());
        assertThat(record.getSshPassword()).isEqualTo(sshCredentials.getPassword());
    }

    private BuildResult mockBuildResult(boolean withSshCredentials) {
        BuildResult result = mock(BuildResult.class);
        BuildDriverResult driverResult = mock(BuildDriverResult.class);
        when(driverResult.getBuildStatus()).thenReturn(BuildStatus.FAILED);
        when(result.getBuildDriverResult()).thenReturn(Optional.of(driverResult));
        RepositoryManagerResult repoManagerResult = mock(RepositoryManagerResult.class);
        when(repoManagerResult.getStatus()).thenReturn(RepositoryManagerStatus.SUCCESS);
        when(result.getRepositoryManagerResult()).thenReturn(Optional.of(repoManagerResult));

        when(result.getBuildExecutionConfiguration()).thenReturn(Optional.of(mock(BuildExecutionConfiguration.class)));
        return result;
    }

    private BuildTask mockBuildTask() {
        BuildTask task = mock(BuildTask.class);
        BuildConfigurationAudited config = mock(BuildConfigurationAudited.class);
        when(config.getId()).thenReturn(new IdRev(12, 13));
        when(task.getBuildConfigurationAudited()).thenReturn(config);
        when(task.getStatus()).thenReturn(BuildCoordinationStatus.DONE);
        return task;
    }

    private static class ArgumentGrabbingAnswer<T> implements Answer<T> {
        private final Class<T> argumentType;
        private final List<T> arguments = new ArrayList<T>();

        private ArgumentGrabbingAnswer(Class<T> argumentType) {
            this.argumentType = argumentType;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            arguments.add(invocation.getArgumentAt(0, argumentType));
            return null;
        }
    }

}
