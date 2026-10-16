/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.notification;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;

import org.jboss.pnc.enums.JobNotificationType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

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
            @JsonProperty("taskId") String taskId,
            @JsonProperty("message") String message) {
        super(job, notificationType, FINISHED, IN_PROGRESS, message);
        this.data = data;
        this.taskId = taskId;
    }
}
