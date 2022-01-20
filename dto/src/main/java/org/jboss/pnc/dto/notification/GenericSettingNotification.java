
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
package org.jboss.pnc.dto.notification;

import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

public class GenericSettingNotification extends Notification {

    public static final String ANNOUNCEMENT_BANNER_CHANGED = "NEW_ANNOUNCEMENT";
    public static final String MAINTENANCE_STATUS_CHANGED = "MAINTENANCE_STATUS_CHANGED";

    public static GenericSettingNotification newAnnoucement(String message) {
        return new GenericSettingNotification(ANNOUNCEMENT_BANNER_CHANGED, "{\"banner\": \"" + message + "\"}");
    }

    public static GenericSettingNotification maintenanceModeChanged(boolean maintenanceMode) {
        return new GenericSettingNotification(
                MAINTENANCE_STATUS_CHANGED,
                "{\"maintenanceModeEnabled\": " + maintenanceMode + "}");
    }

    private GenericSettingNotification(String typeChanged, String message) {
        super(
                JobNotificationType.GENERIC_SETTING,
                typeChanged,
                JobNotificationProgress.FINISHED,
                JobNotificationProgress.PENDING,
                message);
    }
}
