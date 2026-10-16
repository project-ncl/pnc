/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.restclient.websocket.predicates;

import java.util.function.Predicate;

import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.dto.notification.OperationNotification;

public final class OperationNotificationPredicates {

    public static Predicate<OperationNotification> withOperationFinished() {
        return (notification) -> notification.getOperation().getProgressStatus().equals(ProgressStatus.FINISHED);
    }

    public static Predicate<OperationNotification> withOperationID(String operationID) {
        return (notification) -> notification.getOperation() != null
                && notification.getOperation().getId().equals(operationID);
    }

}
