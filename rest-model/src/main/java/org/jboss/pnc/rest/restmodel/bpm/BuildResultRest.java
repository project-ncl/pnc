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

package org.jboss.pnc.rest.restmodel.bpm;

import org.jboss.pnc.rest.restmodel.BuildDriverResultRest;
import org.jboss.pnc.rest.restmodel.BuildExecutionConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryManagerResultRest;
import org.jboss.pnc.rest.utils.JsonOutputConverterMapper;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@XmlRootElement(name = "buildResult")
public class BuildResultRest extends BpmNotificationRest implements Serializable {

    private BuildExecutionConfigurationRest buildExecutionConfiguration;

    private BuildDriverResultRest buildDriverResult;

    private RepositoryManagerResultRest repositoryManagerResult;

    private ExecutorException exception;

    private BuildExecutionStatus failedReasonStatus;

    public BuildResultRest() {
    }

    public BuildResultRest(String serialized) throws IOException {
        BuildResultRest buildResultRest = JsonOutputConverterMapper.readValue(serialized, BuildResultRest.class);
        this.buildExecutionConfiguration = buildResultRest.getBuildExecutionConfiguration();
        this.buildDriverResult = buildResultRest.getBuildDriverResult();
        this.repositoryManagerResult = buildResultRest.getRepositoryManagerResult();
        this.exception = buildResultRest.getException();
        this.failedReasonStatus = buildResultRest.getFailedReasonStatus();
    }

    public BuildResultRest(BuildResult buildResult) {

        buildResult.getBuildExecutionConfiguration().ifPresent((configuration) -> {
            buildExecutionConfiguration = new BuildExecutionConfigurationRest(configuration);
        });

        if (buildResult.getBuildDriverResult().isPresent()) {
            BuildDriverResult result = buildResult.getBuildDriverResult().get();
            buildDriverResult = new BuildDriverResultRest(result);
        }

        buildResult.getRepositoryManagerResult().ifPresent((result) -> {
            repositoryManagerResult = new RepositoryManagerResultRest(result);
        });

        buildResult.getFailedReasonStatus().ifPresent(result -> failedReasonStatus = result);

        exception = buildResult.getException().orElse(null);
    }

    public BuildResult toBuildResult() {
        RepositoryManagerResult repositoryManagerResult = null;
        if (getRepositoryManagerResult() != null) {
            repositoryManagerResult = getRepositoryManagerResult().toRepositoryManagerResult();
        }
        return new BuildResult(
                Optional.ofNullable(buildExecutionConfiguration),
                Optional.ofNullable(buildDriverResult),
                Optional.ofNullable(repositoryManagerResult),
                Optional.ofNullable(exception),
                Optional.ofNullable(failedReasonStatus));
    }

    public void setBuildExecutionConfiguration(BuildExecutionConfigurationRest buildExecutionConfiguration) {
        this.buildExecutionConfiguration = buildExecutionConfiguration;
    }

    public BuildExecutionConfigurationRest getBuildExecutionConfiguration() {
        return buildExecutionConfiguration;
    }

    public BuildDriverResultRest getBuildDriverResult() {
        return buildDriverResult;
    }

    public void setBuildDriverResult(BuildDriverResultRest buildDriverResult) {
        this.buildDriverResult = buildDriverResult;
    }

    public RepositoryManagerResultRest getRepositoryManagerResult() {
        return repositoryManagerResult;
    }

    public void setRepositoryManagerResult(RepositoryManagerResultRest repositoryManagerResult) {
        this.repositoryManagerResult = repositoryManagerResult;
    }

    public ExecutorException getException() {
        return exception;
    }

    public void setException(ExecutorException exception) {
        this.exception = exception;
    }

    public BuildExecutionStatus getFailedReasonStatus() {
        return failedReasonStatus;
    }

    public void setFailedReasonStatus(BuildExecutionStatus failedReasonStatus) {
        this.failedReasonStatus = failedReasonStatus;
    }

    @Override
    public String getEventType() {
        return "BUILD_COMPLETE";
    }

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }
}
