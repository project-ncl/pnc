/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.datastore.repositories.BuildRecordRepositoryImpl;
import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.executor.DefaultBuildExecutionConfiguration;
import org.jboss.pnc.mock.executor.BuildExecutionSessionMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.GraphWithMetadata;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildRecordProviderTest {

    @Mock
    private BuildCoordinator buildCoordinator;

    @InjectMocks
    private BuildRecordProvider buildRecordProvider;

    @Mock
    private BuildRecordRepositoryImpl buildRecordRepository;

    @Mock
    private BuildExecutor buildExecutor;

    @Mock
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Test
    public void shouldGetGraphWithDependencies() {
        //given
        MockitoAnnotations.initMocks(this);
        List<BuildTask> submittedTasks = new ArrayList<>();

        BuildSetTask buildSetTask = Mockito.mock(BuildSetTask.class);
        when(buildSetTask.getId()).thenReturn(1);

        BuildTask task1 = Mockito.mock(BuildTask.class);
        BuildTask taskDependency = Mockito.mock(BuildTask.class);
        BuildTask taskDependencyDependency = Mockito.mock(BuildTask.class);

        mockMethods(buildSetTask, task1, 1);
        when(task1.getDependencies()).thenReturn(asSet(taskDependency));

        mockMethods(buildSetTask, taskDependency, 2);
        when(taskDependency.getDependants()).thenReturn(asSet(task1));

        mockMethods(buildSetTask, taskDependencyDependency, 3);
        when(taskDependency.getDependencies()).thenReturn(asSet(taskDependencyDependency));
        when(taskDependencyDependency.getDependants()).thenReturn(asSet(taskDependency));

        submittedTasks.add(task1);
        submittedTasks.add(taskDependency);
        submittedTasks.add(taskDependencyDependency);

        when(buildCoordinator.getSubmittedBuildTasks()).thenReturn(submittedTasks);

        //when
        Graph<BuildTask> graph = buildRecordProvider.getRunningBCSetRecordGraph(1);

        //then
        Assertions.assertThat(graph.getVerticies().size()).isEqualTo(3);
        Assertions.assertThat(graph.getVerticies().stream().map(Vertex::getName).collect(Collectors.toList())).containsExactly("1", "2", "3");
    }

    @Test
    public void graphShouldContainRunningDependent() {
        //given
        MockitoAnnotations.initMocks(this);
        List<BuildTask> submittedTasks = new ArrayList<>();

        BuildTask runningTask = Mockito.mock(BuildTask.class); //id = 1
        BuildTask runningDependency = Mockito.mock(BuildTask.class); //id = 2
        BuildRecord completedDependencyDependency = Mockito.mock(BuildRecord.class); //id = 3

        when(runningTask.getSubmitTime()).thenReturn(new Date());
        mockMethods(null, runningTask, 1);
        when(runningTask.getDependencies()).thenReturn(asSet(runningDependency));

        mockMethods(null, runningDependency, 2);
        when(runningDependency.getDependants()).thenReturn(asSet(runningTask));

        when(completedDependencyDependency.getId()).thenReturn(3);
        when(completedDependencyDependency.getDependentBuildRecordIds()).thenReturn(new Integer[]{2});
        when(completedDependencyDependency.getDependencyBuildRecordIds()).thenReturn(new Integer[]{});


        BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder().build();
        BuildConfigurationAudited buildConfigurationAudited = BuildConfigurationAudited.Builder.newBuilder()
                .buildConfiguration(buildConfiguration)
                .build();

        when(buildConfigurationAuditedRepository.queryById(any(IdRev.class))).thenReturn(buildConfigurationAudited);

        when(buildRecordRepository.findByIdFetchProperties(3)).thenReturn(completedDependencyDependency);
        when(buildRecordRepository.queryById(3)).thenReturn(completedDependencyDependency);
        when(buildRecordRepository.getDependencyGraph(anyInt())).thenCallRealMethod();

        submittedTasks.add(runningTask);
        submittedTasks.add(runningDependency);
        when(buildCoordinator.getSubmittedBuildTasks()).thenReturn(submittedTasks);

        //bad dependency, it should be refactored
        BuildExecutionConfiguration buildExecutionConfiguration = new DefaultBuildExecutionConfiguration(
                2,
                "",
                null, null, null, null, null, null, null,
                true, null, null, null, null,
                false, null, null, true, null
        );
        BuildExecutionSessionMock executionSession = new BuildExecutionSessionMock(buildExecutionConfiguration, (v) -> {});
        executionSession.setStatus(BuildExecutionStatus.REPO_SETTING_UP, false);
        when(buildExecutor.getRunningExecution(anyInt())).thenReturn(executionSession);

        //when
        GraphWithMetadata<BuildRecordRest, Integer> graph = buildRecordProvider.getDependencyGraph(3);

        //then
        Assertions.assertThat(graph.getGraph().getVerticies().size()).isEqualTo(3);
        Assertions.assertThat(graph.getGraph().getVerticies().stream().map(Vertex::getName).collect(Collectors.toList())).containsExactlyInAnyOrder("1", "2", "3");
        Assertions.assertThat(graph.getMissingNodeIds().isEmpty());

        Assertions.assertThat(graph.getGraph().getEdges().size()).isEqualTo(2);
    }

    private void mockMethods(BuildSetTask buildSetTask, BuildTask task, int id) {
        when(task.getUser()).thenReturn(mock(User.class));
        when(task.getId()).thenReturn(id);
        when(task.getBuildSetTask()).thenReturn(buildSetTask);

        BuildConfigurationAudited bca = Mockito.mock(BuildConfigurationAudited.class);
        when(bca.getIdRev()).thenReturn(new IdRev(0,0));
        when(task.getBuildConfigurationAudited()).thenReturn(bca);
    }

    private Set<BuildTask> asSet(BuildTask task) {
        Set<BuildTask> set = new HashSet<>();
        set.add(task);
        return set;
    }

}
