/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.notification;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.JobNotificationType.SCM_REPOSITORY_CREATION;

import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Notification about created SCM Repository.
 *
 * <pre>
 * Job: {@link JobNotificationType#SCM_REPOSITORY_CREATION} Notification type: {@code SCMR_CREATION_SUCCESS}
 * Progress:{@link JobNotificationProgress#FINISHED} Message: no
 *
 * <pre>
 * For notification about failure see {@link RepositoryCreationFailure}.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class SCMRepositoryCreationSuccess extends Notification {

    public static final String BC_CREATION_SUCCESS = "SCMR_CREATION_SUCCESS";

    /**
     * The created SCM Repository.
     */
    private final SCMRepository scmRepository;

    /**
     * Task id of the repository creation task.
     */
    private final String taskId;

    @JsonCreator
    public SCMRepositoryCreationSuccess(
            @JsonProperty("scmRepository") SCMRepository scmRepository,
            @JsonProperty("taskId") String taskId) {
        super(SCM_REPOSITORY_CREATION, BC_CREATION_SUCCESS, FINISHED, IN_PROGRESS);
        this.scmRepository = scmRepository;
        this.taskId = taskId;
    }
}
