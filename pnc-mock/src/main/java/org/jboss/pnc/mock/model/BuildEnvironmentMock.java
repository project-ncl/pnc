/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.model;

import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.model.BuildEnvironment;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildEnvironmentMock {

    public static BuildEnvironment newTest() {
        BuildEnvironment buildEnvironment = new BuildEnvironment();
        buildEnvironment.setId(274593658);
        buildEnvironment.setName("env");
        buildEnvironment.setDescription("the env");
        buildEnvironment.setSystemImageRepositoryUrl("repo");
        buildEnvironment.setSystemImageType(SystemImageType.DOCKER_IMAGE);
        buildEnvironment.setDeprecated(false);

        return buildEnvironment;
    }
}
