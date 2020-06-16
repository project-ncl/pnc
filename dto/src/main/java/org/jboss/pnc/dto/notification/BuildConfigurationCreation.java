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
package org.jboss.pnc.dto.notification;

import lombok.Data;

import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.enums.JobNotificationType;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationType.BUILD_CONFIG_CREATION;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;

/**
 * Notification about created Build Config.
 *
 * <pre>
 * Job: {@link JobNotificationType#BUILD_CONFIG_CREATION} Notification type: {@code BC_CREATION_SUCCESS} - The Build
 * Config was created successfully. {@code BC_CREATION_ERROR} - The Build Config was not created.
 * Progress:{@link JobNotificationProgress#FINISHED} Message: In case of error it contains an error message.
 *
 * <pre>
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class BuildConfigurationCreation extends Notification {

    private static final String BC_CREATION_SUCCESS = "BC_CREATION_SUCCESS";
    private static final String BC_CREATION_ERROR = "BC_CREATION_ERROR";

    /**
     * SCM Repository that was created as part of the job.
     */
    private final SCMRepository scmRepository;

    /**
     * Build configuration that was created by the job. Null in case of failure.
     */
    private final BuildConfigurationRef buildConfig;

    /**
     * Task id of the repository and build config creation task.
     */
    private final String taskId;

    private BuildConfigurationCreation(SCMRepository scmRepository, BuildConfigurationRef buildConfig, String taskId) {
        super(BUILD_CONFIG_CREATION, BC_CREATION_SUCCESS, FINISHED, IN_PROGRESS);
        this.scmRepository = scmRepository;
        this.buildConfig = buildConfig;
        this.taskId = taskId;
    }

    @JsonCreator
    private BuildConfigurationCreation(
            @JsonProperty("scmRepository") SCMRepository scmRepository,
            @JsonProperty("buildConfig") BuildConfigurationRef buildConfig,
            @JsonProperty("message") String message,
            @JsonProperty("taskId") String taskId) {
        super(BUILD_CONFIG_CREATION, BC_CREATION_ERROR, FINISHED, IN_PROGRESS, message);
        this.scmRepository = scmRepository;
        this.buildConfig = buildConfig;
        this.taskId = taskId;
    }

    public static BuildConfigurationCreation success(
            SCMRepository scmRepository,
            BuildConfigurationRef buildConfig,
            String taskId) {
        return new BuildConfigurationCreation(scmRepository, buildConfig, taskId);
    }

    public static BuildConfigurationCreation error(
            SCMRepository scmRepository,
            BuildConfigurationRef buildConfig,
            String errorMessage,
            String taskId) {
        return new BuildConfigurationCreation(scmRepository, buildConfig, errorMessage, taskId);
    }
}
