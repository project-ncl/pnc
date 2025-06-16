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
package org.jboss.pnc.dto.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.dto.Operation;
import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import static org.jboss.pnc.enums.JobNotificationType.OPERATION;

/**
 * Notification about Operation type job progress.
 *
 * <pre>
 * Job: {@link JobNotificationType#OPERATION} Notification type: depends on the concrete operation type Progress:
 * {@link JobNotificationProgress#PENDING} - the operation is about to start {@link JobNotificationProgress#IN_PROGRESS}
 * - the operation is in progress {@link JobNotificationProgress#FINISHED} - the operation has finished and the result
 * is set. Message: no
 * 
 * <pre>
 *
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class OperationNotification extends Notification {

    /**
     * The operation id.
     */
    private final String operationId;

    /**
     * The operation result.
     */
    private final OperationResult result;

    private final Operation operation;

    public OperationNotification(
            String notificationType,
            String operationId,
            ProgressStatus newStatus,
            ProgressStatus oldStatus,
            OperationResult result,
            Operation operation) {
        // Note: Only one constructor should have @JsonCreator!
        this(notificationType, operationId, newStatus, oldStatus, null, result, operation);
    }

    @JsonCreator
    public OperationNotification(
            @JsonProperty("notificationType") String notificationType,
            @JsonProperty("operationId") String operationId,
            @JsonProperty("progress") ProgressStatus newStatus,
            @JsonProperty("oldProgress") ProgressStatus oldStatus,
            @JsonProperty("message") String message,
            @JsonProperty("result") OperationResult result,
            @JsonProperty("operation") Operation operation) {
        // When using @JsonCreator to a constructor with more than one parameter, we have to add the @JsonProperty
        super(OPERATION, notificationType, convert(newStatus), convert(oldStatus), message);
        this.operationId = operationId;
        this.result = result;
        this.operation = operation;
    }

    private static JobNotificationProgress convert(ProgressStatus status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case NEW:
            case PENDING:
                return JobNotificationProgress.PENDING;
            case IN_PROGRESS:
                return JobNotificationProgress.IN_PROGRESS;
            case FINISHED:
                return JobNotificationProgress.FINISHED;
            default:
                throw new IllegalArgumentException("Unknown Progress Status.");
        }
    }
}
