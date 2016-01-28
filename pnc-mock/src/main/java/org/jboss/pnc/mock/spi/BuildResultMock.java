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

package org.jboss.pnc.mock.spi;

import org.jboss.pnc.mock.builddriver.BuildDriverResultMock;
import org.jboss.pnc.mock.executor.BuildExecutionConfigurationMock;
import org.jboss.pnc.mock.repositorymanager.RepositoryManagerResultMock;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildResultMock {

    public static BuildResult mock(BuildDriverStatus status) {
        BuildExecutionConfiguration buildExecutionConfig = BuildExecutionConfigurationMock.mockConfig();
        BuildDriverResult buildDriverResult = BuildDriverResultMock.mockResult(status);
        RepositoryManagerResult repositoryManagerResult = RepositoryManagerResultMock.mockResult();
        ExecutorException exception = new ExecutorException("Test exception.", new Exception("Test exception cause."));
        BuildExecutionStatus buildExecutionStatus;
        if (status.completedSuccessfully()) {
            buildExecutionStatus = null;
        } else {
            buildExecutionStatus = BuildExecutionStatus.DONE_WITH_ERRORS;
        }

        return new BuildResult(
                Optional.ofNullable(buildExecutionConfig),
                Optional.ofNullable(buildDriverResult),
                Optional.ofNullable(repositoryManagerResult),
                Optional.ofNullable(exception),
                Optional.ofNullable(buildExecutionStatus));

    }
}
