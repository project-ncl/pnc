/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(BuildResultRest.class);

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
        return "BuildResultRest{" +
                "completionStatus=" + completionStatus +
                ", processException=" + processException +
                ", processLog='" + StringUtils.trim(processLog, 100) + '\'' +
                ", buildExecutionConfiguration=" + buildExecutionConfiguration +
                ", buildDriverResult=" + (buildDriverResult == null ? null : buildDriverResult.toStringLimited()) +
                ", repositoryManagerResult=" + (repositoryManagerResult == null ? null : repositoryManagerResult.toStringLimited()) +
                ", environmentDriverResult=" + (environmentDriverResult == null ? null : environmentDriverResult.toStringLimited()) +
                ", repourResult=" + (repourResult == null ? null : repourResult.toStringLimited()) +
                '}';
    }

    public String toFullLogString() {
        return JsonOutputConverterMapper.apply(this);

    }

}
