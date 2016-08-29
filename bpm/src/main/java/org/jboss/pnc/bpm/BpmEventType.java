/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jboss.pnc.rest.restmodel.bpm.BpmNotificationRest;
import org.jboss.pnc.rest.restmodel.bpm.BpmStringMapNotificationRest;
import org.jboss.pnc.rest.restmodel.bpm.BuildResultRest;
import org.jboss.pnc.rest.restmodel.causeway.BrewPushMilestoneResultRest;

import static java.util.Objects.requireNonNull;

/**
 * Types of events that BPM process can send notifications about to PNC.
 * Each type contains two pieces of data - string identifier
 * and type of the received notification. This data is used
 * in deserialization inside BPM REST endpoint, for example.
 * When adding a new one, do not forget to update {@link BpmEventType#events}
 * or otherwise {@link BpmEventType#valueOf(String)} would not work.
 *
 * TODO: replace with enum ?
 *
 * @author Jakub Senko
 */
@EqualsAndHashCode(of = "name")
@ToString
public final class BpmEventType<T extends BpmNotificationRest> {


    public static final BpmEventType<BrewPushMilestoneResultRest> BREW_IMPORT_SUCCESS
            = new BpmEventType<>("BREW_IMPORT_SUCCESS", BrewPushMilestoneResultRest.class);

    public static final BpmEventType<BrewPushMilestoneResultRest> BREW_IMPORT_ERROR
            = new BpmEventType<>("BREW_IMPORT_ERROR", BrewPushMilestoneResultRest.class);

    public static final BpmEventType<BuildResultRest> BUILD_COMPLETE
            = new BpmEventType<>("BUILD_COMPLETE", BuildResultRest.class);

    public static final BpmEventType<BpmStringMapNotificationRest> BCC_REPO_CREATION_SUCCESS
            = new BpmEventType<>("BCC_REPO_CREATION_SUCCESS", BpmStringMapNotificationRest.class);

    public static final BpmEventType<BpmStringMapNotificationRest> BCC_REPO_CREATION_ERROR
            = new BpmEventType<>("BCC_REPO_CREATION_ERROR", BpmStringMapNotificationRest.class);

    public static final BpmEventType<BpmStringMapNotificationRest> BCC_REPO_CLONE_SUCCESS
            = new BpmEventType<>("BCC_REPO_CLONE_SUCCESS", BpmStringMapNotificationRest.class);

    public static final BpmEventType<BpmStringMapNotificationRest> BCC_REPO_CLONE_ERROR
            = new BpmEventType<>("BCC_REPO_CLONE_ERROR", BpmStringMapNotificationRest.class);

    public static final BpmEventType<BpmStringMapNotificationRest> BCC_CREATION_SUCCESS
            = new BpmEventType<>("BCC_CREATION_SUCCESS", BpmStringMapNotificationRest.class);

    public static final BpmEventType<BpmStringMapNotificationRest> BCC_CREATION_ERROR
            = new BpmEventType<>("BCC_CREATION_ERROR", BpmStringMapNotificationRest.class);

    /**
     * Used for {@link BpmEventType#valueOf(String)}.
     */
    private static final BpmEventType[] events = {
            BUILD_COMPLETE,
            BCC_REPO_CREATION_SUCCESS,
            BCC_REPO_CREATION_ERROR,
            BCC_REPO_CLONE_SUCCESS,
            BCC_REPO_CLONE_ERROR,
            BCC_CREATION_SUCCESS,
            BCC_CREATION_ERROR,
            BREW_IMPORT_SUCCESS,
            BREW_IMPORT_ERROR
    };


    public static BpmEventType<?> valueOf(String name) {
        for (BpmEventType<?> event : events) {
            if (event.getName().equals(name)) {
                return event;
            }
        }
        return null;
    }


    private final String name;

    private final Class<T> type;

    /**
     * @param name Unique event identifier used e.g. for correct (de)serialization of events.
     * @param type Type of the class containing event data received from the process.
     *             Usually named *Rest.
     */
    BpmEventType(String name, Class<T> type) {
        requireNonNull(name);
        requireNonNull(type);
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

}
