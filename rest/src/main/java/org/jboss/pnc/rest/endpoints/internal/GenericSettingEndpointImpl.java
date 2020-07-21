/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.response.Banner;
import org.jboss.pnc.rest.api.endpoints.GenericSettingEndpoint;
import org.jboss.pnc.facade.providers.GenericSettingProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GenericSettingEndpointImpl implements GenericSettingEndpoint {

    @Inject
    private GenericSettingProvider genericSettingProvider;

    @Override
    public Banner getAnnouncementBanner() {

        Banner banner = new Banner();
        banner.setBanner(genericSettingProvider.getAnnouncementBanner());
        return banner;
    }

    @Override
    public void setAnnouncementBanner(String banner) {
        genericSettingProvider.setAnnouncementBanner(banner);
    }

    @Override
    public Boolean isInMaintenanceMode() {
        return genericSettingProvider.isInMaintenanceMode();
    }

    @Override
    public void activateMaintenanceMode(String reason) {
        genericSettingProvider.activateMaintenanceMode(reason);

    }

    @Override
    public void deactivateMaintenanceMode() {
        genericSettingProvider.deactivateMaintenanceMode();
    }
}
