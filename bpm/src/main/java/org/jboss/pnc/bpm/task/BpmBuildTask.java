/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.api.constants.Defaults;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationRest;
import org.jboss.pnc.bpm.model.ComponentBuildParameters;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.model.AlignStrategy;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.String.join;
import static org.jboss.pnc.api.constants.ManipulatorKeys.*;
import static org.jboss.pnc.model.utils.DelimitedStringListTypeDescriptor.DELIMITER;

/**
 * @author Jakub Senko
 */
@ToString
public class BpmBuildTask {

    private final BuildTask buildTask;
    private final GlobalModuleGroup globalConfig;

    public BuildTask getBuildTask() {
        return buildTask;
    }

    public BpmBuildTask(BuildTask buildTask, GlobalModuleGroup globalConfig) {
        this.buildTask = buildTask;
        this.globalConfig = globalConfig;
    }

    public Serializable getProcessParameters() {

        return new ComponentBuildParameters(
                globalConfig.getPncUrl(),
                globalConfig.getExternalIndyUrl(),
                globalConfig.getExternalRepourUrl(),
                globalConfig.getExternalDaUrl(),
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
                buildConfigurationAudited.getId().toString(),
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
                buildConfigurationAudited.isBrewPullActive(),
                buildConfigurationAudited.getDefaultAlignmentParams()
                        // TODO convert from this hacky way into dedicated field or add as generic param
                        + ' ' + appendAlignmentStrategies(buildConfigurationAudited),
                buildTask.getBuildOptions().getAlignmentPreference());

        return new BuildExecutionConfigurationRest(buildExecutionConfiguration);
    }

    private String appendAlignmentStrategies(BuildConfigurationAudited buildConfigurationAudited) {
        List<String> toAppend = new ArrayList<>();
        Map<String, AlignStrategy> strats = buildConfigurationAudited.getBuildConfiguration().getAlignStrategies();
        for (var entry : strats.entrySet()) {
            String dependencyScope = entry.getKey();
            AlignStrategy strategy = entry.getValue();
            if (dependencyScope.equals(Defaults.GLOBAL_SCOPE)) {
                appendGlobalManipulatorKeys(toAppend, strategy);
            } else {
                appendManipulatorKeys(toAppend, strategy, dependencyScope);
            }

        }
        return join(" ", toAppend);
    }

    private static void appendGlobalManipulatorKeys(List<String> toAppend, AlignStrategy strategy) {
        if (strategy.getIdRanks() != null && !strategy.getIdRanks().isEmpty()) {
            toAppend.add(format(DEPENDENCY_RANK_PATTERN, join(DELIMITER, strategy.getIdRanks())));
        } else if (strategy.getRanks() != null && !strategy.getRanks().isEmpty()) {
            toAppend.add(format(DEPENDENCY_RANK_PATTERN, join(DELIMITER, strategy.getRanks())));
        }

        if (strategy.getIdDenyList() != null && !strategy.getIdDenyList().isEmpty()) {
            toAppend.add(format(DEPENDENCY_DENY_LIST_PATTERN, strategy.getIdDenyList()));
        } else if (strategy.getDenyList() != null && !strategy.getDenyList().isEmpty()) {
            toAppend.add(format(DEPENDENCY_DENY_LIST_PATTERN, strategy.getDenyList()));
        }

        if (strategy.getIdAllowList() != null && !strategy.getIdAllowList().isEmpty()) {
            toAppend.add(format(DEPENDENCY_ALLOW_LIST_PATTERN, strategy.getIdAllowList()));
        } else if (strategy.getAllowList() != null && !strategy.getAllowList().isEmpty()) {
            toAppend.add(format(DEPENDENCY_ALLOW_LIST_PATTERN, strategy.getAllowList()));
        }
    }

    private static void appendManipulatorKeys(List<String> toAppend, AlignStrategy strategy, String dependencyScope) {
        if (strategy.getIdRanks() != null && !strategy.getIdRanks().isEmpty()) {
            toAppend.add(
                    format(
                            DEPENDENCY_RANK_PATTERN_WITH_OVERRIDE,
                            dependencyScope,
                            join(DELIMITER, strategy.getIdRanks())));
        } else if (strategy.getRanks() != null && !strategy.getRanks().isEmpty()) {
            toAppend.add(
                    format(
                            DEPENDENCY_RANK_PATTERN_WITH_OVERRIDE,
                            dependencyScope,
                            join(DELIMITER, strategy.getRanks())));
        }

        if (strategy.getIdDenyList() != null && !strategy.getIdDenyList().isEmpty()) {
            toAppend.add(format(DEPENDENCY_DENY_LIST_PATTERN_WITH_OVERRIDE, dependencyScope, strategy.getIdDenyList()));
        } else if (strategy.getDenyList() != null && !strategy.getDenyList().isEmpty()) {
            toAppend.add(format(DEPENDENCY_DENY_LIST_PATTERN_WITH_OVERRIDE, dependencyScope, strategy.getDenyList()));
        }

        if (strategy.getIdAllowList() != null && !strategy.getIdAllowList().isEmpty()) {
            toAppend.add(
                    format(DEPENDENCY_ALLOW_LIST_PATTERN_WITH_OVERRIDE, dependencyScope, strategy.getIdAllowList()));
        } else if (strategy.getAllowList() != null && !strategy.getAllowList().isEmpty()) {
            toAppend.add(format(DEPENDENCY_ALLOW_LIST_PATTERN_WITH_OVERRIDE, dependencyScope, strategy.getAllowList()));
        }
    }

    public GlobalModuleGroup getGlobalConfig() {
        return globalConfig;
    }
}
