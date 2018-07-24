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
package org.jboss.pnc.rest.provider;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Before
    public void setUp() {
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
    }

    @Test
    public void shouldGetGraphWithDependencies() {
        //when
        Graph<BuildTask> graph = buildRecordProvider.getRunningBCSetRecordGraph(1);

        //then
        Assertions.assertThat(graph.getVerticies().size()).isEqualTo(3);
        Assertions.assertThat(graph.getVerticies().stream().map(Vertex::getName).collect(Collectors.toList())).containsExactly("1", "2", "3");
    }

    private void mockMethods(BuildSetTask buildSetTask, BuildTask task, int id) {
        when(task.getDependencyGraph()).thenCallRealMethod();
        when(task.getUser()).thenReturn(mock(User.class));
        when(task.getId()).thenReturn(id);
        when(task.getBuildSetTask()).thenReturn(buildSetTask);
    }

    private Set<BuildTask> asSet(BuildTask task) {
        Set<BuildTask> set = new HashSet<>();
        set.add(task);
        return set;
    }

}
