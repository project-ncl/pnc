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

import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.GeneratedBuildConfig;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import java.util.Optional;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-02.
 */
public class BuildResult {

    private final Optional<GeneratedBuildConfig> generatedBuildConfig;
    private final Optional<BuildDriverResult> buildDriverResult;
    private final Optional<RepositoryManagerResult> repositoryManagerResult;
    private final Optional<ExecutorException> executorException;
    private final Optional<BuildExecutionStatus> failedReasonStatus;

    public BuildResult(
                       Optional<GeneratedBuildConfig> generatedBuildConfig,
                       Optional<BuildDriverResult> buildDriverResult,
                       Optional<RepositoryManagerResult> repositoryManagerResult,
                       Optional<ExecutorException> executorException,
                       Optional<BuildExecutionStatus> failedReasonStatus) {
        this.generatedBuildConfig = generatedBuildConfig;
        this.buildDriverResult = buildDriverResult;
        this.repositoryManagerResult = repositoryManagerResult;
        this.executorException = executorException;
        this.failedReasonStatus = failedReasonStatus;
    }

    public Optional<GeneratedBuildConfig> getGeneratedBuildConfig() {
        return generatedBuildConfig;
    }

    public Optional<BuildDriverResult> getBuildDriverResult() {
        return buildDriverResult;
    }

    /**
     * @return Note that RepositoryManagerResult can return nul if build was not successful completed.
     */
    public Optional<RepositoryManagerResult> getRepositoryManagerResult() {
        return repositoryManagerResult;
    }

    public boolean hasFailed() {
        return executorException.isPresent() || failedReasonStatus.isPresent();
    }

    public Optional<ExecutorException> getException() {
        return executorException;
    }

    public Optional<BuildExecutionStatus> getFailedReasonStatus() {
        return failedReasonStatus;
    }
}
