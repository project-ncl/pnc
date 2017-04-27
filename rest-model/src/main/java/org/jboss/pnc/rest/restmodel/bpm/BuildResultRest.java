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

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.rest.restmodel.BuildDriverResultRest;
import org.jboss.pnc.rest.restmodel.BuildExecutionConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryManagerResultRest;
import org.jboss.pnc.rest.utils.JsonOutputConverterMapper;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repour.RepourResult;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@XmlRootElement(name = "buildResult")
public class BuildResultRest extends BpmNotificationRest implements Serializable {

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private final CompletionStatus completionStatus;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private final ProcessException processException;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private final BuildExecutionConfigurationRest buildExecutionConfiguration;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private final BuildDriverResultRest buildDriverResult;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private final RepositoryManagerResultRest repositoryManagerResult;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private final EnvironmentDriverResult environmentDriverResult;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private final RepourResult repourResult;


    public BuildResultRest(String serialized) throws IOException {
        BuildResultRest buildResultRest = JsonOutputConverterMapper.readValue(serialized, BuildResultRest.class);

        this.completionStatus = buildResultRest.getCompletionStatus();
        this.processException = buildResultRest.getProcessException();
        this.buildExecutionConfiguration = buildResultRest.getBuildExecutionConfiguration();
        this.buildDriverResult = buildResultRest.getBuildDriverResult();
        this.repositoryManagerResult = buildResultRest.getRepositoryManagerResult();
        this.environmentDriverResult = buildResultRest.getEnvironmentDriverResult();
        this.repourResult = buildResultRest.getRepourResult();
    }

    public BuildResultRest(BuildResult buildResult) {

        completionStatus = buildResult.getCompletionStatus();
        processException = buildResult.getProcessException().orElse(null);

        if (buildResult.getBuildExecutionConfiguration().isPresent()) {
            BuildExecutionConfiguration bec = buildResult.getBuildExecutionConfiguration().get();
            this.buildExecutionConfiguration = new BuildExecutionConfigurationRest(bec);
        } else {
            this.buildExecutionConfiguration = null;
        }

        if (buildResult.getBuildDriverResult().isPresent()) {
            BuildDriverResult result = buildResult.getBuildDriverResult().get();
            buildDriverResult = new BuildDriverResultRest(result);
        } else {
            this.buildDriverResult = null;
        }

        if (buildResult.getRepositoryManagerResult().isPresent()) {
            RepositoryManagerResult result = buildResult.getRepositoryManagerResult().get();
            repositoryManagerResult = new RepositoryManagerResultRest(result);
        } else {
            this.repositoryManagerResult = null;
        }

        if (buildResult.getEnvironmentDriverResult().isPresent()) {
            environmentDriverResult = buildResult.getEnvironmentDriverResult().get();
        } else {
            environmentDriverResult = null;
        }

        repourResult = buildResult.getRepourResult().orElse(null);
    }

    public BuildResult toBuildResult() {
        RepositoryManagerResult repositoryManagerResult = null;
        if (getRepositoryManagerResult() != null) {
            repositoryManagerResult = getRepositoryManagerResult().toRepositoryManagerResult();
        }

        return new BuildResult(
                completionStatus,
                ofNullable(processException),
                "", ofNullable(buildExecutionConfiguration),
                ofNullable(buildDriverResult),
                ofNullable(repositoryManagerResult),
                ofNullable(environmentDriverResult),
                ofNullable(repourResult));
    }


    @Override
    public String getEventType() {
        return "BUILD_COMPLETE";
    }

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }

    /* //TODO re-implement getter and setters for backcompatibility
    @Getter
    @Setter
    private ExecutorException exception;

    @Getter
    @Setter
    private BuildExecutionStatus failedReasonStatus;

    @Getter
    @Setter
    private SshCredentials sshCredentials;

    @Getter
    @Setter
    private String executionRootName;

    @Getter
    @Setter
    private String executionRootVersion;
    */
}
