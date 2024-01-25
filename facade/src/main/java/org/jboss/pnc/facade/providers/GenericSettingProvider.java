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
package org.jboss.pnc.facade.providers;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.dto.PncStatus;
import org.jboss.pnc.dto.notification.GenericSettingNotification;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.model.GenericSetting;
import org.jboss.pnc.spi.datastore.repositories.GenericSettingRepository;
import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.util.Strings;

import static org.jboss.pnc.api.constants.GenericSettingsKeys.ANNOUNCEMENT_BANNER;
import static org.jboss.pnc.api.constants.GenericSettingsKeys.ETA;
import static org.jboss.pnc.api.constants.GenericSettingsKeys.MAINTENANCE_MODE;
import static org.jboss.pnc.api.constants.GenericSettingsKeys.PNC_VERSION;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;

@PermitAll
@Stateless
@Slf4j
public class GenericSettingProvider {

    @Inject
    private GenericSettingRepository genericSettingRepository;

    @Inject
    private UserService userService;

    @Inject
    private Notifier notifier;

    @Deprecated
    public GenericSettingProvider() {
    }

    @RolesAllowed(USERS_ADMIN)
    public void activateMaintenanceMode(String reason) {

        log.info("Activating Maintenance mode, with reason: '{}'", reason);
        GenericSetting maintenanceMode = createGenericParameterIfNotFound(MAINTENANCE_MODE);

        maintenanceMode.setValue(Boolean.TRUE.toString());
        genericSettingRepository.save(maintenanceMode);
        notifier.sendMessage(GenericSettingNotification.maintenanceModeChanged(true));

        setAnnouncementBanner(reason);
    }

    @RolesAllowed(USERS_ADMIN)
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
        notifier.sendMessage(GenericSettingNotification.maintenanceModeChanged(false));
    }

    public boolean isInMaintenanceMode() {

        GenericSetting maintenanceMode = genericSettingRepository.queryByKey(MAINTENANCE_MODE);

        if (maintenanceMode == null) {
            return false;
        } else {
            return Boolean.parseBoolean(maintenanceMode.getValue());
        }
    }

    public boolean isCurrentUserAllowedToTriggerBuilds() {

        boolean isLoggedInUserSystemUser = userService.hasLoggedInUserRole(USERS_ADMIN);
        if (isLoggedInUserSystemUser) {
            log.info("Users with system-role are always allowed to trigger builds");
            return true;
        }

        return !isInMaintenanceMode();
    }

    @RolesAllowed(USERS_ADMIN)
    public void setPNCVersion(String version) {

        log.info("PNC System version set to: '{}'", version);
        GenericSetting pncVersion = createGenericParameterIfNotFound(PNC_VERSION);
        pncVersion.setValue(version);
        genericSettingRepository.save(pncVersion);
    }

    public String getPNCVersion() {
        return getValueByKey(PNC_VERSION);
    }

    @RolesAllowed(USERS_ADMIN)
    public void setAnnouncementBanner(String banner) {

        log.info("Announcement banner set to: '{}'", banner);
        GenericSetting announcementBanner = createGenericParameterIfNotFound(ANNOUNCEMENT_BANNER);
        announcementBanner.setValue(banner);
        genericSettingRepository.save(announcementBanner);
        notifier.sendMessage(GenericSettingNotification.newAnnoucement(banner));
    }

    public String getAnnouncementBanner() {
        return getValueByKey(ANNOUNCEMENT_BANNER);
    }

    public PncStatus getPncStatus() {
        GenericSetting maintenanceMode = genericSettingRepository.queryByKey(MAINTENANCE_MODE);

        return PncStatus.builder()
                .banner(getAnnouncementBanner())
                .eta(getEta())
                .isMaintenanceMode(transformMaintenanceMode(maintenanceMode))
                .build();
    }

    @RolesAllowed(USERS_ADMIN)
    public void setEta(String eta) {
        log.info("ETA set to: '{}'", eta);
        GenericSetting pncEta = createGenericParameterIfNotFound(ETA);
        pncEta.setValue(eta);
        genericSettingRepository.save(pncEta);
    }

    private String getEta() {
        return getValueByKey(ETA);
    }

    private String getValueByKey(String key) {
        GenericSetting value = genericSettingRepository.queryByKey(key);

        return value == null ? Strings.EMPTY : value.getValue();
    }

    private GenericSetting createGenericParameterIfNotFound(String key) {

        GenericSetting genericSetting = genericSettingRepository.queryByKey(key);

        if (genericSetting == null) {
            genericSetting = new GenericSetting();
            genericSetting.setKey(key);
        }

        return genericSetting;
    }

    private Boolean transformMaintenanceMode(GenericSetting maintenanceMode) {
        return maintenanceMode == null ? null : Boolean.parseBoolean(maintenanceMode.getValue());
    }
}
