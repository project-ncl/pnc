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
 * Group consists of configA,config B and configC. <br/>
 * configC is independent, configB depends on configA. <br/>
 *
 *
 * config1 is an "outside" dependency of configA
 *
 * <p>
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/14/16 Time: 12:09 PM
 * </p>
 */
public class OutsideGroupDependentConfigsTest extends AbstractDependentBuildTest {

    private BuildConfiguration config1;

    private BuildConfiguration configA;
    private BuildConfiguration configB;

    private BuildConfigurationSet configSet;
    private BuildConfiguration configC;

    @Before
    public void initialize() throws DatastoreException, ConfigurationParseException {
        config1 = buildConfig("1");
        configA = buildConfig("A", config1);
        configB = buildConfig("B", configA);
        configC = buildConfig("C");

        configSet = configSet(configA, configB, configC);

        buildConfigurationRepository = spy(new BuildConfigurationRepositoryMock());
        when(buildConfigurationRepository.queryWithPredicates(any()))
                .thenReturn(new ArrayList<>(configSet.getBuildConfigurations()));

        super.initialize();

        saveConfig(config1);
        configSet.getBuildConfigurations().forEach(this::saveConfig);

        insertNewBuildRecords(config1, configA, configB, configC);
        makeResult(configA).dependOn(config1);
    }

    @Test
    public void shouldNotRebuildIfDependencyIsNotRebuilt() throws GraphStructureException {
        Graph<RemoteBuildTask> buildGraph = createGraph(configSet, RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Collection<RemoteBuildTask> nrrBuildTasks = BuildTasksInitializer.removeNRRTasks(buildGraph);

        expectBuiltTask(nrrBuildTasks, configA, configB, configC);
        expectToBuildBuiltTask(buildGraph);
    }

    @Test
    public void shouldRebuildOnlyDependent() throws GraphStructureException {
        insertNewBuildRecords(config1);

        Graph<RemoteBuildTask> buildGraph = createGraph(configSet, RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Collection<RemoteBuildTask> nrrBuildTasks = BuildTasksInitializer.removeNRRTasks(buildGraph);

        expectBuiltTask(nrrBuildTasks, configC);
        expectToBuildBuiltTask(buildGraph, configA, configB);
    }

}