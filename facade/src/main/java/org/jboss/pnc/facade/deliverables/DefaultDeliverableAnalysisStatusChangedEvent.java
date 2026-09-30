/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.deliverables;

import java.util.List;

import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;

import lombok.Getter;

/**
 * @author jakubvanko
 */
@Getter
public class DefaultDeliverableAnalysisStatusChangedEvent implements DeliverableAnalysisStatusChangedEvent {
    private final String operationId;
    private final ProgressStatus status;
    private final OperationResult result;
    private final String milestoneId;
    private final List<String> deliverablesUrls;

    public DefaultDeliverableAnalysisStatusChangedEvent(
            String operationId,
            ProgressStatus status,
            OperationResult result,
            String milestoneId,
            List<String> deliverablesUrls) {
        this.operationId = operationId;
        this.status = status;
        this.result = result;
        this.milestoneId = milestoneId;
        this.deliverablesUrls = deliverablesUrls;
    }

    public static DefaultDeliverableAnalysisStatusChangedEvent started(
            String operationId,
            String milestoneId,
            List<String> deliverablesUrls) {
        return new DefaultDeliverableAnalysisStatusChangedEvent(
                operationId,
                ProgressStatus.IN_PROGRESS,
                null,
                milestoneId,
                deliverablesUrls);
    }

    public static DefaultDeliverableAnalysisStatusChangedEvent finished(
            String operationId,
            String milestoneId,
            OperationResult result,
            List<String> deliverablesUrls) {
        return new DefaultDeliverableAnalysisStatusChangedEvent(
                operationId,
                ProgressStatus.FINISHED,
                result,
                milestoneId,
                deliverablesUrls);
    }

    @Override
    public String toString() {
        return "DefaultAnalysisStatusChangedEvent{" + "status=" + status + ", milestoneId=" + milestoneId
                + ", deliverablesUrls=" + String.join(";", deliverablesUrls) + '}';
    }
}
