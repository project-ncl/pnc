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
package org.jboss.pnc.mock.dto;

import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.dto.Build;
import org.jboss.pnc.spi.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.spi.dto.BuildEnvironment;
import org.jboss.pnc.spi.dto.ProjectRef;
import org.jboss.pnc.spi.dto.RepositoryConfiguration;
import org.jboss.pnc.spi.dto.User;

import java.util.Collections;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildMock {

    public static Build newBuild(BuildCoordinationStatus status, String buildConfigurationName) {
        return newBuild(1, status, buildConfigurationName);
    }

    public static Build newBuild(Integer id, BuildCoordinationStatus status, String buildConfigurationName) {
        return new Build(
                new ProjectRef(1, "A", "desc", "url1", "url2"),
                new RepositoryConfiguration(1, "url1", "url2", true),
                new BuildEnvironment(1, "jdk8", "desc", "url", "11", Collections.emptyMap(), SystemImageType.DOCKER_IMAGE, true),
                Collections.emptyMap(),
                new User(1, "user"),
                new BuildConfigurationRevisionRef(1, 1, buildConfigurationName, "desc", "true", "awqs21"),
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                id,
                status,
                "build-42",
                true,
                "scmrev-1a2d",
                "final-scm-tag"
        );
    }

}
