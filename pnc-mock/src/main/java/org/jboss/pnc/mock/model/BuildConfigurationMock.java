/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.model;

import java.util.Date;

import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildConfigurationMock {

    public static BuildConfiguration createNew(
            Integer id,
            Project project,
            BuildEnvironment buildEnvironment,
            RepositoryConfiguration repositoryConfiguration) {
        BuildConfiguration.Builder builder = BuildConfiguration.Builder.newBuilder()
                .name(id != null ? id.toString() : "no-name-" + RandomUtils.randString(5))
                .creationTime(new Date())
                .project(project)
                .buildEnvironment(buildEnvironment)
                .repositoryConfiguration(repositoryConfiguration)
                .buildType(BuildType.MVN);

        if (id != null) {
            builder.id(id);
        }
        return builder.build();
    }
}
