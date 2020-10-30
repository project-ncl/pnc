/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.bpm.ConnectorSelector;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationRest;
import org.jboss.pnc.bpm.model.ComponentBuildParameters;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.utils.ContentIdentityManager;
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
        super(buildTask.getUser().getLoginToken());
        this.buildTask = buildTask;
    }

    @Override
    protected Serializable getProcessParameters() throws CoreException {

        return new ComponentBuildParameters(
                globalConfig.getPncUrl(),
                globalConfig.getExternalIndyUrl(),
                globalConfig.getExternalRepourUrl(),
                globalConfig.getExternalDaUrl(),
                Boolean.valueOf(Optional.ofNullable(config.getCommunityBuild()).orElse("true")),
                Boolean.valueOf(Optional.ofNullable(config.getVersionAdjust()).orElse("false")),
                getBuildExecutionConfiguration(buildTask));
    }

    private BuildExecutionConfigurationRest getBuildExecutionConfiguration(BuildTask buildTask) {

        BuildConfigurationAudited buildConfigurationAudited = buildTask.getBuildConfigurationAudited();
        String contentId = ContentIdentityManager.getBuildContentId(buildTask.getId());

        BuildExecutionConfiguration buildExecutionConfiguration = BuildExecutionConfiguration.build(
                buildTask.getId(),
                contentId,
                buildTask.getUser().getId().toString(),
                buildConfigurationAudited.getBuildScript(),
                buildConfigurationAudited.getName(),
                // TODO update to use also other parts or Repository Configuration
                buildConfigurationAudited.getRepositoryConfiguration().getInternalUrl(),
                buildConfigurationAudited.getScmRevision(),
                // SCM Tag is about to be set once it is created after the alignment phase
                null,
                buildConfigurationAudited.getRepositoryConfiguration().getExternalUrl(),
                buildConfigurationAudited.getRepositoryConfiguration().isPreBuildSyncEnabled(),
                buildConfigurationAudited.getBuildEnvironment().getSystemImageId(),
                buildConfigurationAudited.getBuildEnvironment().getSystemImageRepositoryUrl(),
                buildConfigurationAudited.getBuildEnvironment().getSystemImageType(),
                buildConfigurationAudited.getBuildConfiguration().getBuildType(),
                buildTask.getBuildOptions().isKeepPodOnFailure(),
                buildConfigurationAudited.getGenericParameters(),
                buildTask.getBuildOptions().isTemporaryBuild(),
                TimeUtils.generateTimestamp(
                        buildTask.getBuildOptions().isTimestampAlignment(),
                        buildTask.getBuildSetTask().getStartTime()),
                buildConfigurationAudited.getDefaultAlignmentParams());

        return new BuildExecutionConfigurationRest(buildExecutionConfiguration);
    }

    @Override
    public String getProcessId() {
        if (ConnectorSelector.useNewProcess(this)) {
            return config.getBpmNewBuildProcessName();
        } else {
            return config.getComponentBuildProcessId();
        }
    }

    public static Optional<BpmTask> getBpmTaskByBuildTaskId(BpmManager bpmManager, Integer buildTaskId) {
        return bpmManager.getActiveTasks().stream().filter(bpmTask -> {
            if (bpmTask instanceof BpmBuildTask) {
                int buildId = ((BpmBuildTask) bpmTask).getBuildTask().getId();
                return buildId == buildTaskId.intValue();
            } else {
                return false;
            }
        }).findFirst();
    }
}
