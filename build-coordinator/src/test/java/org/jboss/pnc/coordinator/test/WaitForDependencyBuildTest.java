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
package org.jboss.pnc.coordinator.test;

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.coordinator.builder.BuildSchedulerFactory;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.jboss.pnc.test.util.Wait;
import org.junit.Before;
import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class WaitForDependencyBuildTest extends AbstractDependentBuildTest {

    private BuildConfiguration configParent;
    private BuildConfiguration configDependency;

    private MockBuildSchedulerWithManualBuildCompletion buildScheduler = new MockBuildSchedulerWithManualBuildCompletion();

    @Before
    public void initialize() throws DatastoreException, ConfigurationParseException {
        buildSchedulerFactory = new MockBuildSchedulerFactory();
        configDependency = buildConfig("Dependency");
        configParent = buildConfig("Parent", configDependency);

        super.initialize();

        saveConfig(configDependency);
        saveConfig(configParent);
    }

    @Test
    public void shouldNotStartParentBuildWhenDependencyIsRunning()
            throws CoreException, TimeoutException, InterruptedException {

        // start dependency
        build(configDependency);
        Wait.forCondition(() -> buildScheduler.isBuilding(configDependency.getId()), 3, ChronoUnit.SECONDS);

        // start parent while dependency is running
        build(configParent);

        // parent should wait for dependency to complete
        BuildTask parentBuildTask = getSubmittedBuildTaskByConfigurationId(configParent.getId()).get();
        assertThat(parentBuildTask.getStatus()).isEqualTo(BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES);

        // complete the dependency
        BuildTask dependencyBuildTask = getScheduledBuildTaskByConfigurationId(configDependency.getId()).get();
        buildScheduler.completeBuild(dependencyBuildTask.getId());

        // check if parent has started
        Wait.forCondition(
                () -> buildScheduler.isBuilding(parentBuildTask.getBuildConfigurationAudited().getId()),
                3,
                ChronoUnit.SECONDS);

        buildScheduler.completeBuild(parentBuildTask.getId());

        waitForEmptyBuildQueue();
        List<BuildConfiguration> configsWithTasks = getBuiltConfigs();
        assertThat(configsWithTasks).isEmpty();
    }

    private Optional<BuildTask> getSubmittedBuildTaskByConfigurationId(Integer buildConfigurationId)
            throws RemoteRequestException {
        return coordinator.getSubmittedBuildTasks()
                .stream()
                .filter(bt -> bt.getBuildConfigurationAudited().getId().equals(buildConfigurationId))
                .findAny();
    }

    private class MockBuildSchedulerFactory extends BuildSchedulerFactory {
        @Override
        public BuildScheduler getBuildScheduler() {
            return buildScheduler;
        }
    }

    private class MockBuildSchedulerWithManualBuildCompletion implements BuildScheduler {

        Map<String, Consumer<BuildResult>> scheduledTasks = new HashMap();

        @Override
        public void startBuilding(BuildTask buildTask) throws CoreException {
            builtTasks.add(buildTask);
            Consumer<BuildResult> onComplete = (buildResult -> {
                coordinator.completeBuild(buildTask, buildResult);
            });
            scheduledTasks.put(buildTask.getId(), onComplete);
        }

        @Override
        public void startBuilding(BuildSetTask buildSetTask) throws CoreException {
            throw new UnsupportedOperationException("Only to be used with remote build scheduler.");
        }

        public void completeBuild(String taskId) {
            BuildResult result = buildResult();
            Consumer<BuildResult> buildResultConsumer = scheduledTasks.get(taskId);

            if (buildResultConsumer == null) {
                throw new RuntimeException("Cannot complete the build. Task with id: " + taskId + " does not exist.");
            }
            builtTasks.remove(getBuildTaskById(taskId));
            scheduledTasks.remove(taskId);
            buildResultConsumer.accept(result);
        }

        public boolean isBuilding(Integer configurationId) {
            Optional<BuildTask> buildTask = getScheduledBuildTaskByConfigurationId(configurationId);
            if (!buildTask.isPresent()) {
                return false;
            }
            return buildTask.get().getStatus().equals(BuildCoordinationStatus.BUILDING);
        }

        @Override
        public boolean cancel(BuildTask buildTask) throws CoreException {
            return false;
        }
    }

}