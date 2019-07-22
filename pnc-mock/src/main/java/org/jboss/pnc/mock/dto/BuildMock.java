/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mock.dto;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildMock {

    public static Build newBuild(BuildStatus status, String buildConfigurationName) {
        return newBuild(1, status, buildConfigurationName);
    }

    public static Build newBuild(Integer id, BuildStatus status, String buildConfigurationName) {
        return Build.builder()
                .id(id)
                .status(status)
                .buildContentId("build-42")
                .temporaryBuild(true)
                .project(ProjectMock.newProjectRef())
                .scmRepository(SCMRepositoryMock.newScmRepository())
                .environment(BuildEnvironmentMock.newBuildEnvironment())
                .user(UserMock.newUser())
                .buildConfigRevision(BuildConfigurationRevisionMock.newBuildConfigurationRevisionRef(buildConfigurationName))
                .build();
    }

}
