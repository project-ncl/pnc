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
package org.jboss.pnc.rest.endpoints.internal;

import org.jboss.pnc.dto.PncStatus;
import org.jboss.pnc.facade.providers.GenericSettingProvider;
import org.jboss.pnc.rest.api.endpoints.PncStatusEndpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

@ApplicationScoped
public class PncStatusEndpointImpl implements PncStatusEndpoint {

    @Inject
    private GenericSettingProvider genericSettingProvider;

    @Override
    public void setPncStatus(PncStatus pncStatus) {
        if (pncStatus.getBanner() == null && Boolean.FALSE.equals(pncStatus.getIsMaintenanceMode())
                && pncStatus.getEta() != null) {
            throw new BadRequestException("Can't set ETA when maintenance mode is off and banner is null.");
        }

        if (pncStatus.getBanner() != null && pncStatus.getBanner().isEmpty()) {
            throw new BadRequestException("Banner cannot be empty.");
        }

        if (pncStatus.getBanner() != null) {
            genericSettingProvider.setAnnouncementBanner(pncStatus.getBanner());
        }

        if (pncStatus.getEta() != null) {
            genericSettingProvider.setEta(pncStatus.getEta().toString());
        }

        if (pncStatus.getIsMaintenanceMode()) {
            genericSettingProvider.activateMaintenanceMode();
        } else {
            genericSettingProvider.deactivateMaintenanceMode();
        }
    }

    @Override
    public PncStatus getPncStatus() {
        return genericSettingProvider.getPncStatus();
    }
}
