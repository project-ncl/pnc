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
package org.jboss.pnc.spi;

import lombok.Getter;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import java.util.Optional;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-02.
 */
public class BuildResult {

    @Getter
    private final Optional<BuildExecutionConfiguration> buildExecutionConfiguration;

    @Getter
    private final Optional<BuildDriverResult> buildDriverResult;

    /**
     * Note that RepositoryManagerResult can return nul if build was not successful completed.
     */
    @Getter
    private final Optional<RepositoryManagerResult> repositoryManagerResult;

    private final Optional<ExecutorException> executorException;

    @Getter
    private final Optional<BuildExecutionStatus> failedReasonStatus;

    @Getter
    private final Optional<SshCredentials> sshCredentials;

    @Getter
    private final Optional<String> executionRootName;

    @Getter
    private final Optional<String> executionRootVersion;

    public BuildResult(Optional<BuildExecutionConfiguration> generatedBuildConfig,
                       Optional<BuildDriverResult> buildDriverResult,
                       Optional<RepositoryManagerResult> repositoryManagerResult,
                       Optional<ExecutorException> executorException,
                       Optional<BuildExecutionStatus> failedReasonStatus,
                       Optional<SshCredentials> sshCredentials,
                       Optional<String> executionRootName,
                       Optional<String> executionRootVersion) {
        this.buildExecutionConfiguration = generatedBuildConfig;
        this.buildDriverResult = buildDriverResult;
        this.repositoryManagerResult = repositoryManagerResult;
        this.executorException = executorException;
        this.failedReasonStatus = failedReasonStatus;
        this.sshCredentials = sshCredentials;
        this.executionRootName = executionRootName;
        this.executionRootVersion = executionRootVersion;
    }

    public Optional<ExecutorException> getException() {
        return executorException;
    }

    public boolean hasFailed() {
        return executorException.isPresent() || failedReasonStatus.isPresent();
    }

}
