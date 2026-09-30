/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import java.util.EnumSet;

import org.jboss.pnc.api.enums.LabelOperation;

import lombok.Getter;

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
                operation == LabelOperation.ADDED ? "add" : "remove",
                label,
                operation == LabelOperation.ADDED ? "to" : "from",
                labels,
                reason);
    }
}
