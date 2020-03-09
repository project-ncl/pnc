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
package org.jboss.pnc.rest.provider;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.model.GenericSetting;
import org.jboss.pnc.rest.genericsetting.notifications.GenericSettingNotificationSender;
import org.jboss.pnc.rest.restmodel.genericsetting.GenericSettingUpdate;
import org.jboss.pnc.spi.datastore.repositories.GenericSettingRepository;
import org.jboss.util.Strings;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;

@PermitAll
@Stateless
@Slf4j
public class GenericSettingProvider {

    public static final String ANNOUNCEMENT_BANNER = "ANNOUNCEMENT_BANNER";
    public static final String MAINTENANCE_MODE = "MAINTENANCE_MODE";

    @Inject
    private GenericSettingRepository genericSettingRepository;

    @Inject
    private GenericSettingNotificationSender notifier;

    @Deprecated
    public GenericSettingProvider() {}

    @RolesAllowed("system-user")
    public void activateMaintenanceMode(String reason) {

        log.info("Activating Maintenance mode, with reason: '{}'", reason);
        GenericSetting maintenanceMode = createGenericParameterIfNotFound(MAINTENANCE_MODE);

        maintenanceMode.setValue(Boolean.TRUE.toString());
        genericSettingRepository.save(maintenanceMode);
        notifyMaintenanceModeChanged(maintenanceMode);

        setAnnouncementBanner(reason);
    }

    @RolesAllowed("system-user")
    public void deactivateMaintenanceMode() {

        log.info("Deactivating Maintenance mode");
        GenericSetting maintenanceMode = genericSettingRepository.queryByKey(MAINTENANCE_MODE);

        if (maintenanceMode != null && Boolean.parseBoolean(maintenanceMode.getValue())) {
            // reset announcement banner if we switch from maintenance mode on -> off
            setAnnouncementBanner(Strings.EMPTY);
        }

        if (maintenanceMode == null) {
            maintenanceMode = new GenericSetting();
            maintenanceMode.setKey(MAINTENANCE_MODE);
        }

        maintenanceMode.setValue(Boolean.FALSE.toString());
        genericSettingRepository.save(maintenanceMode);
        notifyMaintenanceModeChanged(maintenanceMode);
    }

    public boolean isInMaintenanceMode() {

        GenericSetting maintenanceMode = genericSettingRepository.queryByKey(MAINTENANCE_MODE);

        if (maintenanceMode == null) {
            return false;
        } else {
            return Boolean.parseBoolean(maintenanceMode.getValue());
        }

    }

    @RolesAllowed("system-user")
    public void setAnnouncementBanner(String banner) {

        log.info("Announcement banner set to: '{}'", banner);
        GenericSetting announcementBanner = createGenericParameterIfNotFound(ANNOUNCEMENT_BANNER);
        announcementBanner.setValue(banner);
        genericSettingRepository.save(announcementBanner);
        notifyBannerChanged(announcementBanner);
    }

    public String getAnnouncementBanner() {

        GenericSetting announcementBanner = genericSettingRepository.queryByKey(ANNOUNCEMENT_BANNER);

        if (announcementBanner == null) {
            return Strings.EMPTY;
        } else {
            return announcementBanner.getValue();
        }
    }

    private GenericSetting createGenericParameterIfNotFound(String key) {

        GenericSetting genericSetting = genericSettingRepository.queryByKey(key);

        if (genericSetting == null) {
            genericSetting = new GenericSetting();
            genericSetting.setKey(key);
        }

        return genericSetting;
    }

    private void notifyBannerChanged(GenericSetting genericSetting) {

        String banner = genericSetting.getValue();
        notifier.send(
                new GenericSettingUpdate(GenericSettingUpdate.BANNER_CHANGED, "{\"banner\": \"" + banner + "\"}"));
    }

    private void notifyMaintenanceModeChanged(GenericSetting maintenance) {
        boolean statusBool = Boolean.parseBoolean(maintenance.getValue());
        notifier.send(
                new GenericSettingUpdate(
                        GenericSettingUpdate.MAINTENANCE_STATUS_CHANGED,
                        "{\"maintenanceModeEnabled\": " + statusBool + "}"));
    }
}
