/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.impl;

import org.hibernate.Hibernate;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.Operation;
import org.jboss.pnc.spi.events.OperationChangedEvent;

import lombok.Value;

@Value
public class OperationChangedEventImpl implements OperationChangedEvent {

    private final Base32LongID id;
    private final Class operationClass;
    private final ProgressStatus previousStatus;
    private final ProgressStatus status;
    private final OperationResult result;

    public OperationChangedEventImpl(Operation operation, ProgressStatus previousStatus) {
        this.id = operation.getId();
        this.operationClass = Hibernate.getClass(operation);
        this.previousStatus = previousStatus;
        this.status = operation.getProgressStatus();
        this.result = operation.getResult();
    }
}
