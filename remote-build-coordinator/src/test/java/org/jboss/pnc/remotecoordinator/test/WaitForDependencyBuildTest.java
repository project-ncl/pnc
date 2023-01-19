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

import org.junit.Ignore;

@Ignore // TODO if needed move to integration tests
public class WaitForDependencyBuildTest {
//extends AbstractDependentBuildTest
//    private BuildConfiguration configParent;
//    private BuildConfiguration configDependency;
//
//    private MockBuildSchedulerWithManualBuildCompletion buildScheduler = new MockBuildSchedulerWithManualBuildCompletion();
//
//    @Before
//    public void initialize() throws DatastoreException, ConfigurationParseException {
//        configDependency = buildConfig("Dependency");
//        configParent = buildConfig("Parent", configDependency);
//
//        super.initialize();
//
//        saveConfig(configDependency);
//        saveConfig(configParent);
//    }
//
//    @Test
//    public void shouldNotStartParentBuildWhenDependencyIsRunning()
//            throws CoreException, TimeoutException, InterruptedException {
//
//        // start dependency
//        build(configDependency);
//        Wait.forCondition(() -> buildScheduler.isBuilding(configDependency.getId()), 3, ChronoUnit.SECONDS);
//
//        // start parent while dependency is running
//        build(configParent);
//
//        // parent should wait for dependency to complete
//        BuildTask parentBuildTask = getSubmittedBuildTaskByConfigurationId(configParent.getId()).get();
//        assertThat(parentBuildTask.getStatus()).isEqualTo(BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES);
//
//        // complete the dependency
//        BuildTask dependencyBuildTask = getScheduledBuildTaskByConfigurationId(configDependency.getId()).get();
//        buildScheduler.completeBuild(dependencyBuildTask.getId());
//
//        // check if parent has started
//        Wait.forCondition(
//                () -> buildScheduler.isBuilding(parentBuildTask.getBuildConfigurationAudited().getId()),
//                3,
//                ChronoUnit.SECONDS);
//
//        buildScheduler.completeBuild(parentBuildTask.getId());
//
//        List<BuildConfiguration> configsWithTasks = getBuiltConfigs();
//        assertThat(configsWithTasks).isEmpty();
//    }
//
//    private Optional<BuildTask> getSubmittedBuildTaskByConfigurationId(Integer buildConfigurationId) {
//        return coordinator.getSubmittedBuildTasks()
//                .stream()
//                .filter(bt -> bt.getBuildConfigurationAudited().getId().equals(buildConfigurationId))
//                .findAny();
//    }

}