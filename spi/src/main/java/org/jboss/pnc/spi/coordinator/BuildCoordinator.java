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
package org.jboss.pnc.spi.coordinator;

import org.jboss.pnc.common.mdc.MDCMeta;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;

import java.util.List;
import java.util.Optional;

public interface BuildCoordinator {

    BuildSetTask build(BuildConfiguration buildConfiguration,
                       User user, BuildOptions buildOptions) throws BuildConflictException, CoreException;


    BuildSetTask build(BuildConfigurationSet buildConfigurationSet, User user, BuildOptions buildOptions) throws CoreException;

    List<BuildTask> getSubmittedBuildTasks();

    void completeBuild(BuildTask buildTask, BuildResult buildResult);

    boolean cancel(int buildTaskId) throws CoreException;

    boolean cancelSet(int buildSetTaskId) throws CoreException;

    void updateBuildTaskStatus(BuildTask task, BuildCoordinationStatus status);

    void start();

    Optional<MDCMeta> getMDCMeta(Integer buildTaskId);

}
