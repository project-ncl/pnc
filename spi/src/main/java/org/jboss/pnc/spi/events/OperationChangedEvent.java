/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.events;

import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.model.Base32LongID;

public interface OperationChangedEvent {
    Base32LongID getId();

    Class getOperationClass();

    ProgressStatus getPreviousStatus();

    ProgressStatus getStatus();

    OperationResult getResult();
}
