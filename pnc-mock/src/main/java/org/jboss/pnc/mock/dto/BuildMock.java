/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
                .id(id.toString())
                .status(status)
                .buildContentId("build-42")
                .temporaryBuild(true)
                .project(ProjectMock.newProjectRef())
                .scmRepository(SCMRepositoryMock.newScmRepository())
                .environment(BuildEnvironmentMock.newBuildEnvironment())
                .user(UserMock.newUser())
                .buildConfigRevision(
                        BuildConfigurationRevisionMock.newBuildConfigurationRevisionRef(buildConfigurationName))
                .build();
    }

}
