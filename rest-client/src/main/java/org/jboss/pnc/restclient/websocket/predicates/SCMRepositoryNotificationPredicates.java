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
package org.jboss.pnc.restclient.websocket.predicates;

import org.jboss.pnc.dto.notification.RepositoryCreationFailure;
import org.jboss.pnc.dto.notification.SCMRepositoryCreationSuccess;
import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import java.util.function.Predicate;

public class SCMRepositoryNotificationPredicates {

    public SCMRepositoryNotificationPredicates() {
    }

    /**
     * Filter for notifications that say that the repository creation process failed
     *
     * withFailedTaskId and withSuccessTaskId may run in parallel. So it's important to be very specific in the
     * filtering so that we don't accidentally consider a successful notification as a failed one.
     *
     * We consider a notification for a task id as failed if the job notification has 'ERROR' in it. This applies for:
     * RC_REPO_CLONE_ERROR, RC_REPO_CREATION_ERROR, RC_CREATION_ERROR
     *
     * We also want to make sure that the final progress state is 'FINISHED'
     *
     * @param taskId task id to listen for events
     * @return predicate
     */
    public static Predicate<RepositoryCreationFailure> withFailedTaskId(String taskId) {
        return (notification) -> taskId.equals(notification.getTaskId())
                && JobNotificationProgress.FINISHED.equals(notification.getProgress())
                && notification.getNotificationType().contains("ERROR");
    }

    /**
     * Filter for notifications where the scm repository creation is a success!
     *
     * withFailedTaskId and withSuccessTaskId may run in parallel. So it's important to be very specific in the
     * filtering so that we don't accidentally consider a successful notification as a failed one.
     *
     * @param taskId task id to listen for events
     * @return predicate
     */
    public static Predicate<SCMRepositoryCreationSuccess> withSuccessTaskId(String taskId) {
        return (notification) -> taskId.equals(notification.getTaskId())
                && JobNotificationProgress.FINISHED.equals(notification.getProgress())
                && notification.getScmRepository() != null
                && notification.getJob().equals(JobNotificationType.SCM_REPOSITORY_CREATION)
                && notification.getNotificationType().equals(SCMRepositoryCreationSuccess.BC_CREATION_SUCCESS);
    }
}
