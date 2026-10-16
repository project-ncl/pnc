/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.validation.DTOValidationException;

public interface GroupConfigurationProvider extends
        Provider<Integer, org.jboss.pnc.model.BuildConfigurationSet, GroupConfiguration, GroupConfigurationRef> {

    Page<GroupConfiguration> getGroupConfigurationsForProductVersion(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productVersionId);

    Page<GroupConfiguration> getGroupConfigurationsForBuildConfiguration(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String bcId);

    void addConfiguration(String id, String configId) throws DTOValidationException;

    void removeConfiguration(String id, String configId) throws DTOValidationException;
}
