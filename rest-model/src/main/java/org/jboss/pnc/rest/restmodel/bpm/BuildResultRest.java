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
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

import static java.util.Optional.*;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@XmlRootElement(name = "buildResult")
public class BuildResultRest extends BpmNotificationRest implements Serializable {

    @Getter
    @Setter
    private BuildExecutionConfigurationRest buildExecutionConfiguration;

    @Getter
    @Setter
    private BuildDriverResultRest buildDriverResult;

    @Getter
    @Setter
    private RepositoryManagerResultRest repositoryManagerResult;

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

    public BuildResultRest() {
    }

    public BuildResultRest(String serialized) throws IOException {
        BuildResultRest buildResultRest = JsonOutputConverterMapper.readValue(serialized, BuildResultRest.class);
        this.buildExecutionConfiguration = buildResultRest.getBuildExecutionConfiguration();
        this.buildDriverResult = buildResultRest.getBuildDriverResult();
        this.repositoryManagerResult = buildResultRest.getRepositoryManagerResult();
        this.exception = buildResultRest.getException();
        this.failedReasonStatus = buildResultRest.getFailedReasonStatus();
        this.sshCredentials = buildResultRest.getSshCredentials();
        this.executionRootName = buildResultRest.getExecutionRootName();
        this.executionRootVersion = buildResultRest.getExecutionRootVersion();
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

        failedReasonStatus = buildResult.getFailedReasonStatus().orElse(null);

        exception = buildResult.getException().orElse(null);

        sshCredentials = buildResult.getSshCredentials().orElse(null);
    }

    public BuildResult toBuildResult() {
        RepositoryManagerResult repositoryManagerResult = null;
        if (getRepositoryManagerResult() != null) {
            repositoryManagerResult = getRepositoryManagerResult().toRepositoryManagerResult();
        }
        return new BuildResult(
                ofNullable(buildExecutionConfiguration),
                ofNullable(buildDriverResult),
                ofNullable(repositoryManagerResult),
                ofNullable(exception),
                ofNullable(failedReasonStatus),
                ofNullable(sshCredentials),
                ofNullable(executionRootName),
                ofNullable(executionRootVersion));
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
