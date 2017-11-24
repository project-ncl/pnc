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
package org.jboss.pnc.bpm.task;

import lombok.ToString;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.rest.restmodel.BuildExecutionConfigurationRest;
import org.jboss.pnc.rest.restmodel.bpm.ComponentBuildParameters;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author Jakub Senko
 */
@ToString(callSuper = true)
public class BpmBuildTask extends BpmTask {

    private final BuildTask buildTask;

    private static final Logger LOG = LoggerFactory.getLogger(BpmBuildTask.class);

    public BuildTask getBuildTask() {
        return buildTask;
    }

    public BpmBuildTask(BuildTask buildTask) {
        this.buildTask = buildTask;
        setAccessToken(buildTask.getUser().getLoginToken());
    }

    @Override
    protected Serializable getProcessParameters() throws CoreException {

        return new ComponentBuildParameters(
                config.getPncBaseUrl(),
                config.getAproxBaseUrl(),
                config.getRepourBaseUrl(),
                config.getDaBaseUrl(),
                Boolean.valueOf(Optional.ofNullable(config.getCommunityBuild()).orElse("true")),
                Boolean.valueOf(Optional.ofNullable(config.getVersionAdjust()).orElse("false")),
                getBuildExecutionConfiguration(buildTask)
        );
    }

    private BuildExecutionConfigurationRest getBuildExecutionConfiguration(BuildTask buildTask) {

        BuildConfiguration buildConfiguration = buildTask.getBuildConfiguration();
        String contentId = ContentIdentityManager.getBuildContentId(buildConfiguration.getName());

        BuildExecutionConfiguration buildExecutionConfiguration = BuildExecutionConfiguration.build(
                buildTask.getId(),
                contentId,
                buildTask.getUser().getId(),
                buildConfiguration.getBuildScript(),
                buildConfiguration.getName(),
                buildConfiguration.getRepositoryConfiguration().getInternalUrl(),
                buildConfiguration.getScmRevision(),
                buildConfiguration.getRepositoryConfiguration().getExternalUrl(),
                buildConfiguration.getRepositoryConfiguration().isPreBuildSyncEnabled(),
                buildConfiguration.getBuildEnvironment().getSystemImageId(),
                buildConfiguration.getBuildEnvironment().getSystemImageRepositoryUrl(),
                buildConfiguration.getBuildEnvironment().getSystemImageType(),
                buildTask.isPodKeptAfterFailure(),
                buildConfiguration.getGenericParameters(),
                buildConfiguration.getTempBuild(),
                buildConfiguration.getTempBuildTimestamp());

        return new BuildExecutionConfigurationRest(buildExecutionConfiguration);
    }

    @Override
    protected String getProcessId() {
        return config.getComponentBuildProcessId();
    }

    public static Optional<BpmTask> getBpmTaskByBuildTaskId(BpmManager bpmManager, Integer buildTaskId) {
        return bpmManager.getActiveTasks().stream().filter(bpmTask -> {
            if (bpmTask instanceof BpmBuildTask) {
                int buildId = ((BpmBuildTask) bpmTask).getBuildTask().getId();
                return buildId == buildTaskId.intValue();
            } else {
                return false;
            }
        })
        .findFirst();
    }
}
