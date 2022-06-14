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
package org.jboss.pnc.facade.impl;

import lombok.Value;
import org.hibernate.Hibernate;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.Operation;
import org.jboss.pnc.spi.events.OperationChangedEvent;

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
