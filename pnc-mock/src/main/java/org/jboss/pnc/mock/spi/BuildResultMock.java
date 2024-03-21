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

package org.jboss.pnc.mock.spi;

import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildResultMock {

    public static BuildResult mock(BuildStatus status) {
        BuildExecutionConfiguration buildExecutionConfig = BuildExecutionConfigurationMock.mockConfig();
        BuildDriverResult buildDriverResult = BuildDriverResultMock.mockResult(status);
        RepositoryManagerResult repositoryManagerResult = RepositoryManagerResultMock.mockResult();

        CompletionStatus completionStatus;
        if (status.completedSuccessfully()) {
            completionStatus = CompletionStatus.SUCCESS;
        } else {
            completionStatus = CompletionStatus.FAILED;
        }

        return new BuildResult(
                completionStatus,
                Optional.of(new ProcessException("Test Exception.")),
                Optional.ofNullable(buildExecutionConfig),
                Optional.ofNullable(buildDriverResult),
                Optional.ofNullable(repositoryManagerResult),
                Optional.of(EnvironmentDriverResultMock.mock()),
                Optional.of(RepourResultMock.mock()));
    }
}
