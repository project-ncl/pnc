/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import java.util.Optional;

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationWithLatestBuild;
import org.jboss.pnc.dto.requests.BuildConfigWithSCMRequest;
import org.jboss.pnc.dto.response.BuildConfigCreationResponse;
import org.jboss.pnc.dto.response.Page;

public interface BuildConfigurationProvider
        extends Provider<Integer, org.jboss.pnc.model.BuildConfiguration, BuildConfiguration, BuildConfigurationRef> {

    Page<BuildConfiguration> getBuildConfigurationsForProductVersion(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productVersionId);

    Page<BuildConfiguration> getBuildConfigurationsForProject(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String projectId);

    Page<BuildConfiguration> getBuildConfigurationsForGroup(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String groupConfigId);

    Page<BuildConfiguration> getBuildConfigurationsForScmRepository(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String scmRepositoryId);

    Page<BuildConfigurationWithLatestBuild> getBuildConfigurationIncludeLatestBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query);

    BuildConfiguration clone(String buildConfigurationId);

    void addDependency(String configId, String dependencyId);

    void removeDependency(String configId, String dependencyId);

    Page<BuildConfiguration> getDependencies(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String configId);

    Page<BuildConfiguration> getDependants(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String configId);

    Page<BuildConfigurationRevision> getRevisions(int pageIndex, int pageSize, String id);

    BuildConfigurationRevision getRevision(String id, Integer rev);

    BuildConfigurationRevision createRevision(String id, BuildConfiguration buildConfiguration);

    BuildConfigCreationResponse createWithScm(BuildConfigWithSCMRequest request);

    Optional<BuildConfiguration> restoreRevision(String id, int rev);

    /**
     * Method is expecting that the {@link org.jboss.pnc.model.RepositoryConfiguration} defined by scmRepositoryId
     * already exists in the database.
     *
     * @param taskId
     * @param scmRepositoryId
     * @param configuration
     */
    void createBuildConfigurationWithRepository(String taskId, int scmRepositoryId, BuildConfiguration configuration);
}
