/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints.internal;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.dto.response.Banner;
import org.jboss.pnc.facade.providers.GenericSettingProvider;
import org.jboss.pnc.rest.api.endpoints.GenericSettingEndpoint;
import org.jboss.util.Strings;

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
        genericSettingProvider.notifyListeners();
    }

    @Override
    public String getPNCVersion() {
        return genericSettingProvider.getPNCVersion();
    }

    @Override
    public void setPNCVersion(String version) {
        genericSettingProvider.setPNCVersion(version);
        genericSettingProvider.notifyListeners();
    }

    @Override
    public Boolean isInMaintenanceMode() {
        return genericSettingProvider.isInMaintenanceMode();
    }

    @Override
    public Boolean isCurrentUserAllowedToTriggerBuilds() {
        return genericSettingProvider.isCurrentUserAllowedToTriggerBuilds();
    }

    @Override
    public void activateMaintenanceMode(String reason) {
        genericSettingProvider.activateMaintenanceMode();
        genericSettingProvider.setAnnouncementBanner(reason); // For backwards-compatibility
        genericSettingProvider.notifyListeners();

    }

    @Override
    public void deactivateMaintenanceMode() {
        genericSettingProvider.deactivateMaintenanceMode();
        genericSettingProvider.setAnnouncementBanner(Strings.EMPTY); // For backwards-compatibility
        genericSettingProvider.notifyListeners();
    }

    @Override
    public Set<String> getLimitedBuildUsers() {
        return genericSettingProvider.getLimitedBuildUsers();
    }

    @Override
    public void addLimitedBuildUser(String username) {
        genericSettingProvider.addLimitedBuildUser(username);
    }

    @Override
    public void removeLimitedBuildUser(String username) {
        genericSettingProvider.removeLimitedBuildUser(username);
    }
}
