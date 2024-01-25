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
import org.jboss.util.Strings;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PncStatusEndpointImpl implements PncStatusEndpoint {

    @Inject
    private GenericSettingProvider genericSettingProvider;

    @Override
    public void setPncStatus(PncStatus pncStatus) {
        // banner
        if (pncStatus.getEta() == null && pncStatus.getIsMaintenanceMode() == null) {
            genericSettingProvider.setAnnouncementBanner(pncStatus.getBanner());
            return;
        }

        // activate maintenance mode
        if (Boolean.TRUE.equals(pncStatus.getIsMaintenanceMode()) && pncStatus.getEta() != null) {
            genericSettingProvider.activateMaintenanceMode(pncStatus.getBanner());
            genericSettingProvider.setEta(pncStatus.getEta());
            return;
        }

        // deactivate maintenance mode
        if (Boolean.FALSE.equals(pncStatus.getIsMaintenanceMode()) && pncStatus.getEta() == null) {
            genericSettingProvider.deactivateMaintenanceMode();
            genericSettingProvider.setAnnouncementBanner(pncStatus.getBanner());
            genericSettingProvider.setEta(Strings.EMPTY);
            return;
        }

        // TODO: Handle invalid request, ask Mr. who's sitting by the window on the door side in your office
    }

    @Override
    public PncStatus getPncStatus() {
        return genericSettingProvider.getPncStatus();
    }
}
