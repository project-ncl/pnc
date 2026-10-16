/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

import org.jboss.pnc.dto.PncStatus;
import org.jboss.pnc.facade.providers.GenericSettingProvider;
import org.jboss.pnc.rest.api.endpoints.PncStatusEndpoint;

@ApplicationScoped
public class PncStatusEndpointImpl implements PncStatusEndpoint {

    @Inject
    private GenericSettingProvider genericSettingProvider;

    @Override
    public void setPncStatus(PncStatus pncStatus) {
        var isBannerEmpty = pncStatus.getBanner() == null || pncStatus.getBanner().isBlank();

        if (isBannerEmpty && Boolean.FALSE.equals(pncStatus.getIsMaintenanceMode()) && pncStatus.getEta() != null) {
            throw new BadRequestException("Can't set ETA when maintenance mode is off and banner is null or empty.");
        }

        if (isBannerEmpty && Boolean.TRUE.equals(pncStatus.getIsMaintenanceMode())) {
            throw new BadRequestException("Can't set maintenance mode when banner is null or empty.");
        }

        if (isBannerEmpty) {
            genericSettingProvider.clearAnnouncementBanner();
        } else {
            genericSettingProvider.setAnnouncementBanner(pncStatus.getBanner());
        }

        if (pncStatus.getEta() == null) {
            genericSettingProvider.clearEta();
        } else {
            genericSettingProvider.setEta(pncStatus.getEta().toString());
        }

        if (pncStatus.getIsMaintenanceMode()) {
            genericSettingProvider.activateMaintenanceMode();
        } else {
            genericSettingProvider.deactivateMaintenanceMode();
        }

        // Notify listeners now, when everything is set to the latest value
        genericSettingProvider.notifyListeners();
    }

    @Override
    public PncStatus getPncStatus() {
        return genericSettingProvider.getPncStatus();
    }
}
