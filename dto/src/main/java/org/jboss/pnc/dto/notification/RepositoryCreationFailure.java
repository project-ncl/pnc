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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import org.jboss.pnc.enums.JobNotificationType;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;

/**
 * Notification about failure in SCM Repository or Build Config creation. This notification is used when there is
 * problem when creating the SCM repository (which is prerequisit for the Build Config creation).
 *
 * <pre>
 * Job: {@link JobNotificationType#BUILD_CONFIG_CREATION} - When the job is to create Build Config.
 * {@link JobNotificationType#SCM_REPOSITORY_CREATION} - When the job is to create SCM Repository. Notification type:
 * {@code RC_REPO_CREATION_ERROR} - Failure while creating the repository in SCM system. {@code RC_REPO_CLONE_ERROR} -
 * Failure while cloning the repository content. {@code RC_CREATION_ERROR} - Failure while creating SCM Repository
 * record. Progress: {@link JobNotificationProgress#FINISHED} Message: no
 *
 * <pre>
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @see BuildConfigurationCreation
 * @see SCMRepositoryCreationSuccess
 */
@Data
public class RepositoryCreationFailure extends Notification {

    /**
     * Object with data describing the failure.
     */
    private final Object data;

    /**
     * Task id of the repository creation task.
     */
    private final String taskId;

    @JsonCreator
    public RepositoryCreationFailure(
            @JsonProperty("job") JobNotificationType job,
            @JsonProperty("notificationType") String notificationType,
            @JsonProperty("data") Object data,
            @JsonProperty("taskId") String taskId) {
        super(job, notificationType, FINISHED, IN_PROGRESS);
        this.data = data;
        this.taskId = taskId;
    }
}
