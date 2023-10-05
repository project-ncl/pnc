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
package org.jboss.pnc.spi.coordinator;

import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.BuildRequestException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BuildCoordinator {

    BuildSetTask buildConfig(BuildConfiguration buildConfiguration, User user, BuildOptions buildOptions)
            throws CoreException, BuildRequestException, BuildConflictException;

    BuildSetTask buildConfigurationAudited(
            BuildConfigurationAudited buildConfiguration,
            User user,
            BuildOptions buildOptions) throws CoreException, BuildRequestException, BuildConflictException;

    @Deprecated // used only in the tests
    BuildSetTask buildSet(BuildConfigurationSet buildConfigurationSet, User user, BuildOptions buildOptions)
            throws CoreException;

    BuildSetTask buildSet(
            BuildConfigurationSet buildConfigurationSet,
            Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap,
            User user,
            BuildOptions buildOptions) throws CoreException, BuildRequestException, BuildConflictException;

    Optional<BuildTask> getSubmittedBuildTask(String buildId) throws RemoteRequestException, MissingDataException;

    /**
     * List all waiting, ready and in progress tasks
     *
     * @return list of all build tasks in the queue
     */
    List<BuildTask> getSubmittedBuildTasks() throws RemoteRequestException, MissingDataException;

    @Deprecated // get rid of BuildTask
    List<BuildTask> getSubmittedBuildTasksBySetId(long buildConfigSetRecordId)
            throws RemoteRequestException, MissingDataException;

    void completeBuild(BuildTask buildTask, BuildResult buildResult);

    void completeBuild(
            BuildTaskRef buildTask,
            Optional<BuildResult> buildResult,
            BuildCoordinationStatus previousState);

    /**
     * Cancels a running build
     *
     * @param buildTaskId ID of a running build
     * @return True if the cancel request is successfully accepted, false if if there is no running build with such ID
     * @throws CoreException Thrown if cancellation fails due to any internal error
     */
    boolean cancel(String buildTaskId) throws CoreException;

    boolean cancelSet(long buildConfigSetRecordId) throws CoreException;

    @Deprecated // used only internally
    void updateBuildTaskStatus(BuildTask task, BuildCoordinationStatus newState);

    void updateBuildTaskStatus(
            BuildTaskRef task,
            BuildCoordinationStatus previousState,
            BuildCoordinationStatus newState);

    void storeAndNotifyBuildConfigSetRecord(BuildConfigSetRecord setRecord, BuildStatus newState, String description)
            throws CoreException;

    void start();

    @Deprecated
    Optional<BuildTaskContext> getMDCMeta(String buildTaskId);
}
