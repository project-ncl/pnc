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

import lombok.Setter;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mock.datastore.BuildTaskRepositoryMock;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import static org.jboss.pnc.spi.coordinator.CompletionStatus.*;

@ApplicationScoped
@Alternative
public class MockBuildScheduler implements BuildScheduler {

    @Setter(onMethod_ = { @Inject })
    protected BuildTaskRepositoryMock taskRepositoryMock;

    @Setter(onMethod_ = { @Inject })
    protected BuildCoordinator coordinator;

    @Setter
    private boolean keepTasks = false;

    @Override
    public void startBuilding(BuildTask buildTask) throws CoreException {
        if (buildTask.getStatus().isCompleted()) {
            return;
        }

        BuildCoordinationStatus status = BuildCoordinationStatus.DONE;
        String buildScript = buildTask.getBuildConfigurationAudited().getBuildScript();
        if (buildScript != null && !buildScript.isEmpty()) {
            try {
                status = BuildCoordinationStatus.fromBuildStatus(BuildStatus.valueOf(buildScript));
            } catch (IllegalArgumentException e) {
                // assume build that should finish well
            }
        }

        buildTask.setStatus(status);

        taskRepositoryMock.addTask(buildTask);
        if (status.isCompleted()) {
            coordinator.completeBuild(buildTask, mockBuildResult(status));
        }

        if (!keepTasks) {
            taskRepositoryMock.removeTask(buildTask);
        }
    }

    @NotNull
    private static BuildResult mockBuildResult(BuildCoordinationStatus status) {
        BuildResult result;
        switch (status) {
            case REJECTED:
            case REJECTED_FAILED_DEPENDENCIES:
            case DONE_WITH_ERRORS:
                result = AbstractDependentBuildTest.buildResult(FAILED);
                break;
            case REJECTED_ALREADY_BUILT:
                result = AbstractDependentBuildTest.buildResult(NO_REBUILD_REQUIRED);
            case SYSTEM_ERROR:
                result = AbstractDependentBuildTest.buildResult(SYSTEM_ERROR);
                break;
            case CANCELLED:
                result = AbstractDependentBuildTest.buildResult(CANCELLED);
                break;
            case DONE:
            default:
                result = AbstractDependentBuildTest.buildResult();
        }
        return result;
    }

    @Override
    public void startBuilding(BuildSetTask buildSetTask) throws CoreException {
        for (BuildTask buildTask : buildSetTask.getBuildTasks()) {
            startBuilding(buildTask);
        }
    }

    @Override
    public boolean cancel(BuildTask buildTask) throws CoreException {
        return false;
    }
}
