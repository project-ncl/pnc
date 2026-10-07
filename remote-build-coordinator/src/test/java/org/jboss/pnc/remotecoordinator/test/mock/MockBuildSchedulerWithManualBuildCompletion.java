/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;

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
