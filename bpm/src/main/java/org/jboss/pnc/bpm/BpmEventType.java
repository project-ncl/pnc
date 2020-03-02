/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.bpm;

import org.jboss.pnc.bpm.model.RepositoryCreationSuccess;

import lombok.ToString;
import org.jboss.pnc.bpm.model.BpmEvent;
import org.jboss.pnc.bpm.model.BpmStringMapNotificationRest;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.bpm.model.causeway.MilestoneReleaseResultRest;
import org.jboss.pnc.bpm.model.ProcessProgressUpdate;

import static java.util.Objects.requireNonNull;

/**
 * Types of events that BPM process can send notifications about to PNC. Each type contains two pieces of data - string
 * identifier and type of the received notification. This data is used in deserialization inside BPM REST endpoint, for
 * example.
 *
 * @author Jakub Senko
 */
@ToString
public enum BpmEventType { // TODO merge with org.jboss.pnc.spi.notifications.model.EventType ?
    // <T extends BpmNotificationRest>
    PROCESS_PROGRESS_UPDATE(ProcessProgressUpdate.class),
    DEBUG(BpmStringMapNotificationRest.class),
    BREW_IMPORT_SUCCESS(MilestoneReleaseResultRest.class), // TODO remove
                                                           // SUCCESS|ERROR
                                                           // from the
                                                           // event types ?
    BREW_IMPORT_ERROR(BpmStringMapNotificationRest.class),
    BUILD_COMPLETE(BuildResultRest.class),
    RC_REPO_CREATION_SUCCESS(BpmStringMapNotificationRest.class),
    RC_REPO_CREATION_ERROR(BpmStringMapNotificationRest.class),
    RC_REPO_CLONE_SUCCESS(BpmStringMapNotificationRest.class),
    RC_REPO_CLONE_ERROR(BpmStringMapNotificationRest.class),

    // notification for bpm task completion
    RC_CREATION_SUCCESS(RepositoryCreationSuccess.class),
    RC_CREATION_ERROR(BpmStringMapNotificationRest.class),
    BCC_CONFIG_SET_ADDITION_SUCCESS(BpmStringMapNotificationRest.class),
    BCC_CONFIG_SET_ADDITION_ERROR(BpmStringMapNotificationRest.class);

    private final Class<? extends BpmEvent> type;

    /**
     * @param type Type of the class containing event data received from the process. Usually named *Rest.
     */
    BpmEventType(Class<? extends BpmEvent> type) {
        requireNonNull(type);
        this.type = type;
    }

    public <T extends BpmEvent> Class<T> getType() {
        return (Class<T>) type;
    }

    public static BpmEventType nullableValueOf(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

}
