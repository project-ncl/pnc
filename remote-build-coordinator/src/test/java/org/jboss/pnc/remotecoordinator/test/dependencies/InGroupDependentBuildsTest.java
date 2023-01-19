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
package org.jboss.pnc.remotecoordinator.test.dependencies;

import org.jboss.pnc.common.graph.GraphStructureException;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.mock.repository.BuildConfigurationRepositoryMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.remotecoordinator.builder.BuildTasksInitializer;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.util.graph.Graph;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * configC depends on configB, which in turn depends on configA. configD depends on configA and configB configE doesn't
 * have dependencies
 *
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/14/16 Time: 12:09 PM
 */
public class InGroupDependentBuildsTest extends AbstractDependentBuildTest {

    private BuildConfiguration configA;

    private BuildConfiguration configB;
    private BuildConfiguration configC;
    private BuildConfiguration configD;
    private BuildConfiguration configE;

    private BuildConfigurationSet configSet;

    @Before
    public void initialize() throws DatastoreException, ConfigurationParseException {
        configA = buildConfig("A");

        configB = buildConfig("B", configA);
        configC = buildConfig("C", configB);
        configD = buildConfig("D", configA, configB);
        configE = buildConfig("E");

        configSet = configSet(configA, configB, configC, configD, configE);

        buildConfigurationRepository = spy(new BuildConfigurationRepositoryMock());
        when(buildConfigurationRepository.queryWithPredicates(any()))
                .thenReturn(new ArrayList<>(configSet.getBuildConfigurations()));

        super.initialize();

        configSet.getBuildConfigurations().forEach(this::saveConfig);
    }

    @Test
    public void shouldBuildAllIfNotSuccessfullyBuilt() throws GraphStructureException {
        Graph<RemoteBuildTask> buildGraph = createGraph(configSet, RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        expectToBuildBuiltTask(buildGraph, configA, configB, configC, configD, configE);
    }

    @Test
    public void shouldNotCreateTaskForNonDependentBuilt() throws GraphStructureException {
        insertNewBuildRecords(configE);

        Graph<RemoteBuildTask> buildGraph = createGraph(configSet, RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Collection<RemoteBuildTask> nrrBuildTasks = BuildTasksInitializer.removeNRRTasks(buildGraph);

        expectBuiltTask(nrrBuildTasks, configE);
        expectToBuildBuiltTask(buildGraph, configA, configB, configC, configD);
    }

    @Test
    public void shouldCreateTaskForNonDependentBuiltWithRebuildAll() throws GraphStructureException {
        insertNewBuildRecords(configE);
        Graph<RemoteBuildTask> buildGraph = createGraph(configSet, RebuildMode.FORCE);

        expectToBuildBuiltTask(buildGraph, configA, configB, configC, configD, configE);
    }

    @Test
    public void shouldCreateTaskForDependentBuilt() throws GraphStructureException {
        insertNewBuildRecords(configA, configC, configD, configE);
        Graph<RemoteBuildTask> buildGraph = createGraph(configSet, RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Collection<RemoteBuildTask> nrrBuildTasks = BuildTasksInitializer.removeNRRTasks(buildGraph);

        expectBuiltTask(nrrBuildTasks, configA, configE);
        expectToBuildBuiltTask(buildGraph, configB, configC, configD);
    }

    @Test
    public void shouldBuildOnlyCWhenOnlyCIsUpdated() throws GraphStructureException {
        insertNewBuildRecords(configA, configB, configC, configD, configE);

        updateConfiguration(configC);

        Graph<RemoteBuildTask> buildGraph = createGraph(configSet, RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        Collection<RemoteBuildTask> nrrBuildTasks = BuildTasksInitializer.removeNRRTasks(buildGraph);

        expectBuiltTask(nrrBuildTasks, configA, configB, configD, configE);
        expectToBuildBuiltTask(buildGraph, configC);
    }

    @Test
    public void shouldBuildBCDWhenBIsUpdated() throws GraphStructureException {
        insertNewBuildRecords(configA, configB, configC, configD, configE);

        updateConfiguration(configB);

        Graph<RemoteBuildTask> buildGraph = createGraph(configSet, RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        Collection<RemoteBuildTask> nrrBuildTasks = BuildTasksInitializer.removeNRRTasks(buildGraph);

        expectBuiltTask(nrrBuildTasks, configA, configE);
        expectToBuildBuiltTask(buildGraph, configB, configC, configD);
    }
}