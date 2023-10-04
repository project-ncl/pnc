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
package org.jboss.pnc.facade.validation;

import lombok.Getter;
import org.jboss.pnc.api.enums.LabelOperation;

import java.util.EnumSet;

/**
 * This exception is being thrown in case the operation would lead the deliverable analyzer report to inconsistent state
 * (e.g. adding RELEASED label when the report was already marked as DELETED) or in case of unexpected operation (e.g.
 * adding SCRATCH label when the SCRATCH label is already present).
 */
@Getter
public class InvalidLabelOperationException extends RuntimeException {

    private final Enum<?> label;

    private final EnumSet<? extends Enum<?>> labels;

    private final LabelOperation operation;

    private final String reason;

    public InvalidLabelOperationException(
            Enum<?> label,
            EnumSet<? extends Enum<?>> labels,
            LabelOperation operation,
            String reason) {
        super();

        this.label = label;
        this.labels = labels;
        this.operation = operation;
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        return String.format(
                "Unable to %s the label %s %s labels: %s: %s",
                operation.getPresentTense(),
                label,
                operation == LabelOperation.ADDED ? "to" : "from",
                labels,
                reason);
    }
}
