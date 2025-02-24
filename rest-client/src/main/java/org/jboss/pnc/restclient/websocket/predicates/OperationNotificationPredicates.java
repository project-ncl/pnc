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
