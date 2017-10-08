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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.rest.restmodel.BuildDriverResultRest;
import org.jboss.pnc.rest.restmodel.BuildExecutionConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryManagerResultRest;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
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
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@XmlRootElement(name = "buildResult")
@NoArgsConstructor(onConstructor = @__({@Deprecated}))
public class BuildResultRest extends BpmNotificationRest implements Serializable {

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private CompletionStatus completionStatus;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private ProcessException processException;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private String processLog;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private BuildExecutionConfigurationRest buildExecutionConfiguration;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private BuildDriverResultRest buildDriverResult;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private RepositoryManagerResultRest repositoryManagerResult;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private EnvironmentDriverResult environmentDriverResult;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private RepourResult repourResult;

    public static BuildResultRest valueOf(String serialized) throws IOException {
        return JsonOutputConverterMapper.readValue(serialized, BuildResultRest.class);
    }

    public BuildResultRest(BuildResult buildResult) {

        completionStatus = buildResult.getCompletionStatus();
        processException = buildResult.getProcessException().orElse(null);
        processLog = buildResult.getProcessLog();

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
                processLog,
                ofNullable(buildExecutionConfiguration),
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

    /**
     * @param maxStringLength
     * @return serailized object with long strings trimmed to maxStringLength
     */
    public String toLogString(int maxStringLength) {
        BuildDriverResult buildDriverResult = null;
        if (BuildResultRest.this.buildDriverResult != null) {
            buildDriverResult = new BuildDriverResult() {
                @Override
                public String getBuildLog() {
                    return StringUtils.trim(BuildResultRest.this.buildDriverResult.getBuildLog(), maxStringLength);
                }

                @Override
                public BuildStatus getBuildStatus() {
                    return BuildResultRest.this.buildDriverResult.getBuildStatus();
                }
            };
        }

        RepositoryManagerResult repositoryManagerResult = null;
        if (BuildResultRest.this.repositoryManagerResult != null) {
            repositoryManagerResult = new RepositoryManagerResultImpl(
                    BuildResultRest.this.repositoryManagerResult.toRepositoryManagerResult(),
                    maxStringLength);
        }

        BuildResult buildResult = new BuildResult(
                this.completionStatus,
                Optional.ofNullable(this.processException),
                StringUtils.trim(this.processLog, maxStringLength),
                Optional.ofNullable(this.buildExecutionConfiguration),
                Optional.ofNullable(buildDriverResult),
                Optional.ofNullable(repositoryManagerResult),
                Optional.ofNullable(this.environmentDriverResult),
                Optional.ofNullable(this.repourResult)
        );
        return JsonOutputConverterMapper.apply(new BuildResultRest(buildResult));
    }

    class RepositoryManagerResultImpl implements RepositoryManagerResult {

        private RepositoryManagerResult repositoryManagerResult;

        private int maxStringLength;

        public RepositoryManagerResultImpl(RepositoryManagerResult repositoryManagerResult, int maxStringLength) {
            this.repositoryManagerResult = repositoryManagerResult;
            this.maxStringLength = maxStringLength;
        }

        @Override
        public List<Artifact> getBuiltArtifacts() {
            return repositoryManagerResult.getBuiltArtifacts();
        }

        @Override
        public List<Artifact> getDependencies() {
            return repositoryManagerResult.getDependencies();
        }

        @Override
        public String getBuildContentId() {
            return repositoryManagerResult.getBuildContentId();
        }

        @Override
        public String getLog() {
            return StringUtils.trim(repositoryManagerResult.getLog(), maxStringLength);
        }

        @Override
        public CompletionStatus getCompletionStatus() {
            return repositoryManagerResult.getCompletionStatus();
        }
    }
}
