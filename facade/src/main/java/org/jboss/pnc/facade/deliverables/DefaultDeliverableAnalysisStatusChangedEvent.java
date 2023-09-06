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

package org.jboss.pnc.facade.deliverables;

import lombok.Getter;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;

import java.util.List;
import java.util.Optional;

/**
 * @author jakubvanko
 */
@Getter
public class DefaultDeliverableAnalysisStatusChangedEvent implements DeliverableAnalysisStatusChangedEvent {
    private final String operationId;
    private final ProgressStatus status;
    private final OperationResult result;
    private final Optional<String> milestoneId;
    private final List<String> deliverablesUrls;

    public DefaultDeliverableAnalysisStatusChangedEvent(
            String operationId,
            ProgressStatus status,
            OperationResult result,
            Optional<String> milestoneId,
            List<String> deliverablesUrls) {
        this.operationId = operationId;
        this.status = status;
        this.result = result;
        this.milestoneId = milestoneId;
        this.deliverablesUrls = deliverablesUrls;
    }

    public static DefaultDeliverableAnalysisStatusChangedEvent started(
            String operationId,
            Optional<String> milestoneId,
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
            Optional<String> milestoneId,
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
