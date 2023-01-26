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
package org.jboss.pnc.remotecoordinator.test.mock;

import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MockBuildSchedulerWithManualBuildCompletion extends MockBuildScheduler {

    Map<String, Consumer<BuildResult>> scheduledTasks = new HashMap<>();

    public void startBuilding(BuildTask buildTask) throws CoreException {
        // TODO taskRepositoryMock.addTask(buildTask);
        // Consumer<BuildResult> onComplete = (buildResult -> {
        // coordinator.completeBuild(buildTask, buildResult);
        // });
        // scheduledTasks.put(buildTask.getId(), onComplete);
    }

    public void completeBuild(String taskId) {
        BuildResult result = MockBuildScheduler.buildResult();
        Consumer<BuildResult> buildResultConsumer = scheduledTasks.get(taskId);

        if (buildResultConsumer == null) {
            throw new RuntimeException("Cannot complete the build. Task with id: " + taskId + " does not exist.");
        }
        taskRepositoryMock.removeTask(taskRepositoryMock.getTask(taskId));
        scheduledTasks.remove(taskId);
        buildResultConsumer.accept(result);
    }

    public boolean isBuilding(Integer configurationId) {
        // Optional<BuildTask> buildTask = taskRepositoryMock.getAll()
        // .stream()
        // .filter(task -> task.getBuildConfigurationAudited().getId().equals(configurationId))
        // .findFirst();
        // if (buildTask.isEmpty()) {
        // return false;
        // }
        // return buildTask.get().getStatus().equals(BuildCoordinationStatus.BUILDING);
        // TODO
        return false;
    }
}
